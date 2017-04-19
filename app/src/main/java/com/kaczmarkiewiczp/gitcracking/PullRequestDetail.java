package com.kaczmarkiewiczp.gitcracking;

import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.kaczmarkiewiczp.gitcracking.fragment.CommitsFragment;
import com.kaczmarkiewiczp.gitcracking.fragment.PRDetailFragment;
import com.kaczmarkiewiczp.gitcracking.fragment.PRDiffFragment;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;

public class PullRequestDetail extends AppCompatActivity implements PRDetailFragment.PullRequestChangeListener {

    private PullRequest pullRequest;
    private Repository repository;
    private Context context;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private NavBarUtils navBarUtils;
    private boolean dataHasBeenModified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pr_detail);
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
    public void onBackPressed() {
        if (dataHasBeenModified) {
            setResult(Consts.DATA_MODIFIED);
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Boolean accountHasBeenModified = data.getBooleanExtra("accountHasBeenModified", false);
            if (accountHasBeenModified) {
                navBarUtils = new NavBarUtils(this, toolbar, NavBarUtils.NO_SELECTION);
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
    public void onDataHasBeenModified(boolean dataHasBeenModified) {
        this.dataHasBeenModified = dataHasBeenModified;
    }

    class PagerAdapter extends FragmentPagerAdapter {

        private String tabTitles[] = new String[] {"CONVERSATION", "COMMITS", "CHANGES"};

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putSerializable("pull request", pullRequest);
            args.putSerializable("repository", repository);
            switch (position) {
                case 0:
                    PRDetailFragment prDetailFragment = new PRDetailFragment();
                    prDetailFragment.setArguments(args);
                    return prDetailFragment;
                case 1:
                    CommitsFragment commitsFragment = new CommitsFragment();
                    commitsFragment.setArguments(args);
                    return commitsFragment;
                case 2:
                    PRDiffFragment diffFragment = new PRDiffFragment();
                    diffFragment.setArguments(args);
                    return diffFragment;
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
