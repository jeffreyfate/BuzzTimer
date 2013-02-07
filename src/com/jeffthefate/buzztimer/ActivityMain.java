package com.jeffthefate.buzztimer;

import net.simonvt.widget.NumberPicker;
import net.simonvt.widget.NumberPicker.OnValueChangeListener;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.Toast;

import com.jeffthefate.buzztimer.TimerService.TimerBinder;
import com.jeffthefate.buzztimer.TimerService.UiCallback;

public class ActivityMain extends FragmentActivity implements UiCallback {
    
    private FragmentManager fMan;
    private static TextView minText;
    private NumberPicker minPicker;
    private static TextView secText;
    private static TextView msecText;
    private NumberPicker secPicker;
    private LinearLayout timeLayout;
    
    private CheckBox loopCheck;
    private Button doneButton;
    
    private SharedPreferences sharedPrefs;
    
    private boolean bound;
    private boolean loop;
    
    private int newMin = 1;
    private int newSec = 0;
    
    public static final int TIMER_SET = 0;
    public static final int TIMER_TICK = 1;
    
    private UiCallback uiCallback = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fMan = getSupportFragmentManager();
        setContentView(R.layout.main);
        minText = (TextView) findViewById(R.id.MinuteText);
        minPicker = (NumberPicker) findViewById(R.id.MinutePicker);
        minPicker.setMaxValue(120);
        minPicker.setMinValue(0);
        minPicker.setValue(1);
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
        secPicker.setValue(0);
        secPicker.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal,
                    int newVal) {
                newSec = newVal;
            }
        });
        msecText = (TextView) findViewById(R.id.MillisecondText);
        timeLayout = (LinearLayout) findViewById(R.id.TimeLayout);
        timeLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(Constants.LOG_TAG, "ActivityMain mSecs: " + ApplicationEx.getMsecs());
                if (ApplicationEx.getMsecs() < 5000)
                    Toast.makeText(ApplicationEx.getApp(),
                            "Must be at least 5 seconds",
                            Toast.LENGTH_LONG).show();
                else {
                    if (mService != null)
                        mService.startTimer();
                }
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
                    msecText.setVisibility(View.INVISIBLE);
                    newMin = minPicker.getValue();
                    newSec = secPicker.getValue();
                }
                else {
                    if (newMin < 1 && newSec < 5)
                        Toast.makeText(ApplicationEx.getApp(),
                                "Must be at least 5 seconds",
                                Toast.LENGTH_LONG).show();
                    else {
                        mService.setTime(newMin, newSec, loopCheck.isChecked());
                        minText.setVisibility(View.VISIBLE);
                        secText.setVisibility(View.VISIBLE);
                        msecText.setVisibility(View.VISIBLE);
                        minPicker.setVisibility(View.INVISIBLE);
                        secPicker.setVisibility(View.INVISIBLE);
                        loopCheck.setVisibility(View.INVISIBLE);
                        doneButton.setVisibility(View.INVISIBLE);
                    }
                }
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
                if (newMin < 1 && newSec < 5)
                    Toast.makeText(ApplicationEx.getApp(),
                            "Must be at least 5 seconds",
                            Toast.LENGTH_LONG).show();
                else {
                    mService.setTime(newMin, newSec, loopCheck.isChecked());
                    minText.setVisibility(View.VISIBLE);
                    secText.setVisibility(View.VISIBLE);
                    msecText.setVisibility(View.VISIBLE);
                    minPicker.setVisibility(View.INVISIBLE);
                    secPicker.setVisibility(View.INVISIBLE);
                    loopCheck.setVisibility(View.INVISIBLE);
                    doneButton.setVisibility(View.INVISIBLE);
                }
            }
        });
        if (savedInstanceState != null) {
            minText.setText(savedInstanceState.getString("minText"));
            secText.setText(savedInstanceState.getString("secText"));
            msecText.setText(savedInstanceState.getString("msecText"));
        }
        else
            updateTime(ApplicationEx.getMsecs());
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
                ApplicationEx.getApp());
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("minText", minText.getText().toString());
        outState.putString("secText", secText.getText().toString());
        outState.putString("msecText", msecText.getText().toString());
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
        loop = sharedPrefs.getBoolean(getString(R.string.loop_key), true);
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
            unbindService(mConnection);
            bound = false;
        }
        super.onPause();
    }
    
    private static TimerService mService;
    
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerBinder binder = (TimerBinder) service;
            mService = binder.getService();
            mService.setUiCallback(uiCallback);
            bound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };
    
    @Override
    public void updateTime(int mSecs) {
        int mins = (int)((mSecs-(mSecs%60000))/60000);
        int secs = (int)((mSecs%60000)/1000);
        int millis = (int)((mSecs%1000)/100);
        if (mins < 100) {
            if (mins < 10)
                minText.setText("00" + mins);
            else
                minText.setText("0" + mins);
        }
        else
            minText.setText(Integer.toString(mins));
        secText.setText(secs < 10 ? "0" + secs : Integer.toString(secs));
        msecText.setText(Integer.toString(millis));
    }

}
