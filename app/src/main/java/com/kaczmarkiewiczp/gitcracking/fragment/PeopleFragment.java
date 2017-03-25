package com.kaczmarkiewiczp.gitcracking.fragment;

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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kaczmarkiewiczp.gitcracking.AccountUtils;
import com.kaczmarkiewiczp.gitcracking.R;
import com.kaczmarkiewiczp.gitcracking.adapter.PeopleAdapter;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PeopleFragment extends Fragment {

    public final String ARG_SECTION_NUMBER = "sectionNumber";
    private final int NETWORK_ERROR = 0;
    private final int API_ERROR = 1;
    private final int USER_CANCELLED_ERROR = 3;

    private final int SECTION_FOLLOWERS = 0;
    private final int SECTION_FOLLOWING = 1;
    private View rootView;
    private int tabSection;
    private ProgressBar loadingIndicator;
    private PeopleAdapter peopleAdapter;
    private GitHubClient gitHubClient;
    private AccountUtils accountUtils;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AsyncTask backgroundTask;
    private LinearLayout errorView;
    private LinearLayout emptyView;

    public PeopleFragment() {
        // requires empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_people, container, false);
        rootView = view;
        loadingIndicator = (ProgressBar) view.findViewById(R.id.pb_loading_indicator);
        emptyView = (LinearLayout) view.findViewById(R.id.ll_empty_view);
        errorView = (LinearLayout) view.findViewById(R.id.ll_connection_err);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_people);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        peopleAdapter = new PeopleAdapter();
        recyclerView.setAdapter(peopleAdapter);
        recyclerView.setVisibility(View.VISIBLE);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_people);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (backgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                    backgroundTask.cancel(true);
                }
                backgroundTask = new GetPeople().execute(gitHubClient);
            }
        });

        tabSection = getArguments().getInt(ARG_SECTION_NUMBER);

        accountUtils = new AccountUtils(view.getContext());
        gitHubClient = accountUtils.getGitHubClient();

        setHasOptionsMenu(true);

        backgroundTask = new GetPeople().execute(gitHubClient);
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
                backgroundTask = new GetPeople().execute(gitHubClient);
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
                backgroundTask = new GetPeople().execute(gitHubClient);
            }
        });

        if (errorType == NETWORK_ERROR) {
            message.setText(R.string.network_connection_error);
        } else if (errorType == API_ERROR) {
            message.setText(R.string.loading_failed);
        }

        swipeRefreshLayout.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }

    private void showEmptyView() {
        TextView message = (TextView) rootView.findViewById(R.id.tv_empty_view);
        if (tabSection == SECTION_FOLLOWERS) {
            message.setText(R.string.no_followers);
        } else if (tabSection == SECTION_FOLLOWING) {
            message.setText(R.string.no_following);
        }
        emptyView.setVisibility(View.VISIBLE);
    }

    public class GetPeople extends AsyncTask<GitHubClient, Void, Boolean> {

        private ArrayList<User> people;
        private int errorType;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            people = new ArrayList<>();
            peopleAdapter.clearPeople();

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

            try {
                List<User> userList;
                if (tabSection == SECTION_FOLLOWERS) {
                    userList = userService.getFollowers();
                } else {
                     userList = userService.getFollowing();
                }

                for (User user : userList) {
                    if (isCancelled()) {
                        errorType = USER_CANCELLED_ERROR;
                        return false;
                    }
                    User person = userService.getUser(user.getLogin());
                    people.add(person);
                }
            } catch (RequestException e) {
                if (e.getMessage().equals("Bad credentials")) {
                    // TODO token is invalid -- tell user to login again
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

            if (noError && people.isEmpty()) {
                showEmptyView();
            } else if (noError) {
                peopleAdapter.setPeople(people);
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
    }
}
