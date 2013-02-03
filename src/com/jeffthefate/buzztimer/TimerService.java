package com.jeffthefate.buzztimer;

import java.util.ArrayList;

import com.jeffthefate.buzztimer.FragmentTimePickerDialog.TimeSetListener;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.Log;

/**
 * Service that runs in the background.  Registers receivers for actions that
 * the app will respond to.  Also, handles starting the widget updates.
 * 
 * @author Jeff
 */
public class TimerService extends Service {
    
    private long mSecs = 60000;
    private Timer timer;
    
    private ArrayList<Messenger> clients;
    
    @Override
    public void onCreate() {
        super.onCreate();
        clients = new ArrayList<Messenger>();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            if (intent.hasExtra("millis")) {
                mSecs = intent.getLongExtra("millis", 0);
            }
        }
        timer = new Timer(mSecs, 500);
        return Service.START_STICKY_COMPATIBILITY;
    }
    
    @Override
    public void onDestroy() {
        Log.i("BuzzTimer", "onDestroy");
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    public void startTimer() {
        if (timer != null)
            timer.start();
        else {
            timer = new Timer(mSecs, 500);
            timer.start();
        }
    }
    
    private final IBinder mBinder = new TimerBinder();
    
    public class TimerBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }
    
    public void addClient(Messenger messenger) {
        clients.add(messenger);
        Message mMessage = Message.obtain(null, 
                ActivityMain.TIMER_SET);
        int minute = (int)((mSecs-(mSecs%60000))/60000);
        int second = (int)((mSecs%60000)/1000);
        mMessage.arg1 = minute;
        mMessage.arg2 = second;
        try {
            messenger.send(mMessage);
        } catch (RemoteException e) {
            Log.e("BuzzTimer", "Can't connect to " +
                    "ActivityMain", e);
        }
    }
    
    public void removeClient(Messenger messenger) {
        clients.remove(messenger);
    }
    
    private class Timer extends CountDownTimer {
        public Timer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            int minute = (int)((millisInFuture-(millisInFuture%60000))/60000);
            int second = (int)((millisInFuture%60000)/1000);
            Log.d("BuzzTimer", "minute: " + minute);
            Log.d("BuzzTimer", "second: " + second);
            for (Messenger client : clients) {
                Message mMessage = Message.obtain(null, 
                        ActivityMain.TIMER_SET);
                mMessage.arg1 = minute;
                mMessage.arg2 = second;
                try {
                    client.send(mMessage);
                } catch (RemoteException e) {
                    Log.e("BuzzTimer", "Can't connect to " +
                            "ActivityMain", e);
                }
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
            int minute = (int)((millisUntilFinished-(millisUntilFinished%60000))/60000);
            int second = (int)((millisUntilFinished%60000)/1000);
            Log.i("BuzzTimer", "minute: " + minute);
            Log.i("BuzzTimer", "second: " + second);
            for (Messenger client : clients) {
                Message mMessage = Message.obtain(null, 
                        ActivityMain.TIMER_TICK);
                mMessage.arg1 = minute;
                mMessage.arg2 = second;
                try {
                    client.send(mMessage);
                } catch (RemoteException e) {
                    Log.e("BuzzTimer", "Can't connect to " +
                            "ActivityMain", e);
                }
            }
        }
        
        @Override
        public void onFinish() {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(2000);
            timer.start();
        }
    }

    public void setTime(int minute, int second) {
        mSecs = minute*60000 + second*1000;
        if (timer != null)
            timer.cancel();
        timer = new Timer(mSecs, 500);
        for (Messenger client : clients) {
            Message mMessage = Message.obtain(null, 
                    ActivityMain.TIMER_SET);
            mMessage.arg1 = minute;
            mMessage.arg2 = second;
            try {
                client.send(mMessage);
            } catch (RemoteException e) {
                Log.e("BuzzTimer", "Can't connect to " +
                        "ActivityMain", e);
            }
        }
    }

}

