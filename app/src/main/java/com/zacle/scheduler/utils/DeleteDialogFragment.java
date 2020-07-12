package com.zacle.scheduler.utils;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.zacle.scheduler.R;
import com.zacle.scheduler.data.database.entity.Event;

import timber.log.Timber;

public class DeleteDialogFragment extends AppCompatDialogFragment {

    private DeleteDialogListener listener;
    private Event event;

    public interface DeleteDialogListener {
        void onYesClicked(Event event);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.delete_question))
                .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                })
                .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> listener.onYesClicked(event));
        return builder.create();
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (DeleteDialogListener) getTargetFragment();
        } catch (ClassCastException e){
            Timber.e("onAttach: ClassCastException : %s", e.getMessage() );
        }
    }
}
