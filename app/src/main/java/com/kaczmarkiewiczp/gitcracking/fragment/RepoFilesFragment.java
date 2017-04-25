package com.kaczmarkiewiczp.gitcracking.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kaczmarkiewiczp.gitcracking.AccountUtils;
import com.kaczmarkiewiczp.gitcracking.BreadCrumbs;
import com.kaczmarkiewiczp.gitcracking.Consts;
import com.kaczmarkiewiczp.gitcracking.FileViewerDialog;
import com.kaczmarkiewiczp.gitcracking.R;
import com.kaczmarkiewiczp.gitcracking.adapter.FilesAdapter;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RepoFilesFragment extends Fragment implements FilesAdapter.OnClickListener, BreadCrumbs.OnClickListener {

    private final String SAVED_FILES = "saved files HashMap";
    private final String CURRENT_PATH = "current path";

    private View rootView;
    private Context context;
    private Repository repository;
    private GitHubClient gitHubClient;
    private ProgressBar loadingIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AsyncTask<String, Void, Boolean> backgroundTask;
    private FilesAdapter filesAdapter;
    private BreadCrumbs breadCrumbs;
    private List<RepositoryBranch> branchList;
    private HashMap<String, String> branchMap;
    private String currentBranch;
    private boolean isBranchListReady;
    private HashMap<String, List<RepositoryContents>> savedFiles;
    private String currentPath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.repo_files_fragment, container, false);
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

        loadingIndicator = (ProgressBar) view.findViewById(R.id.pb_loading_indicator);
        breadCrumbs = (BreadCrumbs) view.findViewById(R.id.bc_breadcrumbs);
        breadCrumbs.setCallback(this);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_files);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        filesAdapter = new FilesAdapter(this);
        recyclerView.setAdapter(filesAdapter);
        recyclerView.setVisibility(View.VISIBLE);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_files);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (savedFiles != null) {
                    savedFiles.clear();
                }
                if (backgroundTask != null && backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                if (currentPath != null) {
                    backgroundTask = new GetFiles().execute(currentPath);
                    breadCrumbs.setPath(currentPath);
                } else {
                    backgroundTask = new GetFiles().execute();
                }
            }
        });

        AccountUtils accountUtils = new AccountUtils(context);
        gitHubClient = accountUtils.getGitHubClient();

        setHasOptionsMenu(true);

        savedFiles = (HashMap<String, List<RepositoryContents>>) bundle.getSerializable(SAVED_FILES);
        if (savedFiles == null) {
            savedFiles = new HashMap<>();
        }

        new GetBranches().execute();
        currentPath = bundle.getString(CURRENT_PATH);
        if (currentPath != null) {
            backgroundTask = new GetFiles().execute(currentPath);
            breadCrumbs.setPath(currentPath);
        } else {
            backgroundTask = new GetFiles().execute();
        }
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        Bundle bundle = getArguments();
        if (savedFiles != null) {
            bundle.putSerializable(SAVED_FILES, savedFiles);
        }
        if (currentPath != null) {
            bundle.putString(CURRENT_PATH, currentPath);
        }
        if (currentBranch != null) {
            bundle.putString(Consts.BRANCH_ARG, currentBranch);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (backgroundTask != null && backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
            backgroundTask.cancel(true);
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
                savedFiles.clear();
                if (backgroundTask != null && backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                if (currentPath != null) {
                    backgroundTask = new GetFiles().execute(currentPath);
                    breadCrumbs.setPath(currentPath);
                } else {
                    backgroundTask = new GetFiles().execute();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFileClicked(RepositoryContents repositoryContents) {
        String path = repositoryContents.getPath();

        if (repositoryContents.getType().equals("dir")) {
            currentPath = path;

            if (backgroundTask != null && backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                backgroundTask.cancel(true);
            }
            backgroundTask = new GetFiles().execute(path);
            breadCrumbs.setPath(path);
        } else {
            new GetFileContent().execute(path);
        }
    }

    @Override
    public void onBreadCrumbSelected(String path) {
        if (backgroundTask != null && backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
            backgroundTask.cancel(true);
        }
        currentPath = path;
        backgroundTask = new GetFiles().execute(path);
        breadCrumbs.setPath(path);
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
                savedFiles.clear();
                currentPath = "";
                breadCrumbs.setPath(currentPath);
                backgroundTask = new GetFiles().execute(currentPath);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private class GetFileContent extends AsyncTask<String, Void, RepositoryContents> {

        @Override
        protected RepositoryContents doInBackground(String... params) {
            ContentsService contentsService = new ContentsService(gitHubClient);
            String path = params[0];
            String sha = branchMap.get(currentBranch);
            RepositoryContents file;

            try {
                List<RepositoryContents> repositoryContent = contentsService.getContents(repository, path, sha);
                if (repositoryContent.size() == 0) {
                    return null;
                }
                file = repositoryContent.get(0);

            } catch (IOException e) {
                return null;
            }
            return file;
        }

        @Override
        protected void onPostExecute(RepositoryContents file) {
            super.onPostExecute(file);
            if (file == null) {
                return;
            }
            String base64Content = file.getContent();
            byte[] data = Base64.decode(base64Content, Base64.DEFAULT);
            String fileContent = new String(data);

            FileViewerDialog fileViewerDialog = new FileViewerDialog();
            fileViewerDialog.setFileContent(fileContent);
            fileViewerDialog.setToolbarTitle(file.getName());
            fileViewerDialog.show(getFragmentManager(), "File Viewer");
        }
    }


    private class GetFiles extends AsyncTask<String, Void, Boolean> {

        private List<RepositoryContents> repositoryContentsList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!swipeRefreshLayout.isRefreshing()) {
                loadingIndicator.setVisibility(View.VISIBLE);
            }
            filesAdapter.clearFiles();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            ContentsService contentsService = new ContentsService(gitHubClient);
            String sha = branchMap.get(currentBranch);
            String path;
            if (params != null && params.length != 0) {
                path = params[0];
            } else {
                path = null;
            }

            try {
                if (path == null || path.trim().isEmpty()) {
                    repositoryContentsList = contentsService.getContents(repository, null, sha);
                } else {
                    if (savedFiles.containsKey(path)) {
                        repositoryContentsList = savedFiles.get(path);
                        savedFiles.put(path, new ArrayList<>(repositoryContentsList));
                        return true;
                    }

                    repositoryContentsList = contentsService.getContents(repository, path, sha);
                    savedFiles.put(path, new ArrayList<>(repositoryContentsList));
                }
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
            if (success) {
                filesAdapter.addFiles(repositoryContentsList);
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
