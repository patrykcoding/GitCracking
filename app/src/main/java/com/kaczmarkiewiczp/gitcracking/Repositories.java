package com.kaczmarkiewiczp.gitcracking;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kaczmarkiewiczp.gitcracking.adapter.RepositoriesAdapter;
import com.mikepenz.materialdrawer.Drawer;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Repositories extends AppCompatActivity {
    private final int NETWORK_ERROR = 0;
    private final int API_ERROR = 1;
    private final int USER_CANCELLED_ERROR = 2;

    private ProgressBar loadingIndicator;
    private AccountUtils accountUtils;
    private FastScrollRecyclerView recyclerView;
    private RepositoriesAdapter repositoriesAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private GitHubClient gitHubClient;
    private AsyncTask backgroundTask;
    private LinearLayout errorView;
    private LinearLayout emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repositories);
        loadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        errorView = (LinearLayout) findViewById(R.id.ll_connection_err);
        emptyView = (LinearLayout) findViewById(R.id.ll_empty_view);
        /* set toolbar */
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_repositories_toolbar);
        toolbar.setTitle("Repositories");
        setSupportActionBar(toolbar);
        NavBarUtils navBarUtils = new NavBarUtils(this, toolbar, NavBarUtils.REPOSITORIES);
        if (getIntent().getBooleanExtra("hasParent", false)) {
            navBarUtils.setNavigationDrawerButtonAsUp();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        accountUtils = new AccountUtils(this);
        gitHubClient = accountUtils.getGitHubClient();
        /* set recycler view */
        recyclerView = (FastScrollRecyclerView) findViewById(R.id.rv_repositories);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        repositoriesAdapter = new RepositoriesAdapter(this);
        recyclerView.setAdapter(repositoriesAdapter);
        recyclerView.setVisibility(View.VISIBLE);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_repositories);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (backgroundTask.getStatus() == AsyncTask.Status.RUNNING)
                    backgroundTask.cancel(true);
                backgroundTask = new GetRepositories().execute(gitHubClient);
            }
        });
        backgroundTask = new GetRepositories().execute(gitHubClient);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
                findViewById(R.id.action_refresh).startAnimation(rotate);
                if (backgroundTask.getStatus() == AsyncTask.Status.RUNNING)
                    backgroundTask.cancel(true);
                backgroundTask = new GetRepositories().execute(gitHubClient);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showErrorMessage(int errorType) {
        TextView message = (TextView) findViewById(R.id.tv_error_message);
        TextView retry = (TextView) findViewById(R.id.tv_try_again);

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backgroundTask.getStatus() == AsyncTask.Status.RUNNING)
                    backgroundTask.cancel(true);
                backgroundTask = new GetRepositories().execute(gitHubClient);
            }
        });

        if (errorType == NETWORK_ERROR) {
            message.setText(getString(R.string.network_connection_error));
        } else if (errorType == API_ERROR) {
            message.setText(getString(R.string.loading_failed));
        }

        swipeRefreshLayout.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }

    private void showEmptyView() {
        TextView message = (TextView) findViewById(R.id.tv_empty_view);
        message.setText(getString(R.string.no_repositories));
        swipeRefreshLayout.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    public class GetRepositories extends AsyncTask<GitHubClient, Void, Boolean> {

        ArrayList<Repository> repositories;
        int errorType;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
            repositoriesAdapter.clearView();
            repositories = new ArrayList<>();

            if (!swipeRefreshLayout.isRefreshing())
                loadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(GitHubClient... params) {
            GitHubClient gitHubClient = params[0];
            RepositoryService repositoryService = new RepositoryService(gitHubClient);

            try {
                for (Repository repository : repositoryService.getRepositories()) {
                    if (isCancelled()) {
                        errorType = USER_CANCELLED_ERROR;
                        return false;
                    }
                    repositories.add(repository);
                }
                Collections.sort(repositories, new RepositoryComparator());
            } catch (RequestException e) {
                if (e.getMessage().equals("Bad credentials")) {
                    // TODO token is invalid - tell user to login again
                } else {
                    errorType = API_ERROR;
                }
                return false;
            } catch (IOException e) {
                errorType = NETWORK_ERROR;
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean noError) {
            super.onPostExecute(noError);

            if (noError && !repositories.isEmpty()) {
                repositoriesAdapter.setRepositories(repositories);
            } else if (noError && repositories.isEmpty()) {
                showEmptyView();
            } else if (errorType != USER_CANCELLED_ERROR){
                showErrorMessage(errorType);
            }

            if (loadingIndicator.getVisibility() == View.VISIBLE)
                loadingIndicator.setVisibility(View.GONE);
            if (swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (loadingIndicator.getVisibility() == View.VISIBLE)
                loadingIndicator.setVisibility(View.GONE);
            if (swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
        }
    }

    public class RepositoryComparator implements Comparator<Repository> {

        @Override
        public int compare(Repository o1, Repository o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }
}
