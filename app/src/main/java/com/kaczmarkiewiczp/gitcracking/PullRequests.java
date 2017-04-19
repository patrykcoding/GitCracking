package com.kaczmarkiewiczp.gitcracking;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.kaczmarkiewiczp.gitcracking.fragment.PullRequestsFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PullRequests extends AppCompatActivity implements PullRequestsFragment.PullRequestCountListener, PullRequestsFragment.PullRequestChangeListener {

    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private NavBarUtils navBarUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_requests);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Pull Requests");
        setSupportActionBar(toolbar);
        navBarUtils = new NavBarUtils(this, toolbar, NavBarUtils.PULL_REQUESTS);
        if (getIntent().getBooleanExtra("hasParent", false)) {
            navBarUtils.setNavigationDrawerButtonAsUp();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        viewPager = (ViewPager) findViewById(R.id.container);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Boolean accountHasBeenModified = data.getBooleanExtra("accountHasBeenModified", false);
            if (accountHasBeenModified) {
                navBarUtils = new NavBarUtils(this, toolbar, NavBarUtils.PULL_REQUESTS);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
                findViewById(R.id.action_refresh).startAnimation(rotate);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPullRequestCountHasChanged(int tabSection, int count) {
        pagerAdapter.setTabBadge(tabSection, count);
    }

    @Override
    public void onPRDataHasChanged(boolean dataHasChanged) {
        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            PullRequestsFragment fragment = pagerAdapter.getFragment(i);
            fragment.reloadFragmentData();
        }
    }

    class PagerAdapter extends FragmentPagerAdapter {
        private String tabStrings[] = new String[] {"CREATED", "ASSIGNED"};
        private String tabTitles[] = new String[] {"CREATED", "ASSIGNED"};
        private SparseArray<PullRequestsFragment> fragments;

        public PagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            fragments = new SparseArray<>();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                case 1: // fall through
                    PullRequestsFragment pullRequestsFragment = new PullRequestsFragment();
                    Bundle args = new Bundle();
                    args.putInt(pullRequestsFragment.ARG_SECTION_NUMBER, position);
                    pullRequestsFragment.setArguments(args);
                    fragments.put(position, pullRequestsFragment);
                    return pullRequestsFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        public void setTabBadge(int position, int count) {
            String title = tabStrings[position] + " (" + count + ")";
            tabTitles[position] = title;
            notifyDataSetChanged();
        }

        public PullRequestsFragment getFragment(int position) {
            return fragments.get(position);
        }
    }
}
