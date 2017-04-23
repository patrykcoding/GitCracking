package com.kaczmarkiewiczp.gitcracking;

import android.content.Context;
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
    private int numberOfCrumbs;
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
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
        numberOfCrumbs = 0;
        addRootCrumb();

        if (path.trim().isEmpty()) {
            return;
        }
        
        StringBuilder leadingPath = new StringBuilder(path.length());
        String[] crumbs = path.split("/");
        for (String crumb : crumbs){
            leadingPath.append(crumb);
            addArrow();
            addCrumb(leadingPath.toString(), crumb);
            leadingPath.append("/");
        }
    }

    private void addRootCrumb() {
        ImageView imageViewRoot = new ImageView(context);
        imageViewRoot.setImageResource(R.drawable.ic_home_black_24dp);
        imageViewRoot.setTag(" ");
        imageViewRoot.setOnClickListener(this);
        childFrame.addView(imageViewRoot);
    }

    private void addCrumb(String path, String crumbTitle) {
        numberOfCrumbs++;
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
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        View child = childFrame.getChildAt(numberOfCrumbs);
        smoothScrollTo(child.getLeft(), 0);
    }

    @Override
    public void onClick(View v) {
        String path = (String) v.getTag();
        clickListener.onBreadCrumbSelected(path);
    }
}
