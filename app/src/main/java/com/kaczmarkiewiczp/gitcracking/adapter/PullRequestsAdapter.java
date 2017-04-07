package com.kaczmarkiewiczp.gitcracking.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kaczmarkiewiczp.gitcracking.Comparators;
import com.kaczmarkiewiczp.gitcracking.R;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;


public class PullRequestsAdapter extends RecyclerView.Adapter<PullRequestsAdapter.ViewHolder> {

    private ArrayList<PullRequest> pullRequests;
    private HashMap<String, Repository> repositories;
    private Context context;
    private final PullRequestClickListener onClickListener;

    public interface PullRequestClickListener {
        void onPullRequestClick(PullRequest pullRequest, Repository repository);
    }

    public PullRequestsAdapter(PullRequestClickListener listener) {
        pullRequests = new ArrayList<>();
        repositories = new HashMap<>();
        onClickListener = listener;
    }

    @Override
    public PullRequestsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        int layoutIdForListItem = R.layout.pull_requests_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PullRequest pullRequest = pullRequests.get(position);
        Repository repo = repositories.get(pullRequest.getUrl());
        String repositoryName = repo.getName();
        String repositoryOwner = repo.getOwner().getLogin();
        String repository = repositoryOwner + "/" + repositoryName;
        String prNumber = String.valueOf(pullRequest.getNumber());
        prNumber = "#" + prNumber;

        String title = pullRequest.getTitle();
        String userIconUrl = pullRequest.getUser().getAvatarUrl();
        String username = pullRequest.getUser().getLogin();

        Date date = pullRequest.getCreatedAt();
        PrettyTime prettyTime = new PrettyTime();
        int commentsCount = pullRequest.getComments();

        holder.textViewRepository.setText(repository);
        holder.textViewPrNumber.setText(prNumber);
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
    }

    @Override
    public int getItemCount() {
        if (pullRequests == null) {
            return 0;
        }
        return pullRequests.size();
    }

    public void addPullRequest(PullRequest pullRequest, Repository repository) {
        pullRequests.add(pullRequest);
        String key = pullRequest.getUrl();
        repositories.put(key, repository);
    }

    public void showPullRequests() {
        Collections.sort(pullRequests, new Comparators.PullRequestsComparator());
        int count = pullRequests.size();
        notifyItemRangeInserted(0, count);
    }

    public void clearPullRequests() {
        int size = pullRequests.size();
        pullRequests.clear();
        repositories.clear();
        notifyItemRangeRemoved(0, size);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView textViewRepository;
        public final TextView textViewPrNumber;
        public final TextView textViewTitle;
        public final ImageView imageViewUserIcon;
        public final TextView textViewUser;
        public final TextView textViewDate;
        public final LinearLayout linearLayoutComments;
        public final TextView textViewCommentsCount;

        public ViewHolder(View view) {
            super(view);
            textViewRepository = (TextView) view.findViewById(R.id.tv_pull_request_repo);
            textViewPrNumber = (TextView) view.findViewById(R.id.tv_pull_request_number);
            textViewTitle = (TextView) view.findViewById(R.id.tv_pull_request_title);
            imageViewUserIcon = (ImageView) view.findViewById(R.id.iv_user_icon);
            textViewUser = (TextView) view.findViewById(R.id.tv_user);
            textViewDate = (TextView) view.findViewById(R.id.tv_date);
            linearLayoutComments = (LinearLayout) view.findViewById(R.id.ll_comments);
            textViewCommentsCount = (TextView) view.findViewById(R.id.tv_comments_count);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int positionedClicked = getAdapterPosition();
            PullRequest prClicked = pullRequests.get(positionedClicked);
            String key = prClicked.getUrl();
            Repository repository = repositories.get(key);
            onClickListener.onPullRequestClick(prClicked, repository);
        }
    }
}
