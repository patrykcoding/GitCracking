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


public class EditDialog extends DialogFragment {

    private EditText editTextTitle;
    private EditText editTextDescription;
    private Toolbar toolbar;
    private String title;
    private String description;
    private String titleHint;
    private String descriptionHint;
    private String toolbarTitle;
    private EditListener listener;

    public interface EditListener {
        void onSaveEdit(EditDialog editDialog, String title, String description);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme);
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.edit_dialog, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        editTextTitle = (EditText) view.findViewById(R.id.et_title);
        editTextDescription = (EditText) view.findViewById(R.id.et_description);

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
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
        setLabels();
        return alertDialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getTargetFragment() != null) {
            listener = (EditListener) getTargetFragment();
        } else {
            listener = (EditListener) context;
        }
    }

    private void setLabels() {
        if (toolbarTitle != null && !toolbarTitle.isEmpty()) {
            toolbar.setTitle(toolbarTitle);
        }
        if (title != null && !title.isEmpty()) {
            editTextTitle.setText(title);
        }
        if (titleHint != null && !titleHint.isEmpty()) {
            editTextTitle.setHint(titleHint);
        }
        if (description != null && !description.isEmpty()) {
            editTextDescription.setText(description);
        }
        if (descriptionHint != null && !descriptionHint.isEmpty()) {
            editTextDescription.setHint(descriptionHint);
        }
    }

    private void saveButtonPressed() {
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();

        if (title.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.title_cannot_be_empty), Toast.LENGTH_LONG).show();
            return;
        }
        listener.onSaveEdit(this, title, description);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitleHint(String titleHint) {
        this.titleHint = titleHint;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDescriptionHint(String descriptionHint) {
        this.descriptionHint = descriptionHint;
    }

    public void setToolbarTitle(String toolbarTitle) {
        this.toolbarTitle = toolbarTitle;
    }
}
