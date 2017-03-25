package com.kaczmarkiewiczp.gitcracking.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kaczmarkiewiczp.gitcracking.R;
import com.squareup.picasso.Picasso;

import org.eclipse.egit.github.core.PullRequest;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;


public class PullRequestsAdapter extends RecyclerView.Adapter<PullRequestsAdapter.ViewHolder> {

    private ArrayList<PullRequest> pullRequests;
    private Context context;

    public PullRequestsAdapter() {
        pullRequests = new ArrayList<>();
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
        String url = pullRequests.get(position).getUrl();
        int repositoryNameStartIndex = 29;
        String repository = url.substring(repositoryNameStartIndex, url.indexOf("/pulls/"));

        String prNumber = String.valueOf(pullRequests.get(position).getNumber());
        prNumber = "#" + prNumber;

        String title = pullRequests.get(position).getTitle();
        String userIconUrl = pullRequests.get(position).getUser().getAvatarUrl();
        String username = pullRequests.get(position).getUser().getLogin();

        Date date = pullRequests.get(position).getCreatedAt();
        PrettyTime prettyTime = new PrettyTime();
        int commentsCount = pullRequests.get(position).getComments();

        holder.textViewRepository.setText(repository);
        holder.textViewPrNumber.setText(prNumber);
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
    }

    @Override
    public int getItemCount() {
        if (pullRequests == null) {
            return 0;
        }
        return pullRequests.size();
    }

    public void setPullRequests(ArrayList<PullRequest> pullRequests) {
        this.pullRequests.addAll(pullRequests);
        notifyDataSetChanged();
    }

    public void clearPullRequests() {
        this.pullRequests.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

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
        }
    }
}
