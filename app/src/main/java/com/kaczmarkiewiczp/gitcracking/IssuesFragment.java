package com.kaczmarkiewiczp.gitcracking;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.kaczmarkiewiczp.gitcracking.adapter.IssuesAdapter;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IssuesFragment extends Fragment {
    public final String ARG_SECTION_NUMBER = "sectionNumber";
    private Context context;
    private int tabNumber;
    private ProgressBar loadingIndicator;
    private IssuesAdapter issuesAdapter;
    private GitHubClient gitHubClient;
    private AccountUtils accountUtils;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AsyncTask backgroundTask;

    public IssuesFragment() {
        // requires empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_issues, container, false);

        context = rootView.getContext();
        loadingIndicator = (ProgressBar) rootView.findViewById(R.id.pb_loading_indicator);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_issues);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        issuesAdapter = new IssuesAdapter();
        recyclerView.setAdapter(issuesAdapter);
        recyclerView.setVisibility(View.VISIBLE);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srl_issues);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                backgroundTask = new GetIssues().execute(gitHubClient);
            }
        });

        tabNumber = getArguments().getInt(ARG_SECTION_NUMBER);

        accountUtils = new AccountUtils(context);
        gitHubClient = accountUtils.getGitHubClient();

        setHasOptionsMenu(true);

        backgroundTask = new GetIssues().execute(gitHubClient);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                backgroundTask = new GetIssues().execute(gitHubClient);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class GetIssues extends AsyncTask<GitHubClient, Void, Boolean> {

        private final int NETWORK_ERROR = 0;
        private final int API_ERROR = 1;
        private final int USER_CANCELLED_ERROR = 3;

        private ArrayList<Issue> issues;
        private int error_type;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            issues = new ArrayList<>();
            issuesAdapter.clearView();
            if (!swipeRefreshLayout.isRefreshing()) {
                loadingIndicator.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Boolean doInBackground(GitHubClient... params) {
            GitHubClient gitHubClient = params[0];
            IssueService issueService = new IssueService(gitHubClient);
            RepositoryService repositoryService = new RepositoryService(gitHubClient);

            try {
                for (Repository repository : repositoryService.getRepositories()) {
                    if (isCancelled()) {
                        error_type = USER_CANCELLED_ERROR;
                        return false;
                    }
                    if (repository.getOpenIssues() < 1) {
                        continue;
                    }
                    List<Issue> repositoryIssues = issueService.getIssues(repository, null);
                    for (Issue issue : repositoryIssues) {
                        issues.add(issue);
                    }
                }
                Collections.sort(issues, new IssuesComparator());
            } catch (IOException e) {
                // TODO catch errors -- show correct view
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean noError) {
            super.onPostExecute(noError);

            if (noError && !issues.isEmpty()) {
                issuesAdapter.setIssues(issues);
                issuesAdapter.updateView();
            } else if (noError && issues.isEmpty()) {
                // TODO show emptyview
            } else if (error_type != USER_CANCELLED_ERROR) {
                // TODO show error message
            }

            if (loadingIndicator.getVisibility() == View.VISIBLE) {
                loadingIndicator.setVisibility(View.GONE);
            }
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (loadingIndicator.getVisibility() == View.VISIBLE) {
                loadingIndicator.setVisibility(View.GONE);
            }
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    public class IssuesComparator implements Comparator<Issue> {

        @Override
        public int compare(Issue o1, Issue o2) {
            return o2.getCreatedAt().compareTo(o1.getCreatedAt());
        }
    }
}
