package com.jeffthefate.buzztimer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jeffthefate.buzztimer.FragmentTimePickerDialog.TimeSetListener;
import com.jeffthefate.buzztimer.TimerService.TimerBinder;

public class ActivityMain extends FragmentActivity implements TimeSetListener {
    
    private FragmentManager fMan;
    private static TextView minText;
    private static TextView secText;
    private LinearLayout timeLayout;
    
    private boolean bound;
    private long mSecs;
    
    public static final int TIMER_SET = 0;
    public static final int TIMER_TICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fMan = getSupportFragmentManager();
        setContentView(R.layout.main);
        Intent timerIntent = new Intent(getApplicationContext(),
                TimerService.class);
        getApplicationContext().startService(timerIntent);
        minText = (TextView) findViewById(R.id.MinuteText);
        secText = (TextView) findViewById(R.id.SecondText);
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
                DialogFragment dialogFragment = new FragmentTimePickerDialog();
                dialogFragment.show(fMan, "fTimePicker");
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        bindService(new Intent(this, TimerService.class), mConnection, 0);
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

    @Override
    public void onTimeSet(int minute, int second) {
        mService.setTime(minute, second);
    }
    
    private static TimerService mService;
    
    private static class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
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
