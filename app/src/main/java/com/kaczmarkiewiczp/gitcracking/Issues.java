package com.kaczmarkiewiczp.gitcracking;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.kaczmarkiewiczp.gitcracking.fragment.IssuesFragment;

import org.eclipse.egit.github.core.client.GitHubClient;

public class Issues extends AppCompatActivity implements IssuesFragment.IssueCountListener, IssuesFragment.IssueChangeListener {

    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private NavBarUtils navBarUtils;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issues);
        context = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Issues");
        setSupportActionBar(toolbar);
        navBarUtils = new NavBarUtils(this, toolbar, NavBarUtils.ISSUES);
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

        findViewById(R.id.fab_new_issue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewIssue.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Boolean accountHasBeenModified = data.getBooleanExtra("accountHasBeenModified", false);
            if (accountHasBeenModified) {
                navBarUtils = new NavBarUtils(this, toolbar, NavBarUtils.ISSUES);
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
    public void onIssueCountHasChanged(int tabSection, int count) {
        pagerAdapter.setTabBadge(tabSection, count);
    }

    @Override
    public void onIssueDataHasChanged(boolean dataHasChanged) {
        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            IssuesFragment fragment = pagerAdapter.getFragment(i);
            fragment.reloadFragment();
        }
    }

    class PagerAdapter extends FragmentPagerAdapter {
        private String tabStrings[] = new String[] {"CREATED", "ASSIGNED"};
        private String tabTitles[] = new String[] {"CREATED", "ASSIGNED"};
        private SparseArray<IssuesFragment> fragments;

        public PagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            fragments = new SparseArray<>();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                case 1: // fall through
                    IssuesFragment issuesFragment = new IssuesFragment();
                    Bundle args = new Bundle();
                    args.putInt(issuesFragment.ARG_SECTION_NUMBER, position);
                    issuesFragment.setArguments(args);
                    fragments.put(position, issuesFragment);
                    return issuesFragment;
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

        public IssuesFragment getFragment(int position) {
            return fragments.get(position);
        }
    }
}
