package com.kaczmarkiewiczp.gitcracking;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.kaczmarkiewiczp.gitcracking.adapter.RepositoriesAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Repositories extends AppCompatActivity {

    private ProgressBar loadingIndicator;
    private AccountUtils accountUtils;
    private FastScrollRecyclerView recyclerView;
    private RepositoriesAdapter repositoriesAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repositories);
        loadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        /* set toolbar */
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_repositories_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Repositories");
        setSupportActionBar(toolbar);
        /* set recycler view */
        recyclerView = (FastScrollRecyclerView) findViewById(R.id.rv_repositories);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        repositoriesAdapter = new RepositoriesAdapter();
        recyclerView.setAdapter(repositoriesAdapter);
        recyclerView.setVisibility(View.VISIBLE); // TODO move it somewhere else ???
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.sr_repositories);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO refresh
            }
        });
        new NavBarUtils(this, toolbar, 2);
        accountUtils = new AccountUtils(this);
        GitHubClient gitHubClient = accountUtils.getGitHubClient();
        new GetRepositories().execute(gitHubClient);
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class GetRepositories extends AsyncTask<GitHubClient, Void, Void> {

        ArrayList<String> repositoriesName;
        ArrayList<String> repositoriesDescription;
        ArrayList<String> repositoriesForks;
        ArrayList<String> repositoriesLanguage;
        ArrayList<String> repositoriesSize;
        ArrayList<String> repositoriesWatchers;
        ArrayList<Boolean> repositoriesPrivate;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            repositoriesName = new ArrayList<>();
            repositoriesDescription = new ArrayList<>();
            repositoriesForks = new ArrayList<>();
            repositoriesLanguage = new ArrayList<>();
            repositoriesSize = new ArrayList<>();
            repositoriesWatchers = new ArrayList<>();
            repositoriesPrivate = new ArrayList<>();

            loadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(GitHubClient... params) {
            GitHubClient gitHubClient = params[0];
            RepositoryService repositoryService = new RepositoryService(gitHubClient);

            try {
                for (Repository repository : repositoryService.getRepositories()) {
                    String owner = repository.getOwner().getLogin();
                    String repositoryName = repository.getName();
                    String description = repository.getDescription();
                    String forks = String.valueOf(repository.getForks());
                    String language = repository.getLanguage();
                    String size = String.valueOf(repository.getSize());
                    String watchers = String.valueOf(repository.getWatchers());
                    Boolean isPrivate = repository.isPrivate();

                    repositoriesName.add(owner + "/" + repositoryName);
                    repositoriesDescription.add(description);
                    repositoriesForks.add(forks);
                    repositoriesLanguage.add(language);
                    repositoriesSize.add(size);
                    repositoriesWatchers.add(watchers);
                    repositoriesPrivate.add(isPrivate);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            repositoriesAdapter.addRepositories(repositoriesName);
            repositoriesAdapter.addDescriptions(repositoriesDescription);
            repositoriesAdapter.addForks(repositoriesForks);
            repositoriesAdapter.addLanguages(repositoriesLanguage);
            repositoriesAdapter.addSizes(repositoriesSize);
            repositoriesAdapter.addWatchers(repositoriesWatchers);
            repositoriesAdapter.addPrivates(repositoriesPrivate);

            loadingIndicator.setVisibility(View.GONE);
            repositoriesAdapter.updateView();
        }
    }
}
