package com.jeffthefate.buzztimer;

import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.widget.TimePicker;

public class TimerPickerDialog extends TimePickerDialog {

    public TimerPickerDialog(Context context, int theme,
            OnTimeSetListener callBack, int hourOfDay, int minute,
            boolean is24HourView) {
        super(context, theme, callBack, hourOfDay, minute, is24HourView);
    }
    
    public TimerPickerDialog(Context context,
            OnTimeSetListener callBack, int hourOfDay, int minute,
            boolean is24HourView) {
        super(context, callBack, hourOfDay, minute, is24HourView);
    }
    
    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        super.onTimeChanged(view, hourOfDay, minute);
        Log.i("BuzzTimer", "hourOfDay: " + hourOfDay);
        Log.i("BuzzTimer", "minute: " + minute);
    }
    
    @Override
    public void updateTime(int hourOfDay, int minute) {
        super.updateTime(hourOfDay, minute);
        Log.i("BuzzTimer", "hourOfDay: " + hourOfDay);
        Log.i("BuzzTimer", "minute: " + minute);
    }

}
