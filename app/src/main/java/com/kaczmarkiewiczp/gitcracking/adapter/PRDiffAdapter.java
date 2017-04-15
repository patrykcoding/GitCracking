package com.kaczmarkiewiczp.gitcracking.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kaczmarkiewiczp.gitcracking.R;

import java.util.ArrayList;

public class PRDiffAdapter extends RecyclerView.Adapter<PRDiffAdapter.ViewHolder> {

    private ArrayList<String> files;
    private ArrayList<String> diffs;
    private Context context;

    public PRDiffAdapter() {
        files = new ArrayList<>();
        diffs = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        int layoutIdForListItem = R.layout.diff_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    @SuppressWarnings("deprecation") // for getColor -- check in code for android version
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String filename = files.get(position);
        String diff = diffs.get(position);

        holder.textViewDiffFile.setText(filename);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        String[] diffLines = diff.split("\\r?\\n");
        for (String line : diffLines) {
            TextView textView = new TextView(context);
            textView.setText(line);
            textView.setLayoutParams(layoutParams);
            textView.setPadding(0, 0, 0, 4);

            if (line.startsWith("+")) {
                int color;
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    color = context.getColor(R.color.diff_addition);
                } else {
                    color = context.getResources().getColor(R.color.diff_addition);
                }
                textView.setBackgroundColor(color);
            } else if (line.startsWith("-")) {
                int color;
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    color = context.getColor(R.color.diff_deletion);
                } else {
                    color = context.getResources().getColor(R.color.diff_deletion);
                }
                textView.setBackgroundColor(color);
            } else if (line.startsWith("@@")) {
                int color;
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    color = context.getColor(R.color.diff_hunk_range);
                } else {
                    color = context.getResources().getColor(R.color.diff_hunk_range);
                }
                textView.setBackgroundColor(color);
                textView.setTextColor(Color.WHITE);
            }
            holder.linearLayoutDiffLines.addView(textView);
        }
    }

    @Override
    public int getItemCount() {
        if (files == null) {
            return 0;
        }
        return files.size();
    }

    public void addFileDiff(String filename, String diff) {
        files.add(filename);
        diffs.add(diff);
        notifyItemInserted(files.size() - 1);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView textViewDiffFile;
        public final LinearLayout linearLayoutDiffLines;

        public ViewHolder(View view) {
            super(view);
            textViewDiffFile = (TextView) view.findViewById(R.id.tv_diff_file);
            linearLayoutDiffLines = (LinearLayout) view.findViewById(R.id.ll_diff_lines);
        }
    }
}
