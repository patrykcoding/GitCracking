package com.kaczmarkiewiczp.gitcracking;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.kaczmarkiewiczp.gitcracking.adapter.PRDiffAdapter;
import com.kaczmarkiewiczp.gitcracking.fragment.PRDiffFragment;

import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryCommitCompare;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.PullRequestService;

import java.io.IOException;
import java.util.List;

public class CommitDiff extends AppCompatActivity {

    private Context context;
    private RepositoryCommit repositoryCommit;
    private Repository repository;
    private PullRequest pullRequest;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingIndicator;
    Toolbar toolbar;
    private AccountUtils accountUtils;
    private GitHubClient gitHubClient;
    private PRDiffAdapter diffAdapter;
    private AsyncTask backgroundTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.commit_diffs);

        Bundle bundle = getIntent().getExtras();
        repositoryCommit = (RepositoryCommit) bundle.getSerializable("commit");
        repository = (Repository) bundle.getSerializable("repository");
        pullRequest = (PullRequest) bundle.getSerializable("pull request");
        if (repositoryCommit == null || repository == null || pullRequest == null) {
            finish();
            return;
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(repositoryCommit.getCommit().getMessage());
        setSupportActionBar(toolbar);
        NavBarUtils navBarUtils = new NavBarUtils(this, toolbar, NavBarUtils.NO_SELECTION);
        navBarUtils.setNavigationDrawerButtonAsUp();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        navBarUtils.killAllActivitiesOnNewActivityStart(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_diffs);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        diffAdapter = new PRDiffAdapter();
        recyclerView.setAdapter(diffAdapter);
        recyclerView.setVisibility(View.VISIBLE);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_diffs);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO
            }
        });
        loadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        accountUtils = new AccountUtils(this);
        gitHubClient = accountUtils.getGitHubClient();

        backgroundTask = new GetDiff().execute();
    }

    private class GetDiff extends AsyncTask<Void, Void, Boolean> {

        private RepositoryCommit commit;
        private RepositoryCommitCompare repositoryCommitCompare;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            diffAdapter.removeDiffs();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            CommitService commitService = new CommitService(gitHubClient);

            try {
                commit = commitService.getCommit(repository, repositoryCommit.getSha());
                repositoryCommitCompare = commitService.compare(repository, pullRequest.getBase().getSha(), commit.getSha());
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
