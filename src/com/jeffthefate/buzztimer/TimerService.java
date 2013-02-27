package com.jeffthefate.buzztimer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

/**
 * Service that runs in the background.  Runs the timer and updates the UI as
 * the timer progresses.
 * 
 * @author Jeff Fate
 */
public class TimerService extends Service {
    
    private boolean loop = true;
    private boolean isRunning = false;
    private Timer timer;
    private Notification notification;
    private NotificationCompat.Builder nBuilder;
    private Resources res;
    
    private String title = "";
    private String ticker;
    
    private UiCallback uiCallback;
    private CancelReceiver cancelReceiver;
    
    private final IBinder mBinder = new TimerBinder();
    
    /**
     * Callback to allow the service to update the UI, which is on a different
     * thread.
     * 
     * @author Jeff Fate
     *
     */
    public interface UiCallback {
    	/**
    	 * Apply the timer time to the UI.
    	 * 
    	 * @param mSecs		timer time in milliseconds
    	 * @param setLocal	if true, update the UI's values for the timer time
    	 */
        public void updateTime(int mSecs, boolean setLocal);
    }
    
    /**
     * Receives broadcasts to cancel the timer - from the notification.
     * 
     * @author Jeff Fate
     *
     */
    public class CancelReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constants.ACTION_STOP_TIMER)) {
				stopTimer();
				if (uiCallback != null)
					uiCallback.updateTime(ApplicationEx.getMsecs(), false);
			}
		}
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        res = ApplicationEx.getApp().getResources();
        cancelReceiver = new CancelReceiver();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (ApplicationEx.getMsecs() < 0)
        	ApplicationEx.setMsecs(ApplicationEx.dbHelper.getTime());
        if (timer != null)
            timer.cancel();
        timer = new Timer(ApplicationEx.getMsecs(), 100, loop);
        isRunning = false;
        registerReceiver(cancelReceiver,
        		new IntentFilter(Constants.ACTION_STOP_TIMER));
        return Service.START_STICKY_COMPATIBILITY;
    }
    
    @Override
    public void onDestroy() {
    	unregisterReceiver(cancelReceiver);
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    /**
     * Set the current callback that receives UI updates.
     * 
     * @param uiCallback	object that has implemented
     * 						{@link com.jeffthefate.buzztimer.TimerService.UICallback}
     */
    public void setUiCallback(UiCallback uiCallback) {
        this.uiCallback = uiCallback;
    }
    
    /**
     * Start the timer with the current set time value.  This shows the
     * notification and starts updating the UI while the timer counts down.
     */
    public void startTimer() {
        isRunning = true;
        if (timer != null) {
            timer.cancel();
            timer.start();
        }
        else {
            timer = new Timer(ApplicationEx.getMsecs(), 100, loop);
            timer.start();
        }
        makeText();
        showNotification(true);
    }
    
    /**
     * Create the string for the notification title.  Uses the ticker value
     * and adds "Repeating" if the timer is repeating.
     */
    private void makeText() {
    	makeTicker(ApplicationEx.getMsecs());
        if (loop)
        	title = ticker + " Repeating";
        else
        	title = ticker;
    }
    
    /**
     * Create the ticker value that show the timer time.
     * 
     * @param milliseconds	time to display in milliseconds
     */
    private void makeTicker(int milliseconds) {
    	int mins = (int)((milliseconds-(milliseconds%60000))/60000);
        int secs = (int)((milliseconds%60000)/1000);
        ticker = Integer.toString(mins) + ":" + (secs >= 10 ?
        		Integer.toString(secs) : "0" + secs);
    }
    
    /**
     * Create and start the ongoing notification while the timer is working.
     * 
     * @param showTicker	if true, show the ticker text
     */
    private void showNotification(boolean showTicker) {
        Intent notificationIntent = new Intent(ApplicationEx.getApp(),
        		ActivityMain.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
        		Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
        		ApplicationEx.getApp(), 0, notificationIntent,
        		Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        nBuilder = new NotificationCompat.Builder(ApplicationEx.getApp());
        nBuilder.setLargeIcon(BitmapFactory.decodeResource(res,
        		R.drawable.ic_launcher)).
    		setSmallIcon(R.drawable.ic_stat_notification).
    		setWhen(System.currentTimeMillis()).
    		setContentTitle(title).
    		setContentText("Buzz Timer").
    		setContentIntent(pendingIntent).
    		addAction(R.drawable.ic_notification_cancel, "Cancel",
    				PendingIntent.getBroadcast(ApplicationEx.getApp(), 0,
    						new Intent(Constants.ACTION_STOP_TIMER), 0));
        if (showTicker)
        	nBuilder.setTicker(ticker);
        notification = nBuilder.build();
        startForeground(Constants.NOTIFICATION_RUNNING, notification);
    }
    
    /**
     * Stop count down of the timer and remove the notification.
     */
    public void stopTimer() {
        if (timer != null)
            timer.cancel();
        isRunning = false;
        stopForeground(true);
    }
    
    /**
     * @return if the timer is currently running
     */
    public boolean isTimerRunning() {
        return isRunning;
    }
    
    /**
     * The binder to return to any clients that connect to this service.
     */
    public class TimerBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }
    
    /**
     * Set the current time for the timer and whether it should repeat.
     * 
     * @param loop	if true, repeat the timer indefinitely
     */
    public void setTime(boolean loop) {
        this.loop = loop;
        if (timer != null)
            timer.cancel();
        timer = new Timer(ApplicationEx.getMsecs(), 100, loop);
        isRunning = false;
    }
    
    /**
     * Custom timer that updates the time in the UI and vibrates when done.
     * Will start again if the timer is set to repeat indefinitely.  Shows
     * notification when it starts and dismisses it when it ends.
     */
    private class Timer extends CountDownTimer {
        private boolean loop = false;
        
        public Timer(long millisInFuture, long countDownInterval,
                boolean loop) {
            super(millisInFuture, countDownInterval);
            this.loop = loop;
            if (uiCallback != null)
                uiCallback.updateTime((int)millisInFuture, false);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (uiCallback != null)
                uiCallback.updateTime((int)millisUntilFinished, false);
            makeText();
        }
        
        @Override
        public void onFinish() {
            if (uiCallback != null)
                uiCallback.updateTime((int)ApplicationEx.getMsecs(), false);
            Vibrator v = (Vibrator) ApplicationEx.getApp().getSystemService(
                    Context.VIBRATOR_SERVICE);
            v.vibrate(new long[] {0, 500, 500, 1000}, -1);
            if (loop) {
            	showNotification(true);
                this.start();
            }
            else
            	stopForeground(true);
        }
    }
}