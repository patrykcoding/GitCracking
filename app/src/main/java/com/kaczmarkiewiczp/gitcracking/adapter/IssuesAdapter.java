package com.kaczmarkiewiczp.gitcracking.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import com.kaczmarkiewiczp.gitcracking.R;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Repository;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class IssuesAdapter extends RecyclerView.Adapter<IssuesAdapter.ViewHolder> {

    private ArrayList<Issue> issues;
    private ArrayList<Repository> repositories;
    private Context context;
    private final IssueClickListener onClickListener;

    public interface IssueClickListener {
        void onIssueClick(Issue clickedIssue, Repository issueRepository);
    }

    public IssuesAdapter(IssueClickListener listener) {
        issues = new ArrayList<>();
        repositories = new ArrayList<>();
        onClickListener = listener;
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
        Repository repo = repositories.get(position);
        String repositoryName = repo.getName();
        String repositoryOwner = repo.getOwner().getLogin();
        String repository = repositoryOwner + "/" + repositoryName;
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
        Glide
                .with(context)
                .load(userIconUrl)
                .error(context.getDrawable(android.R.drawable.sym_def_app_icon))
                .placeholder(R.drawable.progress_animation)
                .crossFade()
                .into(holder.imageViewUserIcon);
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

    public void addIssue(Issue issue, Repository repository) {
        issues.add(issue);
        repositories.add(repository);
        notifyItemInserted(issues.size() - 1);
    }

    public void clearIssues() {
        int size = issues.size();
        issues.clear();
        notifyItemRangeRemoved(0, size);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

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

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int positionClicked = getAdapterPosition();
            Issue clickedIssue = issues.get(positionClicked);
            Repository issueRepository = repositories.get(positionClicked);
            onClickListener.onIssueClick(clickedIssue, issueRepository);
        }
    }
}
