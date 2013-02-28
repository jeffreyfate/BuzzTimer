package com.jeffthefate.buzztimer;


/**
 * Keep track of global constants.
 * 
 * @author Jeff Fate
 */
public class Constants {
    
	/**
	 * Text that appears for the log tag for this application.
	 */
    public static final String LOG_TAG = "Buzz Timer";
    /**
     * Value for the notification created by the timer service.
     */
    public static final int NOTIFICATION_RUNNING = 3641;
    /**
     * Broadcast to stop the timer from the notification.
     */
    public static final String ACTION_STOP_TIMER =
    		"com.jeffthefate.buzztimer.ACTION_STOP_TIMER";
    
}
