package com.kaczmarkiewiczp.gitcracking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class CreateMilestoneDialog extends DialogFragment {

    public String milestoneTitle;
    public String milestoneDescription;
    private EditText editTextTitle;
    private EditText editTextDescription;
    private SaveMilestoneListener listener;

    public interface SaveMilestoneListener {
        public void onSaveMilestone(CreateMilestoneDialog dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme);
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.create_milestone_dialog, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        //builder.setView(inflater.inflate(R.layout.create_milestone_dialog, null));

        editTextTitle = (EditText) view.findViewById(R.id.et_milestone_title);
        editTextDescription = (EditText) view.findViewById(R.id.et_milestone_description);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle("Create new Milestone");
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        toolbar.inflateMenu(R.menu.save);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_save) {
                    saveButtonPressed();
                    return true;
                }
                return false;
            }
        });

        return alertDialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (SaveMilestoneListener) context;
    }

    private void saveButtonPressed() {
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();

        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Title cannot be empty", Toast.LENGTH_LONG).show();
        }
        milestoneTitle = title;
        milestoneDescription = description;
        listener.onSaveMilestone(this);
    }

    public String getMilestoneTitle() {
        return this.milestoneTitle;
    }

    public String getMilestoneDescription() {
        return  this.milestoneDescription;
    }

}
