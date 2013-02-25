package com.jeffthefate.buzztimer;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
    private static boolean mIsActive = false;
    private static int mSecs;
    public static Toast mToast;
    private static SharedPreferences sharedPrefs;
    
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        mToast = Toast.makeText(app, "", Toast.LENGTH_LONG);
        dbHelper = DatabaseHelper.getInstance();
        dbHelper.checkUpgrade();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
                ApplicationEx.getApp());
        mSecs = sharedPrefs.getInt(app.getString(R.string.msec_key), 60000);
    }
    /**
     * Used by other classes to get the application's global context.
     * @return  the context of the application
     */
    public static Context getApp() {
        return app;
    }
    
    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib"))
                    deleteDir(new File(appDir, s));
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success)
                    return false;
            }
        }
        return dir.delete();
    }
    
    public static boolean isActive() {
        return mIsActive;
    }
    
    public static void setActive() {
        mIsActive = true;
    }
    
    public static void setInactive() {
        mIsActive = false;
    }
    
    public static int getMsecs() {
        return mSecs;
    }
    
    public static void setMsecs(int milliSecs) {
        mSecs = milliSecs;
    }
    
}