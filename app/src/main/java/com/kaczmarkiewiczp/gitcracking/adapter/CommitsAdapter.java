package com.kaczmarkiewiczp.gitcracking.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kaczmarkiewiczp.gitcracking.R;

import org.eclipse.egit.github.core.RepositoryCommit;

import java.util.ArrayList;
import java.util.List;

public class CommitsAdapter extends RecyclerView.Adapter<CommitsAdapter.ViewHolder>{

    private List<RepositoryCommit> commits;
    private Context context;

    public CommitsAdapter() {
        commits = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        int layoutIdForListItem = R.layout.commit_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RepositoryCommit repositoryCommit = commits.get(position);
        String userIconUrl = repositoryCommit.getAuthor().getAvatarUrl();
        String commitMessage = repositoryCommit.getCommit().getMessage();
        String commitAuthor = repositoryCommit.getAuthor().getLogin();
        String committedBy = "committed by " + commitAuthor;
        String commitHash = repositoryCommit.getSha().substring(0,8);

        Glide
                .with(context)
                .load(userIconUrl)
                .error(context.getDrawable(android.R.drawable.sym_def_app_icon))
                .placeholder(R.drawable.progress_animation)
                .crossFade()
                .into(holder.imageViewUserIcon);
        holder.textViewCommit.setText(commitMessage);
        holder.textViewUser.setText(committedBy);
        holder.textViewHash.setText(commitHash);
    }

    @Override
    public int getItemCount() {
        if (commits == null) {
            return 0;
        }
        return commits.size();
    }

    public void addCommit(RepositoryCommit commit) {
        int position = commits.size();
        commits.add(commit);
        notifyItemInserted(position);
    }

    public void clearCommits() {
        int count = commits.size();
        commits.clear();
        notifyItemRangeRemoved(0, count);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView textViewCommit;
        public final TextView textViewUser;
        public final TextView textViewHash;
        public final ImageView imageViewUserIcon;

        public ViewHolder(View view) {
            super(view);
            textViewCommit = (TextView) view.findViewById(R.id.tv_commit);
            textViewUser = (TextView) view.findViewById(R.id.tv_committed_by);
            textViewHash = (TextView) view.findViewById(R.id.tv_commit_hash);
            imageViewUserIcon = (ImageView) view.findViewById(R.id.iv_commit_user_icon);
        }
    }
}
