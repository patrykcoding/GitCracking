package com.kaczmarkiewiczp.gitcracking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class CreateMilestoneDialog extends DialogFragment {

    private String milestoneTitle;
    private String milestoneDescription;
    private Date milestoneDueDate;
    private EditText editTextTitle;
    private EditText editTextDescription;
    private TextView textViewDueDate;
    private ImageView imageViewClear;
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

        editTextTitle = (EditText) view.findViewById(R.id.et_milestone_title);
        editTextDescription = (EditText) view.findViewById(R.id.et_milestone_description);
        imageViewClear = (ImageView) view.findViewById(R.id.iv_clear_due_date);
        textViewDueDate = (TextView) view.findViewById(R.id.tv_milestone_due_date);

        imageViewClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDueDate();
            }
        });

        Calendar now = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        dueDateSelected(year, monthOfYear, dayOfMonth);
                    }
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        textViewDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show(getFragmentManager(), "Datepicker");
            }
        });

        
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
            Toast.makeText(getActivity(), "Title cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }
        milestoneTitle = title;
        milestoneDescription = description;
        listener.onSaveMilestone(this);
    }

    private void dueDateSelected(int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        milestoneDueDate = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        String formatted = simpleDateFormat.format(calendar.getTime());
        textViewDueDate.setText(formatted);
        imageViewClear.setVisibility(View.VISIBLE);
    }

    private void clearDueDate() {
        imageViewClear.setVisibility(View.GONE);
        textViewDueDate.setText(R.string.due_date_optional);
        milestoneDueDate = null;
    }

    public String getMilestoneTitle() {
        return this.milestoneTitle;
    }

    public String getMilestoneDescription() {
        return  this.milestoneDescription;
    }

    public boolean isMilestoneDueDateSet() {
        return !(milestoneDueDate == null);
    }

    public Date getMilestoneDueDate() {
        return milestoneDueDate;
    }

}
