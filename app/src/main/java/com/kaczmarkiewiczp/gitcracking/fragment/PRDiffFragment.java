package com.kaczmarkiewiczp.gitcracking.fragment;

import android.content.Context;
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
import com.kaczmarkiewiczp.gitcracking.R;
import com.kaczmarkiewiczp.gitcracking.adapter.PRDiffAdapter;

import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommitCompare;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;

import java.io.IOException;
import java.util.List;

public class PRDiffFragment extends Fragment {

    private View rootView;
    private Context context;
    private PullRequest pullRequest;
    private Repository repository;
    private PRDiffAdapter diffAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingIndicator;
    private AccountUtils accountUtils;
    private GitHubClient gitHubClient;
    private AsyncTask backgroundTask;

    public PRDiffFragment() {
        // requires empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_diffs, container, false);
        rootView = view;
        context = view.getContext();

        Bundle bundle = getArguments();
        pullRequest = (PullRequest) bundle.getSerializable("pull request");
        repository = (Repository) bundle.getSerializable("repository");

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_diffs);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        diffAdapter = new PRDiffAdapter();
        recyclerView.setAdapter(diffAdapter);
        recyclerView.setVisibility(View.VISIBLE);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_diffs);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (backgroundTask != null && backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                backgroundTask = new GetDiffs().execute();
            }
        });
        loadingIndicator = (ProgressBar) view.findViewById(R.id.pb_loading_indicator);

        accountUtils = new AccountUtils(context);
        gitHubClient = accountUtils.getGitHubClient();

        setHasOptionsMenu(true);

        backgroundTask = new GetDiffs().execute();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (backgroundTask != null && backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                backgroundTask = new GetDiffs().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class GetDiffs extends AsyncTask<Void, Void, Boolean> {

        private RepositoryCommitCompare commitCompare;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!swipeRefreshLayout.isRefreshing()) {
                loadingIndicator.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setEnabled(false);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            CommitService commitService = new CommitService(gitHubClient);

            String base = pullRequest.getBase().getSha();
            String head = pullRequest.getHead().getSha();
            try {
                commitCompare = commitService.compare(repository, base, head);
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
                List<CommitFile> commitFiles = commitCompare.getFiles();
                for (CommitFile commitFile : commitFiles) {
                    String filename = commitFile.getFilename();
                    String diff = commitFile.getPatch();
                    diffAdapter.addFileDiff(filename, diff);
                }
            }

            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            } else {
                loadingIndicator.setVisibility(View.GONE);
                swipeRefreshLayout.setEnabled(true);
            }
        }
    }
}
