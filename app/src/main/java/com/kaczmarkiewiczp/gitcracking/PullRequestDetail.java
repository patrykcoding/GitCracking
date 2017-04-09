package com.kaczmarkiewiczp.gitcracking;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.kaczmarkiewiczp.gitcracking.fragment.CommitsFragment;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;

public class PullRequestDetail extends AppCompatActivity {

    private PullRequest pullRequest;
    private Repository repository;
    private Context context;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private NavBarUtils navBarUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_request_detail);
        Bundle bundle = getIntent().getExtras();
        pullRequest = (PullRequest) bundle.getSerializable("pull request");
        repository = (Repository) bundle.getSerializable("repository");
        if (pullRequest == null || repository == null) {
            // something really bad happened -- return
            finish();
            return;
        }
        context = this;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        String toolbarTitle = "Pull Request #" + String.valueOf(pullRequest.getNumber());
        toolbar.setTitle(toolbarTitle);
        setSupportActionBar(toolbar);
        NavBarUtils navBarUtils = new NavBarUtils(this, toolbar, NavBarUtils.NO_SELECTION);
        navBarUtils.setNavigationDrawerButtonAsUp();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        navBarUtils.killAllActivitiesOnNewActivityStart(true);

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

    class PagerAdapter extends FragmentPagerAdapter {

        private String tabTitles[] = new String[] {"CONVERSATION", "COMMITS", "CHANGES"};

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    //return null;
                case 2: // TODO temp
                case 1:
                    CommitsFragment commitsFragment = new CommitsFragment();
                    Bundle args = new Bundle();
                    args.putSerializable("pull request", pullRequest);
                    args.putSerializable("repository", repository);
                    commitsFragment.setArguments(args);
                    return commitsFragment;
                //case 2:
                    //return null;
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
    }
}
