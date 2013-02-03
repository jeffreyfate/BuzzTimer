package com.jeffthefate.buzztimer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TimePicker;

public class FragmentTimePickerDialog extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    
    private TimeSetListener mCallback;
    
    public interface TimeSetListener {
        public void onTimeSet(int minute, int second);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new TimerPickerDialog(getActivity(), this, 1, 0,
                DateFormat.is24HourFormat(getActivity()));
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (TimeSetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnButtonListener");
        }
    }

    public void onTimeSet(TimePicker view, int minute, int second) {
        mCallback.onTimeSet(minute, second);
    }
    
}