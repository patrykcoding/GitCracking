package com.kaczmarkiewiczp.gitcracking.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import com.kaczmarkiewiczp.gitcracking.Consts;
import com.kaczmarkiewiczp.gitcracking.R;
import com.kaczmarkiewiczp.gitcracking.adapter.CommitsAdapter;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class RepoCommitsFragment extends Fragment implements CommitsAdapter.CommitClickListener {

    private View rootView;
    private Context context;
    private Repository repository;
    private GitHubClient gitHubClient;
    private ProgressBar loadingIndicator;
    private CommitsAdapter commitsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<RepositoryBranch> branchList;
    private HashMap<String, String> branchMap;
    private String currentBranch;
    private boolean isBranchListReady;
    private AsyncTask backgroundTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_commits, container, false);
        rootView = view;
        context = view.getContext();
        Bundle bundle = getArguments();
        repository = (Repository) bundle.getSerializable(Consts.REPOSITORY_ARG);
        currentBranch = bundle.getString(Consts.BRANCH_ARG);
        if (currentBranch == null && repository.getDefaultBranch() != null) {
            currentBranch = repository.getDefaultBranch();
        }
        branchMap = new HashMap<>();
        isBranchListReady = false;

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_commits);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        commitsAdapter = new CommitsAdapter(this);
        recyclerView.setAdapter(commitsAdapter);
        recyclerView.setItemAnimator(new SlideInUpAnimator());
        recyclerView.getItemAnimator().setAddDuration(1000);
        recyclerView.setVisibility(View.VISIBLE);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_commits);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (backgroundTask != null && backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                backgroundTask = new GetCommits().execute();
            }
        });
        loadingIndicator = (ProgressBar) view.findViewById(R.id.pb_loading_indicator);

        AccountUtils accountUtils = new AccountUtils(context);
        gitHubClient = accountUtils.getGitHubClient();

        setHasOptionsMenu(true);

        new GetBranches().execute();
        backgroundTask = new GetCommits().execute();
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        Bundle bundle = getArguments();
        if (currentBranch != null) {
            bundle.putString(Consts.BRANCH_ARG, currentBranch);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.repo_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_branch:
                displayBranches();
                return true;
            case R.id.action_refresh:
                if (backgroundTask != null && backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                backgroundTask = new GetCommits().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCommitClick(RepositoryCommit repositoryCommit) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Consts.COMMIT_ARG, repositoryCommit);
        bundle.putSerializable(Consts.REPOSITORY_ARG, repository);
        intent.putExtras(bundle);
        intent.setClass(context, CommitDiff.class);
        startActivity(intent);
    }

    private void updateBranchesMap() {
        if (branchMap == null) {
            branchMap = new HashMap<>();
        }
        for (RepositoryBranch branch : branchList) {
            branchMap.put(branch.getName(), branch.getCommit().getSha());
        }
    }

    private void displayBranches() {
        if (branchList == null) {
            new ShowLoadingDialog().execute();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select a branch");
        final int[] selectedOption = new int[1];
        selectedOption[0] = -1;
        String[] options = new String[branchList.size()];
        int i = 0;
        for (RepositoryBranch branch : branchList) {
            if (branch.getName().equals(currentBranch)) {
                selectedOption[0] = i;
            }
            options[i++] = branch.getName();
        }
        builder.setSingleChoiceItems(options, selectedOption[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedOption[0] = which;
            }
        });
        builder.setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentBranch = branchList.get(selectedOption[0]).getName();
                backgroundTask = new GetCommits().execute();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private class GetCommits extends AsyncTask<Void, Void, Boolean> {

        private List<RepositoryCommit> repositoryCommits;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!swipeRefreshLayout.isRefreshing()) {
                loadingIndicator.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            CommitService commitService = new CommitService(gitHubClient);
            String sha = branchMap.get(currentBranch);

            try {
                repositoryCommits = commitService.getCommits(repository, sha, null);
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
            } else {
                loadingIndicator.setVisibility(View.GONE);
            }
            if (!success) {
                return;
            }
            commitsAdapter.clearCommits();

            for (RepositoryCommit repositoryCommit : repositoryCommits) {
                commitsAdapter.addCommit(repositoryCommit);
            }
        }
    }

    private class GetBranches extends AsyncTask<Void, Void, Boolean> {

        private List<RepositoryBranch> repositoryBranches;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isBranchListReady = false;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            RepositoryService repositoryService = new RepositoryService(gitHubClient);

            try {
                repositoryBranches = repositoryService.getBranches(repository);
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
            branchList = repositoryBranches;
            isBranchListReady = true;
            updateBranchesMap();
        }
    }

    private class ShowLoadingDialog extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, "Getting Branches", "Please wait", true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (!isBranchListReady) {
                if (isCancelled()) {
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            displayBranches();
        }
    }
}
