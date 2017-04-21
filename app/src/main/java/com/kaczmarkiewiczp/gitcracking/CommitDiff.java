package com.kaczmarkiewiczp.gitcracking;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.kaczmarkiewiczp.gitcracking.adapter.DiffAdapter;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryCommitCompare;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;

import java.io.IOException;
import java.util.List;

public class CommitDiff extends AppCompatActivity {

    private Context context;
    private RepositoryCommit repositoryCommit;
    private Repository repository;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingIndicator;
    private Toolbar toolbar;
    private AccountUtils accountUtils;
    private NavBarUtils navBarUtils;
    private GitHubClient gitHubClient;
    private DiffAdapter diffAdapter;
    private AsyncTask backgroundTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.commit_diffs);

        Bundle bundle = getIntent().getExtras();
        repositoryCommit = (RepositoryCommit) bundle.getSerializable("commit");
        repository = (Repository) bundle.getSerializable("repository");
        if (repositoryCommit == null || repository == null) {
            finish();
            return;
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(repositoryCommit.getCommit().getMessage());
        setSupportActionBar(toolbar);
        navBarUtils = new NavBarUtils(this, toolbar, NavBarUtils.NO_SELECTION);
        navBarUtils.setNavigationDrawerButtonAsUp();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        navBarUtils.killAllActivitiesOnNewActivityStart(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_diffs);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        diffAdapter = new DiffAdapter();
        recyclerView.setAdapter(diffAdapter);
        recyclerView.setVisibility(View.VISIBLE);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_diffs);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (backgroundTask != null && backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                backgroundTask = new GetDiff().execute();
            }
        });
        loadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        accountUtils = new AccountUtils(this);
        gitHubClient = accountUtils.getGitHubClient();

        backgroundTask = new GetDiff().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Boolean accountHasBeenModified = data.getBooleanExtra("accountHasBeenModified", false);
            if (accountHasBeenModified) {
                navBarUtils = new NavBarUtils(this, toolbar, NavBarUtils.NO_SELECTION);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
                findViewById(R.id.action_refresh).startAnimation(rotate);
                if (backgroundTask != null && backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                backgroundTask = new GetDiff().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class GetDiff extends AsyncTask<Void, Void, Boolean> {

        private RepositoryCommit commit;
        private RepositoryCommitCompare repositoryCommitCompare;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!swipeRefreshLayout.isRefreshing()) {
                loadingIndicator.setVisibility(View.VISIBLE);
            }
            diffAdapter.removeDiffs();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            CommitService commitService = new CommitService(gitHubClient);

            try {
                commit = commitService.getCommit(repository, repositoryCommit.getSha());
                Commit parent = commit.getParents().get(0);
                repositoryCommitCompare = commitService.compare(repository, parent.getSha(), commit.getSha());
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            } else if (loadingIndicator.getVisibility() == View.VISIBLE) {
                loadingIndicator.setVisibility(View.GONE);
            }

            if (!success) {
                return;
            }

            int fileChanged = 0;
            int additions = 0;
            int deletions = 0;
            List<CommitFile> commitFiles = repositoryCommitCompare.getFiles();
            for (CommitFile commitFile : commitFiles) {
                fileChanged++;
                additions += commitFile.getAdditions();
                deletions += commitFile.getDeletions();
                diffAdapter.addFileDiff(commitFile.getFilename(), commitFile.getPatch());
            }
            diffAdapter.addSummary(fileChanged, additions, deletions);
        }
    }
}
