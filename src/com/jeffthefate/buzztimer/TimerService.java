package com.jeffthefate.buzztimer;

import java.util.ArrayList;

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
    private boolean loop = true;
    private Timer timer;
    
    private ArrayList<Messenger> clients;
    
    @Override
    public void onCreate() {
        super.onCreate();
        clients = new ArrayList<Messenger>();
        Log.i("BuzzTimer", "onCreate");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i("BuzzTimer", "onStart");
        mSecs = ApplicationEx.dbHelper.getTime();
        Log.i("BuzzTimer", "onStartCommand setting mSecs to " + mSecs);
        if (timer != null)
            timer.cancel();
        timer = new Timer(mSecs, 500, loop);
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
        if (timer != null) {
            if (timer != null)
                timer.cancel();
            timer.start();
        }
        else {
            timer = new Timer(mSecs, 500, loop);
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
        Log.e("BuzzTimer", "what: " + mMessage.what);
        Log.e("BuzzTimer", "arg1: " + mMessage.arg1);
        Log.e("BuzzTimer", "arg2: " + mMessage.arg2);
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
        private boolean loop = false;
        
        public Timer(long millisInFuture, long countDownInterval,
                boolean loop) {
            super(millisInFuture, countDownInterval);
            this.loop = loop;
            ApplicationEx.dbHelper.setTime(millisInFuture);
            int minute = (int)((millisInFuture-(millisInFuture%60000))/60000);
            int second = (int)((millisInFuture%60000)/1000);
            for (Messenger client : clients) {
                Message mMessage = Message.obtain(null, 
                        ActivityMain.TIMER_SET);
                mMessage.arg1 = minute;
                mMessage.arg2 = second;
                Log.d("BuzzTimer", "what: " + mMessage.what);
                Log.d("BuzzTimer", "arg1: " + mMessage.arg1);
                Log.d("BuzzTimer", "arg2: " + mMessage.arg2);
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
            mSecs = millisUntilFinished;
            ApplicationEx.dbHelper.setTime(mSecs);
            int minute = (int)((mSecs-(mSecs%60000))/60000);
            int second = (int)((mSecs%60000)/1000);
            for (Messenger client : clients) {
                Message mMessage = Message.obtain(null, 
                        ActivityMain.TIMER_TICK);
                mMessage.arg1 = minute;
                mMessage.arg2 = second;
                Log.d("BuzzTimer", "what: " + mMessage.what);
                Log.d("BuzzTimer", "arg1: " + mMessage.arg1);
                Log.d("BuzzTimer", "arg2: " + mMessage.arg2);
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
            if (loop)
                timer.start();
        }
    }

    public void setTime(int minute, int second, boolean loop) {
        this.loop = loop;
        mSecs = minute*60000 + second*1000;
        Log.i("BuzzTimer", "setTime setting mSecs to " + mSecs);
        if (timer != null)
            timer.cancel();
        timer = new Timer(mSecs, 500, loop);
        for (Messenger client : clients) {
            Message mMessage = Message.obtain(null, 
                    ActivityMain.TIMER_SET);
            mMessage.arg1 = minute;
            mMessage.arg2 = second;
            Log.d("BuzzTimer", "what: " + mMessage.what);
            Log.d("BuzzTimer", "arg1: " + mMessage.arg1);
            Log.d("BuzzTimer", "arg2: " + mMessage.arg2);
            try {
                client.send(mMessage);
            } catch (RemoteException e) {
                Log.e("BuzzTimer", "Can't connect to " +
                        "ActivityMain", e);
            }
        }
    }

}