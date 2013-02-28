package com.jeffthefate.buzztimer;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

/**
 * Used as a holder of many values and objects for the entire application.
 * 
 * @author Jeff Fate
 */
@SuppressLint("ShowToast")
public class ApplicationEx extends Application {
    /**
     * The application's context
     */
    private static Context app;
    public static DatabaseHelper dbHelper;
    private static int mSecs = -1;
    public static Toast mToast;
    public static ArrayList<String> devices = new ArrayList<String>();
    
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        mToast = Toast.makeText(app, "", Toast.LENGTH_LONG);
        dbHelper = DatabaseHelper.getInstance();
        dbHelper.checkUpgrade();
        mSecs = ApplicationEx.dbHelper.getTime() < 0 ? 60000 :
        		ApplicationEx.dbHelper.getTime();
        devices.add("HT0ATHJ09671");
        devices.add("0146914813011017");
        devices.add("015d172c9a440412");
    }
    /**
     * Used by other classes to get the application's global context.
     * @return  the context of the application
     */
    public static Context getApp() {
        return app;
    }
    /**
     * Get the time that the timer is set to right now.
     * @return	current time value for the application
     */
    public static int getMsecs() {
        return mSecs;
    }
    /**
     * Set the time that the timer uses to count down.
     * @param milliSecs	new time value for the application
     */
    public static void setMsecs(int milliSecs) {
        mSecs = milliSecs;
    }
    
}