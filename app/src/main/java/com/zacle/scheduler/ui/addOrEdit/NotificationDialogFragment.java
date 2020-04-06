package com.zacle.scheduler.ui.addOrEdit;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.zacle.scheduler.R;
import com.zacle.scheduler.utils.AppConstants;

public class NotificationDialogFragment extends DialogFragment {

    private static final String TAG = "NotificationDialogFragm";
    private static final String NOTIFY = "com.zacle.scheduler.ui.addOrEdit.notification";
    public static final String NOTIFY_SETTINGS = "com.zacle.scheduler.ui.addOrEdit.notification_time";

    private TextInputLayout time;
    private RadioGroup time_settings;
    private RadioButton minutes;
    private RadioButton hours;
    private TextView ok_button;
    private TextView cancel_button;

    private int notification_time;
    private String notification_settings;

    public static NotificationDialogFragment newInstance(int notification_time, String notification_settings) {
        NotificationDialogFragment dialogFragment = new NotificationDialogFragment();
        Bundle args = new Bundle();
        args.putInt(NOTIFY, notification_time);
        args.putString(NOTIFY_SETTINGS, notification_settings);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    public interface OnInputSelected{
        void sendInput(String time, String time_settings);
    }

    public OnInputSelected onInputSelected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_dialog, container, false);

        setUp(view);

        settings(notification_time, notification_settings);

        cancel_button.setOnClickListener(v -> getDialog().dismiss());

        ok_button.setOnClickListener(v -> {
            int id = time_settings.getCheckedRadioButtonId();
            String value = time.getEditText().getText().toString();
            if (id == minutes.getId())
                onInputSelected.sendInput(value, AppConstants.MINUTES);
            else
                onInputSelected.sendInput(value, AppConstants.HOURS);

            getDialog().dismiss();
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            notification_time = args.getInt(NOTIFY);
            notification_settings = args.getString(NOTIFY_SETTINGS);
        }
    }

    private void setUp(View view) {
        time = view.findViewById(R.id.notification_number);
        time_settings = view.findViewById(R.id.time_settings);
        minutes = view.findViewById(R.id.notification_minutes);
        hours = view.findViewById(R.id.notification_hours);
        ok_button = view.findViewById(R.id.event_action_ok);
        cancel_button = view.findViewById(R.id.event_action_cancel);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            onInputSelected = (OnInputSelected) getTargetFragment();
        } catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException : " + e.getMessage() );
        }
    }

    public void settings(int number, String setting) {
        time.getEditText().setText("" + number);
        Log.d(TAG, "settings: " + setting);

        if (setting.equals(AppConstants.MINUTES)) {
            time_settings.check(R.id.notification_minutes);
        } else {
            time_settings.check(R.id.notification_hours);
        }
    }
}
