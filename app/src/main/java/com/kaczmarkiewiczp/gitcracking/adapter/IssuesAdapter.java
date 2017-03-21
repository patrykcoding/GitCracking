package com.kaczmarkiewiczp.gitcracking.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kaczmarkiewiczp.gitcracking.R;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class IssuesAdapter extends RecyclerView.Adapter<IssuesAdapter.ViewHolder> {

    private ArrayList<Issue> issues;
    private Context context;

    public IssuesAdapter(Context context) {
        issues = new ArrayList<>();
        this.context = context;
    }

    @Override
    public IssuesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

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
        Date now = new Date();
        CharSequence relativeDate = DateUtils.getRelativeTimeSpanString(date.getTime(), now.getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);
        String createdAt = relativeDate.toString();

        int commentsCount = issues.get(position).getComments();

        holder.textViewRepository.setText(repository);
        holder.textViewIssueNumber.setText(issueNumber);
        holder.textViewTitle.setText(title);
        // TODO set icon
        holder.textViewUser.setText(username);
        if (commentsCount > 0) {
            holder.linearLayoutComments.setVisibility(View.VISIBLE);
            holder.textViewCommentsCount.setText(String.valueOf(commentsCount));
        } else {
            holder.linearLayoutComments.setVisibility(View.GONE);
        }
        /*
        List<Label> labels = issues.get(position).getLabels();
        if (labels != null && labels.size() > 0) {
            holder.linearLayoutTags.setVisibility(View.VISIBLE);
            for (Label label : labels) {
                TextView textViewLabel = new TextView(context);
                textViewLabel.setText(label.getName());
                textViewLabel.setBackgroundColor(Color.parseColor(label.getColor()));
                textViewLabel.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
        } else {
            if (holder.linearLayoutTags.getChildCount() > 0) {
                holder.linearLayoutTags.removeAllViews();
            }
            holder.linearLayoutTags.setVisibility(View.GONE);
        }*/
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
    }

    public void clearView() {
        this.issues.clear();
    }

    public void updateView() {
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
        public final LinearLayout linearLayoutTags;

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
            linearLayoutTags = (LinearLayout) view.findViewById(R.id.ll_tags);
        }
    }
}
