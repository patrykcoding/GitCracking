package com.kaczmarkiewiczp.gitcracking.fragment;

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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kaczmarkiewiczp.gitcracking.AccountUtils;
import com.kaczmarkiewiczp.gitcracking.BreadCrumbs;
import com.kaczmarkiewiczp.gitcracking.Consts;
import com.kaczmarkiewiczp.gitcracking.R;
import com.kaczmarkiewiczp.gitcracking.adapter.FilesAdapter;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.ContentsService;

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
                if (backgroundTask != null && backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                if (currentPath != null) {
                    backgroundTask = new GetFiles().execute(currentPath);
                } else {
                    backgroundTask = new GetFiles().execute();
                }
            }
        });
        loadingIndicator = (ProgressBar) view.findViewById(R.id.pb_loading_indicator);
        breadCrumbs = (BreadCrumbs) view.findViewById(R.id.bc_breadcrumbs);
        breadCrumbs.setCallback(this);

        AccountUtils accountUtils = new AccountUtils(context);
        gitHubClient = accountUtils.getGitHubClient();

        setHasOptionsMenu(true);

        savedFiles = (HashMap<String, List<RepositoryContents>>) bundle.getSerializable(SAVED_FILES);
        if (savedFiles == null) {
            savedFiles = new HashMap<>();
        }
        currentPath = bundle.getString(CURRENT_PATH);
        if (currentPath != null) {
            backgroundTask = new GetFiles().execute(currentPath);
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
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (backgroundTask != null && backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                if (currentPath != null) {
                    backgroundTask = new GetFiles().execute(currentPath);
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
        if (!repositoryContents.getType().equals("dir")) {
            Toast.makeText(context, "Opening files is not supported yet", Toast.LENGTH_SHORT).show();
            // TODO open file
            return;
        }
        String path = repositoryContents.getPath();
        currentPath = path;

        if (backgroundTask != null && backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
            backgroundTask.cancel(true);
        }
        backgroundTask = new GetFiles().execute(path);
        breadCrumbs.setPath(path);
    }

    @Override
    public void onBreadCrumbSelected(String path) {
        Log.i("#RepoFilesFragment", "clicked '" + path + "'");
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
            String path;
            if (params != null && params.length != 0) {
                path = params[0];
            } else {
                path = null;
            }

            try {
                if (path == null || path.isEmpty()) {
                    repositoryContentsList = contentsService.getContents(repository);
                } else {
                    if (savedFiles.containsKey(path)) {
                        repositoryContentsList = savedFiles.get(path);
                        if (repositoryContentsList != null && !repositoryContentsList.isEmpty()) {
                            return true;
                        }
                    }

                    repositoryContentsList = contentsService.getContents(repository, path);
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
}
