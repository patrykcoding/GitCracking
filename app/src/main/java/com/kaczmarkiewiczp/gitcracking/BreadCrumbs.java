package com.kaczmarkiewiczp.gitcracking;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BreadCrumbs extends HorizontalScrollView implements View.OnClickListener {

    private LinearLayout childFrame;
    private Context context;
    private OnClickListener clickListener;

    public interface OnClickListener {
        void onBreadCrumbSelected(String path);
    }

    public BreadCrumbs(Context context) {
        super(context);
        init(context);
    }

    public BreadCrumbs(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    private void init(Context context) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(24, 8, 24, 8);

        childFrame = new LinearLayout(context);
        childFrame.setLayoutParams(layoutParams);
        this.context = context;
        addView(childFrame, layoutParams);
        addRootCrumb();
    }

    public void setCallback(OnClickListener listener) {
        clickListener = listener;
    }

    public void setPath(String path) {
        childFrame.removeAllViews();
        addRootCrumb();
        addArrow();

        StringBuilder leadingPath = new StringBuilder(path.length());
        String[] crumbs = path.split("/");
        for (int i = 0; i < crumbs.length; i++) {
            leadingPath.append(crumbs[i]);
            addCrumb(leadingPath.toString(), crumbs[i]);
            leadingPath.append("/");
            if (i < crumbs.length - 1) {
                addArrow();
            }
        }
    }

    private void addRootCrumb() {
        TextView textViewRoot = new TextView(context);
        textViewRoot.setText("/");
        textViewRoot.setTag(" ");
        textViewRoot.setPadding(0, 0, 8, 0);
        textViewRoot.setOnClickListener(this);
        childFrame.addView(textViewRoot);
    }

    private void addCrumb(String path, String crumbTitle) {
        TextView textViewCrumb = new TextView(context);
        textViewCrumb.setText(crumbTitle);
        textViewCrumb.setTag(path);
        textViewCrumb.setPadding(8, 0, 8, 0);
        textViewCrumb.setOnClickListener(this);
        childFrame.addView(textViewCrumb);
    }

    private void addArrow() {
        ImageView imageViewArrow = new ImageView(context);
        imageViewArrow.setImageResource(R.drawable.ic_navigate_next_black_24dp);
        childFrame.addView(imageViewArrow);
    }

    @Override
    public void onClick(View v) {
        String path = (String) v.getTag();
        clickListener.onBreadCrumbSelected(path);
    }
}
