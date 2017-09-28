package com.kaczmarkiewiczp.gitcracking;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MenuItem;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.kaczmarkiewiczp.gitcracking.fragment.IssuesFragment;

import org.eclipse.egit.github.core.Repository;

/*
 * Issues class showing all issues or issues for a specific repository
 */
public class Issues extends AppCompatActivity implements IssuesFragment.IssueCountListener, IssuesFragment.IssueChangeListener {

    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private NavBarUtils navBarUtils;
    private Repository repository;
    private boolean dataHasBeenModified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issues);
        dataHasBeenModified = false;

        // repository is passed in when a specific repository starts this activity
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            repository = (Repository) bundle.getSerializable(Consts.REPOSITORY_ARG);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.issues));
        setSupportActionBar(toolbar);
        // if this is all issues screen then Issues should be selected in the nav drawer
        if (repository == null) {
            navBarUtils = new NavBarUtils(this, toolbar, NavBarUtils.ISSUES);
        } else {
            navBarUtils = new NavBarUtils(this, toolbar, NavBarUtils.NO_SELECTION);
        }
        if (getIntent().getBooleanExtra(Consts.HAS_PARENT, false)) {
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
                Intent intent = new Intent();
                if (repository != null) {
                    Bundle args = new Bundle();
                    args.putSerializable(Consts.REPOSITORY_ARG, repository);
                    intent.putExtras(args);
                }
                intent.setClass(getApplicationContext(), NewIssue.class);
                startActivityForResult(intent, Consts.NEW_ISSUE_INTENT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1: // if we're coming back from ManageAccounts screen, and a user has been deleted then the nav bar needs to be refreshed
                if (resultCode == RESULT_OK) {
                    Boolean accountHasBeenModified = data.getBooleanExtra("accountHasBeenModified", false);
                    if (accountHasBeenModified) {
                        if (repository == null) {
                            navBarUtils = new NavBarUtils(this, toolbar, NavBarUtils.ISSUES);
                        } else {
                            navBarUtils = new NavBarUtils(this, toolbar, NavBarUtils.NO_SELECTION);
                        }
                    }
                }
                return;
            // after returning to this screen from a specific issue, reload if issue has been modified
            case Consts.NEW_ISSUE_INTENT:
                if (resultCode == Consts.DATA_MODIFIED) {
                    dataHasBeenModified = true;
                    for (int i = 0; i < pagerAdapter.getCount(); i++) {
                        IssuesFragment fragment = pagerAdapter.getFragment(i);
                        if (fragment != null) {
                            fragment.reloadFragment();
                        }
                    }
                }
                return;
        }
    }

    @Override
    public void onBackPressed() {
        if (dataHasBeenModified) {
            setResult(Consts.DATA_MODIFIED);
            finish();
        } else {
            super.onBackPressed();
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
        dataHasBeenModified = true;
        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            IssuesFragment fragment = pagerAdapter.getFragment(i);
            fragment.reloadFragment();
        }
    }

    private class PagerAdapter extends FragmentPagerAdapter {
        private String tabStrings[] = new String[] {getString(R.string.created_tab), getString(R.string.assigned_tab)};
        private String tabTitles[] = new String[] {getString(R.string.created_tab), getString(R.string.assigned_tab)};
        private SparseArray<IssuesFragment> fragments;

        PagerAdapter(FragmentManager fragmentManager) {
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
                    if (repository != null) {
                        args.putSerializable(Consts.REPOSITORY_ARG, repository);
                    }
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

        void setTabBadge(int position, int count) {
            String title = tabStrings[position] + " (" + count + ")";
            tabTitles[position] = title;
            notifyDataSetChanged();
        }

        IssuesFragment getFragment(int position) {
            return fragments.get(position);
        }
    }
}
