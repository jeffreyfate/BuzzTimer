package com.jeffthefate.buzztimer;

import net.simonvt.widget.NumberPicker;
import net.simonvt.widget.NumberPicker.OnValueChangeListener;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jeffthefate.buzztimer.TimerService.TimerBinder;

public class ActivityMain extends FragmentActivity {
    
    private FragmentManager fMan;
    private static TextView minText;
    private NumberPicker minPicker;
    private static TextView secText;
    private NumberPicker secPicker;
    private LinearLayout timeLayout;
    
    private CheckBox loopCheck;
    private Button doneButton;
    
    private SharedPreferences sharedPrefs;
    
    private boolean bound;
    
    private int newMin = 1;
    private int newSec = 0;
    
    public static final int TIMER_SET = 0;
    public static final int TIMER_TICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fMan = getSupportFragmentManager();
        setContentView(R.layout.main);
        minText = (TextView) findViewById(R.id.MinuteText);
        minPicker = (NumberPicker) findViewById(R.id.MinutePicker);
        minPicker.setMaxValue(120);
        minPicker.setMinValue(0);
        minPicker.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal,
                    int newVal) {
                newMin = newVal;
            }
        });
        secText = (TextView) findViewById(R.id.SecondText);
        secPicker = (NumberPicker) findViewById(R.id.SecondPicker);
        secPicker.setMaxValue(59);
        secPicker.setMinValue(0);
        secPicker.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal,
                    int newVal) {
                newSec = newVal;
            }
        });
        timeLayout = (LinearLayout) findViewById(R.id.TimeLayout);
        timeLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService != null)
                    mService.startTimer();
            }
        });
        timeLayout.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (minPicker.getVisibility() == View.INVISIBLE &&
                        secPicker.getVisibility() == View.INVISIBLE) {
                    minPicker.setVisibility(View.VISIBLE);
                    secPicker.setVisibility(View.VISIBLE);
                    loopCheck.setVisibility(View.VISIBLE);
                    doneButton.setVisibility(View.VISIBLE);
                    minText.setVisibility(View.INVISIBLE);
                    secText.setVisibility(View.INVISIBLE);
                }
                else {
                    mService.setTime(newMin, newSec, loopCheck.isChecked());
                    minText.setVisibility(View.VISIBLE);
                    secText.setVisibility(View.VISIBLE);
                    minPicker.setVisibility(View.INVISIBLE);
                    secPicker.setVisibility(View.INVISIBLE);
                    loopCheck.setVisibility(View.INVISIBLE);
                    doneButton.setVisibility(View.INVISIBLE);
                }
                //DialogFragment dialogFragment = new FragmentTimePickerDialog();
                //dialogFragment.show(fMan, "fTimePicker");
                return true;
            }
        });
        loopCheck = (CheckBox) findViewById(R.id.LoopCheck);
        loopCheck.setVisibility(View.INVISIBLE);
        loopCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                sharedPrefs.edit().putBoolean(getString(R.string.loop_key),
                        isChecked).commit();
            }
        });
        doneButton = (Button) findViewById(R.id.DoneButton);
        doneButton.setVisibility(View.INVISIBLE);
        doneButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mService.setTime(newMin, newSec, loopCheck.isChecked());
                minText.setVisibility(View.VISIBLE);
                secText.setVisibility(View.VISIBLE);
                minPicker.setVisibility(View.INVISIBLE);
                secPicker.setVisibility(View.INVISIBLE);
                loopCheck.setVisibility(View.INVISIBLE);
                doneButton.setVisibility(View.INVISIBLE);
            }
        });
        if (savedInstanceState != null) {
            minText.setText(savedInstanceState.getString("minText"));
            secText.setText(savedInstanceState.getString("secText"));
        }
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
                ApplicationEx.getApp());
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("minText", minText.getText().toString());
        outState.putString("secText", secText.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mService == null) {
            Intent timerIntent = new Intent(getApplicationContext(),
                    TimerService.class);
            getApplicationContext().startService(timerIntent);
        }
        bindService(new Intent(getApplicationContext(), TimerService.class),
                mConnection, 0);
        
    }
    
    @Override
    public void onPause() {
        if (bound) {
            mService.removeClient(mMessenger);
            unbindService(mConnection);
            bound = false;
        }
        super.onPause();
    }
    
    private static TimerService mService;
    
    private static class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.i("BuzzTimer", "what: " + msg.what);
            Log.i("BuzzTimer", "arg1: " + msg.arg1);
            Log.i("BuzzTimer", "arg2: " + msg.arg2);
            int minute = msg.arg1;
            int second = msg.arg2;
            switch(msg.what) {
            case TIMER_SET:
                minText.setText(minute < 10 ? "0" + Integer.toString(minute) : Integer.toString(minute));
                secText.setText(second < 10 ? "0" + Integer.toString(second) : Integer.toString(second));
                break;
            case TIMER_TICK:
                minText.setText(minute < 10 ? "0" + Integer.toString(minute) : Integer.toString(minute));
                secText.setText(second < 10 ? "0" + Integer.toString(second) : Integer.toString(second));
                break;
            default:
                super.handleMessage(msg);
                break;
            }
        }
    }
    
    final Messenger mMessenger = new Messenger(new TimerHandler());
    
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerBinder binder = (TimerBinder) service;
            mService = binder.getService();
            mService.addClient(mMessenger);
            bound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

}
