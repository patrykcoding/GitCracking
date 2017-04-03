package com.kaczmarkiewiczp.gitcracking.fragment;

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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kaczmarkiewiczp.gitcracking.AccountUtils;
import com.kaczmarkiewiczp.gitcracking.R;
import com.kaczmarkiewiczp.gitcracking.adapter.PullRequestsAdapter;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class PullRequestsFragment extends Fragment {
    public final String ARG_SECTION_NUMBER = "sectionNumber";
    private final int NETWORK_ERROR = 0;
    private final int API_ERROR = 1;
    private final int USER_CANCELLED_ERROR = 3;

    private final int SECTION_CREATED = 0;
    private final int SECTION_ASSIGNED = 1;
    private View rootView;
    private int tabSection;
    private ProgressBar loadingIndicator;
    private PullRequestsAdapter prAdapter;
    private GitHubClient gitHubClient;
    private AccountUtils accountUtils;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AsyncTask backgroundTask;
    private LinearLayout errorView;
    private LinearLayout emptyView;

    public PullRequestsFragment() {
        // requires empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_pull_requests, container, false);
        rootView = view;
        loadingIndicator = (ProgressBar) view.findViewById(R.id.pb_loading_indicator);
        emptyView = (LinearLayout) view.findViewById(R.id.ll_empty_view);
        errorView = (LinearLayout) view.findViewById(R.id.ll_connection_err);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_pull_requests);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        prAdapter = new PullRequestsAdapter();
        recyclerView.setAdapter(prAdapter);
        recyclerView.setItemAnimator(new SlideInUpAnimator());
        recyclerView.getItemAnimator().setAddDuration(1000);
        recyclerView.getItemAnimator().setRemoveDuration(1000);
        recyclerView.setVisibility(View.VISIBLE);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_pull_requests);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                backgroundTask = new GetPullRequests().execute(gitHubClient);
            }
        });

        tabSection = getArguments().getInt(ARG_SECTION_NUMBER);
        accountUtils = new AccountUtils(rootView.getContext());
        gitHubClient = accountUtils.getGitHubClient();

        setHasOptionsMenu(true);

        backgroundTask = new GetPullRequests().execute(gitHubClient);

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
                if (backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                backgroundTask = new GetPullRequests().execute(gitHubClient);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showErrorMessage(int errorType) {
        TextView message = (TextView) rootView.findViewById(R.id.tv_error_message);
        TextView retry = (TextView) rootView.findViewById(R.id.tv_try_again);

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                backgroundTask = new GetPullRequests().execute(gitHubClient);
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
        TextView message = (TextView) rootView.findViewById(R.id.tv_empty_view);
        message.setText(getString(R.string.no_pull_requests));
        emptyView.setVisibility(View.VISIBLE);
    }

    public class GetPullRequests extends AsyncTask<GitHubClient, Void, Boolean> {

        private ArrayList<PullRequest> pullRequests;
        private int errorType;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pullRequests = new ArrayList<>();
            prAdapter.clearPullRequests();

            swipeRefreshLayout.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
            if (!swipeRefreshLayout.isRefreshing()) {
                loadingIndicator.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Boolean doInBackground(GitHubClient... params) {
            GitHubClient gitHubClient = params[0];
            UserService userService = new UserService(gitHubClient);
            RepositoryService repositoryService = new RepositoryService(gitHubClient);
            PullRequestService pullRequestService = new PullRequestService(gitHubClient);

            try {
                String user = userService.getUser().getLogin();
                for (Repository repository : repositoryService.getRepositories()) {
                    if (isCancelled()) {
                        errorType = USER_CANCELLED_ERROR;
                        return false;
                    }
                    List<PullRequest> repositoryPullRequests = pullRequestService.getPullRequests(repository, "open");
                    for (PullRequest pullRequest : repositoryPullRequests) {
                        if (tabSection == SECTION_ASSIGNED) {
                            if (pullRequest.getAssignee() != null && pullRequest.getAssignee().getLogin().equals(user)) {
                                pullRequests.add(pullRequest);
                            }
                        } else if (tabSection == SECTION_CREATED) {
                            pullRequests.add(pullRequest);
                        }
                    }
                }
                Collections.sort(pullRequests, new PullRequestsComparator());
            } catch (RequestException e) {
                if (e.getMessage().equals("Bad credentials")) {
                    // TODO token is invalid -- tell user to again
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

            if (noError && !pullRequests.isEmpty()) {
                prAdapter.setPullRequests(pullRequests);
            } else if (noError && pullRequests.isEmpty()) {
                showEmptyView();
            } else if (errorType != USER_CANCELLED_ERROR) {
                showErrorMessage(errorType);
            }

            if (loadingIndicator.getVisibility() == View.VISIBLE) {
                loadingIndicator.setVisibility(View.GONE);
            }
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (loadingIndicator.getVisibility() == View.VISIBLE) {
                loadingIndicator.setVisibility(View.GONE);
            }
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }

        public class PullRequestsComparator implements Comparator<PullRequest> {

            @Override
            public int compare(PullRequest o1, PullRequest o2) {
                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
            }
        }
    }
}
