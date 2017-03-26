package com.kaczmarkiewiczp.gitcracking;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kaczmarkiewiczp.gitcracking.adapter.DashboardAdapter;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.event.Event;
import org.eclipse.egit.github.core.service.EventService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class Dashboard extends AppCompatActivity {

    private ProgressBar loadingIndicator;
    private RecyclerView recyclerView;
    private TextView pullRequestsWidget;
    private TextView issuesWidget;
    private TextView repositoriesWidget;
    private AccountUtils accountUtils;
    private GitHubClient gitHubClient;
    private DashboardAdapter dashboardAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AsyncTask widgetBackgroundTask;
    private AsyncTask newsFeedBackgroundTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_dashboard_toolbar);
        toolbar.setTitle("Dashboard");
        setSupportActionBar(toolbar);
        new NavBarUtils(this, toolbar, NavBarUtils.DASHBOARD);

        accountUtils = new AccountUtils(this);
        gitHubClient = accountUtils.getGitHubClient();

        loadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        pullRequestsWidget = (TextView) findViewById(R.id.tv_pull_request_count);
        issuesWidget = (TextView) findViewById(R.id.tv_issues_count);
        repositoriesWidget = (TextView) findViewById(R.id.tv_repositories_count);

        recyclerView = (RecyclerView) findViewById(R.id.rv_dashboard);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        dashboardAdapter = new DashboardAdapter();
        recyclerView.setAdapter(dashboardAdapter);
        recyclerView.setVisibility(View.VISIBLE);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_dashboard);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (widgetBackgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    widgetBackgroundTask.cancel(true);
                }
                if (newsFeedBackgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    newsFeedBackgroundTask.cancel(true);
                }
                widgetBackgroundTask = new GetDashboardData().execute(gitHubClient);
                newsFeedBackgroundTask = new GetNewsFeedData().execute(gitHubClient);
            }
        });

        widgetBackgroundTask = new GetDashboardData().execute(gitHubClient);
        newsFeedBackgroundTask = new GetNewsFeedData().execute(gitHubClient);
    }

    public class GetNewsFeedData extends AsyncTask<GitHubClient, Void, Boolean> {

        private ArrayList<Event> events;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            events = new ArrayList<>();

            swipeRefreshLayout.setVisibility(View.VISIBLE);
            dashboardAdapter.clearEvents();
            if (!swipeRefreshLayout.isRefreshing()) {
                loadingIndicator.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Boolean doInBackground(GitHubClient... params) {
            GitHubClient gitHubClient = params[0];
            EventService eventService = new EventService(gitHubClient);
            String user = accountUtils.getLogin();

            PageIterator<Event> eventPageIterator = eventService.pageUserReceivedEvents(user);
            Collection<Event> eventCollection = eventPageIterator.next();
            if (eventCollection.isEmpty()) {
                return false;
            }
            for (Event anEventCollection : eventCollection) {
                dashboardAdapter.addEvent(anEventCollection);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (loadingIndicator.getVisibility() == View.VISIBLE) {
                loadingIndicator.setVisibility(View.INVISIBLE);
            }
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            if (!success) {
                // TODO clear adapter
                // TODO show empty view
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (loadingIndicator.getVisibility() == View.VISIBLE) {
                loadingIndicator.setVisibility(View.INVISIBLE);
            }
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            // TODO clear adapter && show empty view
        }
    }


    public class GetDashboardData extends AsyncTask<GitHubClient, Void, Boolean> {

        private int pullRequestsCount;
        private int repositoriesCount;
        private int issuesCount;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pullRequestsCount = 0;
            repositoriesCount = 0;
            issuesCount = 0;

            if (pullRequestsWidget.getText().toString().isEmpty()) {
                pullRequestsWidget.setText("-");
            }
            if (issuesWidget.getText().toString().isEmpty()) {
                issuesWidget.setText("-");
            }
            if (repositoriesWidget.getText().toString().isEmpty()) {
                repositoriesWidget.setText("-");
            }
        }

        @Override
        protected Boolean doInBackground(GitHubClient... params) {
            GitHubClient gitHubClient = params[0];
            RepositoryService repositoryService = new RepositoryService(gitHubClient);
            PullRequestService pullRequestService = new PullRequestService(gitHubClient);

            try {
                for (Repository repository : repositoryService.getRepositories()) {
                    if (isCancelled()) {
                        return false;
                    }

                    issuesCount += repository.getOpenIssues();
                    repositoriesCount++;
                    pullRequestsCount += pullRequestService.getPullRequests(repository, PullRequestService.PR_STATE).size();
                }
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                pullRequestsWidget.setText(String.valueOf(pullRequestsCount));
                issuesWidget.setText(String.valueOf(issuesCount));
                repositoriesWidget.setText(String.valueOf(repositoriesCount));
            }
        }
    }
}
