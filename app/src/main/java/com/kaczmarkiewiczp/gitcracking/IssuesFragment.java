package com.kaczmarkiewiczp.gitcracking;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.kaczmarkiewiczp.gitcracking.adapter.IssuesAdapter;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IssuesFragment extends Fragment {
    public final String ARG_SECTION_NUMBER = "sectionNumber";
    private Context context;
    private int tabNumber;
    private ProgressBar loadingIndicator;
    private IssuesAdapter issuesAdapter;
    private GitHubClient gitHubClient;
    private AccountUtils accountUtils;
    private SwipeRefreshLayout swipeRefreshLayout;

    public IssuesFragment() {
        // requires empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_issues, container, false);

        context = rootView.getContext();
        loadingIndicator = (ProgressBar) rootView.findViewById(R.id.pb_loading_indicator);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_issues);
        recyclerView.setHasFixedSize(true);
        issuesAdapter = new IssuesAdapter();
        recyclerView.setAdapter(issuesAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srl_issues);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO refresh
            }
        });

        tabNumber = getArguments().getInt(ARG_SECTION_NUMBER);

        accountUtils = new AccountUtils(context);
        gitHubClient = accountUtils.getGitHubClient();

        setHasOptionsMenu(true);

        new GetIssues().execute(gitHubClient);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_refresh) {
            // TODO refresh
        }
        return super.onOptionsItemSelected(item);
    }

    public class GetIssues extends AsyncTask<GitHubClient, Void, Boolean> {

        ArrayList<Issue> issues;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            issues = new ArrayList<>();
            loadingIndicator.setVisibility(View.VISIBLE);

        }

        @Override
        protected Boolean doInBackground(GitHubClient... params) {
            GitHubClient gitHubClient = params[0];
            IssueService issueService = new IssueService(gitHubClient);
            RepositoryService repositoryService = new RepositoryService(gitHubClient);

            try {
                for (Repository repository : repositoryService.getRepositories()) {
                    if (repository.getOpenIssues() < 1) {
                        continue;
                    }
                    List<Issue> repositoryIssues = issueService.getIssues(repository, null);
                    for (Issue issue : repositoryIssues) {
                        issues.add(issue);
                    }
                }
                Collections.sort(issues, new IssuesComparator());
            } catch (IOException e) {
                // TODO catch errors -- show correct view
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            issuesAdapter.setIssues(issues);
            issuesAdapter.updateView();

            loadingIndicator.setVisibility(View.GONE);
        }

        public class IssuesComparator implements Comparator<Issue> {

            @Override
            public int compare(Issue o1, Issue o2) {
                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
            }
        }
    }
}
