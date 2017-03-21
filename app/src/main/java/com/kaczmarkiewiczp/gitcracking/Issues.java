package com.kaczmarkiewiczp.gitcracking;

import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.kaczmarkiewiczp.gitcracking.adapter.IssuesAdapter;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Issues extends AppCompatActivity {


    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private AccountUtils accountUtils;
    private GitHubClient gitHubClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issues);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Issues");
        setSupportActionBar(toolbar);

        new NavBarUtils(this, toolbar, 3);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        accountUtils = new AccountUtils(this);
        gitHubClient = accountUtils.getGitHubClient();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
                findViewById(R.id.action_refresh).startAnimation(rotate);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class IssuesFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";
        private RecyclerView recyclerView;
        private IssuesAdapter issuesAdapter;
        private SwipeRefreshLayout swipeRefreshLayout;
        private AccountUtils accountUtils;

        public IssuesFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static IssuesFragment newInstance(int sectionNumber) {
            IssuesFragment fragment = new IssuesFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_issues, container, false);

            recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_issues);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(true);
            issuesAdapter = new IssuesAdapter(getContext());
            recyclerView.setAdapter(issuesAdapter);
            recyclerView.setVisibility(View.VISIBLE);
            swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srl_issues);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // TODO (1) stop running background tasks, (2) refresh
                }
            });
            accountUtils = new AccountUtils(getContext());
            GitHubClient gitHubClient = accountUtils.getGitHubClient();

            GetIssues backgroundTask = new GetIssues(issuesAdapter);
            backgroundTask.execute(gitHubClient);
            return rootView;
        }

        public class GetIssues extends AsyncTask<GitHubClient, Void, Void> {

            ArrayList<Issue> issues;
            IssuesAdapter issuesAdapter;

            public GetIssues(IssuesAdapter adapter) {
                issuesAdapter = adapter;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                issues = new ArrayList<>();
                // TODO show progressbar
            }

            @Override
            protected Void doInBackground(GitHubClient... params) {
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                issuesAdapter.setIssues(issues);
                issuesAdapter.updateView();
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a IssuesFragment (defined as a static inner class below).
            return IssuesFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "CREATED";
                case 1:
                    return "ASSIGNED";
            }
            return null;
        }
    }
}
