package com.kaczmarkiewiczp.gitcracking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;

import com.pddstudio.highlightjs.HighlightJsView;
import com.pddstudio.highlightjs.models.Language;
import com.pddstudio.highlightjs.models.Theme;

/*
 * Dialog for showing files
 */
public class FileViewerDialog extends DialogFragment {

    private HighlightJsView highlightJsViewCode;
    private String fileContent;
    private String toolbarTitle;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme);
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.file_viewer_dialog, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        highlightJsViewCode = (HighlightJsView) view.findViewById(R.id.hjv_code);
        highlightJsViewCode.setTheme(Theme.ANDROID_STUDIO);
        highlightJsViewCode.setHighlightLanguage(Language.AUTO_DETECT);
        highlightJsViewCode.setShowLineNumbers(true);
        if (fileContent != null) {
            highlightJsViewCode.setSource(fileContent);
        }

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (toolbarTitle != null) {
            toolbar.setTitle(toolbarTitle);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        return alertDialog;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public void setToolbarTitle(String title) {
        toolbarTitle = title;
    }
}
