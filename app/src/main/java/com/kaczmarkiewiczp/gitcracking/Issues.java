package com.kaczmarkiewiczp.gitcracking;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.MenuItem;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.kaczmarkiewiczp.gitcracking.fragment.IssuesFragment;

import org.eclipse.egit.github.core.client.GitHubClient;

public class Issues extends AppCompatActivity {

    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private TabLayout tabLayout;

    private AccountUtils accountUtils;
    private GitHubClient gitHubClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issues);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Issues");
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.container);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        new NavBarUtils(this, toolbar, 3);
        accountUtils = new AccountUtils(this);
        gitHubClient = accountUtils.getGitHubClient();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
                findViewById(R.id.action_refresh).startAnimation(rotate);
        }
        return super.onOptionsItemSelected(item);
    }

    class PagerAdapter extends FragmentPagerAdapter {
        private String tabTitles[] = new String[] {"CREATED", "ASSIGNED"};

        public PagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
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
                    return issuesFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }
}
