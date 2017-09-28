package com.kaczmarkiewiczp.gitcracking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

/*
 * Custom dialog for creating labels
 */
public class CreateLabelDialog extends DialogFragment {

    private EditText editTextLabelName;
    private LinearLayout linearLayoutLabelColor;
    private TextView textViewLabelColor;
    private ImageView imageViewLabelColor;
    private String labelName;
    private String labelHexColor;
    private labelCreationListener listener;

    public interface labelCreationListener {
        void onSaveLabel(CreateLabelDialog dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme);
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.create_label_dialog, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        final android.support.v7.app.AlertDialog colorPicker = ColorPickerDialogBuilder
                .with(getActivity())
                .setTitle(getString(R.string.pick_color))
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .density(8)
                .showColorEdit(true)
                .setColorEditTextColor(0xff000000)
                .showAlphaSlider(false)
                .showLightnessSlider(false)
                .setPositiveButton(getString(R.string.set), new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, Integer[] integers) {
                        colorSelected(i);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .build();

        editTextLabelName = (EditText) view.findViewById(R.id.et_label_name);
        textViewLabelColor = (TextView) view.findViewById(R.id.tv_label_color);
        imageViewLabelColor = (ImageView) view.findViewById(R.id.iv_label_color);
        linearLayoutLabelColor = (LinearLayout) view.findViewById(R.id.ll_label_color);
        linearLayoutLabelColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPicker.show();
            }
        });


        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.create_new_label));
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
        if (getTargetFragment() != null) {
            listener = (labelCreationListener) getTargetFragment();
        } else {
            listener = (labelCreationListener) context;
        }
    }

    private void colorSelected(int color) {
        String hexColor = Integer.toHexString(color).substring(2);
        String formattedHexColor = "#" + hexColor.toUpperCase();

        textViewLabelColor.setText(formattedHexColor);
        GradientDrawable gradientDrawable = (GradientDrawable) imageViewLabelColor.getBackground().mutate();
        gradientDrawable.setColor(color);
        gradientDrawable.invalidateSelf();

        labelHexColor = hexColor;
    }

    private void saveButtonPressed() {
        labelName = editTextLabelName.getText().toString();

        if (labelName.isEmpty()) {
            Toast.makeText(getActivity(), getString(R.string.label_name_cannot_be_empty), Toast.LENGTH_LONG).show();
            return;
        }
        listener.onSaveLabel(this);
    }

    public String getLabelName() {
        return this.labelName;
    }

    public String getLabelHexColor() {
        return this.labelHexColor;
    }
}
