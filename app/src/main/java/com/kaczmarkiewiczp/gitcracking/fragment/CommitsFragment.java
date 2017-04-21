package com.kaczmarkiewiczp.gitcracking.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.kaczmarkiewiczp.gitcracking.AccountUtils;
import com.kaczmarkiewiczp.gitcracking.CommitDiff;
import com.kaczmarkiewiczp.gitcracking.R;
import com.kaczmarkiewiczp.gitcracking.adapter.CommitsAdapter;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.PullRequestService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommitsFragment extends Fragment implements CommitsAdapter.CommitClickListener {

    private View rootView;
    private Context context;
    private PullRequest pullRequest;
    private Repository repository;
    private CommitsAdapter commitsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingIndicator;
    private AccountUtils accountUtils;
    private GitHubClient gitHubClient;
    private AsyncTask backgroundTask;

    public CommitsFragment() {
        // requires empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_commits, container, false);
        rootView = view;
        context = view.getContext();

        Bundle bundle = getArguments();
        pullRequest = (PullRequest) bundle.getSerializable("pull request");
        repository = (Repository) bundle.getSerializable("repository");

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_commits);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        commitsAdapter = new CommitsAdapter(this);
        recyclerView.setAdapter(commitsAdapter);
        recyclerView.setVisibility(View.VISIBLE);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_commits);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (backgroundTask != null && backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                backgroundTask = new GetCommits().execute(gitHubClient);
            }
        });
        loadingIndicator = (ProgressBar) view.findViewById(R.id.pb_loading_indicator);

        accountUtils = new AccountUtils(context);
        gitHubClient = accountUtils.getGitHubClient();

        setHasOptionsMenu(true);
        backgroundTask = new GetCommits().execute(gitHubClient);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (backgroundTask != null && backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                backgroundTask = new GetCommits().execute(gitHubClient);
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCommitClick(RepositoryCommit repositoryCommit) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("commit", repositoryCommit);
        bundle.putSerializable("repository", repository);
        intent.putExtras(bundle);
        intent.setClass(context, CommitDiff.class);
        startActivity(intent);
    }

    private class GetCommits extends AsyncTask<GitHubClient, Void, Boolean> {

        private List<RepositoryCommit> commits;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!swipeRefreshLayout.isRefreshing()) {
                loadingIndicator.setVisibility(View.VISIBLE);
            }
            commits = new ArrayList<>();
        }

        @Override
        protected Boolean doInBackground(GitHubClient... params) {
            GitHubClient gitHubClient = params[0];
            PullRequestService pullRequestService = new PullRequestService(gitHubClient);

            try {
                commits = pullRequestService.getCommits(repository, pullRequest.getNumber());
            } catch (IOException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (!success) {
                return;
            }

            commitsAdapter.clearCommits();
            for (RepositoryCommit repositoryCommit : commits) {
                commitsAdapter.addCommit(repositoryCommit);
            }

            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            } else if (loadingIndicator.getVisibility() == View.VISIBLE) {
                loadingIndicator.setVisibility(View.GONE);
            }
        }
    }
}
