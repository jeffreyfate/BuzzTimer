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
    
    private boolean loop = true;
    private boolean isRunning = false;
    private Timer timer;
    
    public interface UiCallback {
        public void updateTime(int mSecs);
    }
    
    private UiCallback uiCallback;
    
    @Override
    public void onCreate() {
        super.onCreate();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        ApplicationEx.setMsecs(ApplicationEx.dbHelper.getTime());
        if (timer != null)
            timer.cancel();
        timer = new Timer(ApplicationEx.getMsecs(), 100, loop);
        isRunning = false;
        return Service.START_STICKY_COMPATIBILITY;
    }
    
    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    public void setUiCallback(UiCallback uiCallback) {
        this.uiCallback = uiCallback;
    }
    
    public void startTimer() {
        Log.i(Constants.LOG_TAG, "TimerService mSecs: " + ApplicationEx.getMsecs());
        isRunning = true;
        if (timer != null) {
            timer.cancel();
            timer.start();
        }
        else {
            timer = new Timer(ApplicationEx.getMsecs(), 100, loop);
            timer.start();
        }
    }
    
    public void stopTimer() {
        if (timer != null)
            timer.cancel();
        isRunning = false;
    }
    
    public boolean isTimerRunning() {
        return isRunning;
    }
    
    private final IBinder mBinder = new TimerBinder();
    
    public class TimerBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }
    
    public void setTime(boolean loop) {
        this.loop = loop;
        ApplicationEx.dbHelper.setTime(ApplicationEx.getMsecs());
        if (timer != null)
            timer.cancel();
        timer = new Timer(ApplicationEx.getMsecs(), 100, loop);
        isRunning = false;
    }
    
    private class Timer extends CountDownTimer {
        private boolean loop = false;
        
        public Timer(long millisInFuture, long countDownInterval,
                boolean loop) {
            super(millisInFuture, countDownInterval);
            this.loop = loop;
            ApplicationEx.dbHelper.setTime(millisInFuture);
            if (uiCallback != null)
                uiCallback.updateTime((int)millisInFuture);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            ApplicationEx.dbHelper.setTime(millisUntilFinished);
            if (uiCallback != null)
                uiCallback.updateTime((int)millisUntilFinished);
        }
        
        @Override
        public void onFinish() {
            ApplicationEx.dbHelper.setTime(ApplicationEx.getMsecs());
            if (uiCallback != null)
                uiCallback.updateTime((int)ApplicationEx.getMsecs());
            Vibrator v = (Vibrator) ApplicationEx.getApp().getSystemService(
                    Context.VIBRATOR_SERVICE);
            v.vibrate(new long[] {500, 1000, 1000, 2000, 2000, 3000}, 2);
            if (loop)
                this.start();
        }
    }

}