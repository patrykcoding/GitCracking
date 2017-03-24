package com.kaczmarkiewiczp.gitcracking.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.kaczmarkiewiczp.gitcracking.R;
import com.squareup.picasso.Picasso;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class IssuesAdapter extends RecyclerView.Adapter<IssuesAdapter.ViewHolder> {

    private ArrayList<Issue> issues;
    private Context context;

    public IssuesAdapter() {
        issues = new ArrayList<>();
    }

    @Override
    public IssuesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        int layoutIdForListItem = R.layout.issues_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String url = issues.get(position).getUrl();
        int repositoryNameStartIndex = 29;
        String repository = url.substring(repositoryNameStartIndex, url.indexOf("/issues/"));

        String issueNumber = String.valueOf(issues.get(position).getNumber());
        issueNumber = "#" + issueNumber;

        String title = issues.get(position).getTitle();
        String userIconUrl = issues.get(position).getUser().getAvatarUrl();
        String username = issues.get(position).getUser().getLogin();

        Date date = issues.get(position).getCreatedAt();
        PrettyTime prettyTime = new PrettyTime();
        int commentsCount = issues.get(position).getComments();

        holder.textViewRepository.setText(repository);
        holder.textViewIssueNumber.setText(issueNumber);
        holder.textViewTitle.setText(title);
        Picasso.with(context).load(userIconUrl).into(holder.imageViewUserIcon);
        holder.textViewUser.setText(username);
        holder.textViewDate.setText(prettyTime.format(date));
        if (commentsCount > 0) {
            holder.linearLayoutComments.setVisibility(View.VISIBLE);
            holder.textViewCommentsCount.setText(String.valueOf(commentsCount));
        } else {
            holder.linearLayoutComments.setVisibility(View.GONE);
        }

        List<Label> labels = issues.get(position).getLabels();
        if (labels != null && labels.size() > 0) {
            if (holder.flexBoxLayoutTags.getChildCount() > 0) {
                holder.flexBoxLayoutTags.removeAllViews();
            }
            holder.flexBoxLayoutTags.setVisibility(View.VISIBLE);
            for (Label label : labels) {
                String colorString = label.getColor();
                int rgb = Color.parseColor("#" + colorString);
                int r = Color.red(rgb);
                int g = Color.green(rgb);
                int b = Color.blue(rgb);
                double a = 1 - (0.299 * r + 0.587 * g + 0.114 * b) / 255;

                TextView textViewLabel = new TextView(context);
                textViewLabel.setText(label.getName());
                textViewLabel.setBackgroundColor(Color.parseColor("#" + colorString));
                if (a < 0.5) {
                    textViewLabel.setTextColor(Color.BLACK);
                } else {
                    textViewLabel.setTextColor(Color.WHITE);
                }
                textViewLabel.setElevation(4);
                textViewLabel.setPadding(8, 4, 8, 4);
                FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 0, 12, 12);
                textViewLabel.setLayoutParams(layoutParams);
                holder.flexBoxLayoutTags.addView(textViewLabel);
            }
        } else {
            if (holder.flexBoxLayoutTags.getChildCount() > 0) {
                holder.flexBoxLayoutTags.removeAllViews();
            }
            holder.flexBoxLayoutTags.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (issues == null) {
            return 0;
        }
        return issues.size();
    }

    public void setIssues(ArrayList<Issue> issues) {
        this.issues.addAll(issues);
        notifyDataSetChanged();
    }

    public void clearView() {
        this.issues.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView textViewRepository;
        public final TextView textViewIssueNumber;
        public final TextView textViewTitle;
        public final ImageView imageViewUserIcon;
        public final TextView textViewUser;
        public final TextView textViewDate;
        public final LinearLayout linearLayoutComments;
        public final TextView textViewCommentsCount;
        public final FlexboxLayout flexBoxLayoutTags;

        public ViewHolder(View view) {
            super(view);
            textViewRepository = (TextView) view.findViewById(R.id.tv_issues_repo);
            textViewIssueNumber = (TextView) view.findViewById(R.id.tv_issue_number);
            textViewTitle = (TextView) view.findViewById(R.id.tv_issues_title);
            imageViewUserIcon = (ImageView) view.findViewById(R.id.iv_user_icon);
            textViewUser = (TextView) view.findViewById(R.id.tv_user);
            textViewDate = (TextView) view.findViewById(R.id.tv_date);
            linearLayoutComments = (LinearLayout) view.findViewById(R.id.ll_comments);
            textViewCommentsCount = (TextView) view.findViewById(R.id.tv_comments_count);
            flexBoxLayoutTags = (FlexboxLayout) view.findViewById(R.id.fl_tags);
        }
    }
}
