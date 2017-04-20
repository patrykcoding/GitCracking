package com.kaczmarkiewiczp.gitcracking.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kaczmarkiewiczp.gitcracking.AccountUtils;
import com.kaczmarkiewiczp.gitcracking.Consts;
import com.kaczmarkiewiczp.gitcracking.R;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.MarkdownService;
import org.eclipse.egit.github.core.service.PullRequestService;

import java.io.IOException;

public class RepoHomeFragment extends Fragment {

    private View rootView;
    private Context context;
    private Repository repository;
    private GitHubClient gitHubClient;
    private ProgressBar loadingIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;

    public RepoHomeFragment() {
        // requires empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_repo_home, container, false);
        rootView = view;
        context = view.getContext();
        Bundle bundle = getArguments();
        repository = (Repository) bundle.getSerializable(Consts.REPOSITORY_ARG);

        AccountUtils accountUtils = new AccountUtils(context);
        gitHubClient = accountUtils.getGitHubClient();

        loadingIndicator = (ProgressBar) view.findViewById(R.id.pb_loading_indicator);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srl_repo_home);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO
            }
        });
        // TODO set on click listeners
        TextView textViewRepoOwner = (TextView) view.findViewById(R.id.tv_repo_owner);
        textViewRepoOwner.setText(repository.getOwner().getLogin());
        TextView textViewRepoName = (TextView) view.findViewById(R.id.tv_repo_name);
        textViewRepoName.setText(repository.getName());
        TextView textViewRepoDescription = (TextView) view.findViewById(R.id.tv_repo_description);
        if (repository.getDescription() != null) {
            textViewRepoDescription.setText(repository.getDescription());
        }

        setHasOptionsMenu(true);

        new GetReadMe().execute();
        new GetWidgetData().execute();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // TODO refresh
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class GetReadMe extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            ContentsService contentsService = new ContentsService(gitHubClient);
            MarkdownService markdownService = new MarkdownService(gitHubClient);
            String formattedReadme;

            try {
                RepositoryContents readme = contentsService.getReadme(repository);

                String decodedReadme = readme.getContent();
                byte[] data = Base64.decode(decodedReadme, Base64.DEFAULT);
                String encodedReadme = new String(data);

                formattedReadme = markdownService.getRepositoryHtml(repository, encodedReadme);
            } catch (Exception e) {
                return null;
            }
            return formattedReadme;
        }

        @SuppressWarnings("deprecation") // for Html.fromHtml -- check in code for android version
        @Override
        protected void onPostExecute(String readme) {
            super.onPostExecute(readme);
            TextView textViewReadme = (TextView) rootView.findViewById(R.id.tv_readme);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                textViewReadme.setText(Html.fromHtml(readme, Html.FROM_HTML_MODE_COMPACT));
            } else {
                textViewReadme.setText(Html.fromHtml(readme));
            }
        }
    }

    private class GetWidgetData extends AsyncTask<Void, Void, Boolean> {

        private int pullRequestCount;
        private int issuesCount;
        private int forksCount;

        @Override
        protected Boolean doInBackground(Void... params) {
            PullRequestService pullRequestService = new PullRequestService(gitHubClient);

            try {
                pullRequestCount = pullRequestService.getPullRequests(repository, PullRequestService.PR_STATE).size();
                issuesCount = repository.getOpenIssues() - pullRequestCount;
                forksCount = repository.getForks();
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
            TextView textViewIssuesCount = (TextView) rootView.findViewById(R.id.tv_issues_count);
            TextView textViewPRCount = (TextView) rootView.findViewById(R.id.tv_pull_request_count);
            TextView textViewForksCount = (TextView) rootView.findViewById(R.id.tv_forks_count);

            textViewIssuesCount.setText(String.valueOf(issuesCount));
            textViewPRCount.setText(String.valueOf(pullRequestCount));
            textViewForksCount.setText(String.valueOf(forksCount));
        }
    }
}
