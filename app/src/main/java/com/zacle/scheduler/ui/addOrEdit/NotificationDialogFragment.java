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

public class NotificationDialogFragment extends DialogFragment {

    private static final String TAG = "NotificationDialogFragm";
    public final String MINUTES = "minutes";

    private TextInputLayout time;
    private RadioGroup time_settings;
    private RadioButton minutes;
    private RadioButton hours;
    private TextView ok_button;
    private TextView cancel_button;

    public interface OnInputSelected{
        void sendInput(String time, String time_settings);
    }

    public OnInputSelected onInputSelected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_dialog, container, false);

        setUp(view);

        time.getEditText().setText("5");

        cancel_button.setOnClickListener(v -> getDialog().dismiss());

        ok_button.setOnClickListener(v -> {
            int id = time_settings.getCheckedRadioButtonId();
            String value = time.getEditText().getText().toString();
            if (id == minutes.getId())
                onInputSelected.sendInput(value, MINUTES);
            else
                onInputSelected.sendInput(value, "hours");

            getDialog().dismiss();
        });

        return view;
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
}
