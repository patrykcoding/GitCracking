package com.kaczmarkiewiczp.gitcracking;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import java.io.IOException;

public class Dashboard extends AppCompatActivity {

    private ProgressBar loadingIndicator;
    private TextView pullRequestsWidget;
    private TextView issuesWidget;
    private TextView repositoriesWidget;
    private AccountUtils accountUtils;
    private GitHubClient gitHubClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_dashboard_toolbar);
        toolbar.setTitle("Dashboard");
        setSupportActionBar(toolbar);
        new NavBarUtils(this, toolbar, NavBarUtils.DASHBOARD);

        loadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        pullRequestsWidget = (TextView) findViewById(R.id.tv_pull_request_count);
        issuesWidget = (TextView) findViewById(R.id.tv_issues_count);
        repositoriesWidget = (TextView) findViewById(R.id.tv_repositories_count);

        accountUtils = new AccountUtils(this);
        gitHubClient = accountUtils.getGitHubClient();

        new GetDashboardData().execute(gitHubClient);
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

            loadingIndicator.setVisibility(View.VISIBLE);
            pullRequestsWidget.setText("-");
            issuesWidget.setText("-");
            repositoriesWidget.setText("-");
        }

        @Override
        protected Boolean doInBackground(GitHubClient... params) {
            GitHubClient gitHubClient = params[0];
            RepositoryService repositoryService = new RepositoryService(gitHubClient);
            PullRequestService pullRequestService = new PullRequestService(gitHubClient);

            try {
                for (Repository repository : repositoryService.getRepositories()) {
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
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            loadingIndicator.setVisibility(View.GONE);

            pullRequestsWidget.setText(String.valueOf(pullRequestsCount));
            issuesWidget.setText(String.valueOf(issuesCount));
            repositoriesWidget.setText(String.valueOf(repositoriesCount));
        }
    }
}
