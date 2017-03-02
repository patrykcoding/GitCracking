package com.kaczmarkiewiczp.gitcracking;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;

public class Dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_dashboard_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Dashboard");
        setSupportActionBar(toolbar);
        setupNavigationDrawer(toolbar);
    }

    private void setupNavigationDrawer(Toolbar toolbar) {
        new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Dashboard").withIcon(getDrawable(R.drawable.ic_home_black_24dp)),
                        new PrimaryDrawerItem().withName("Repositories").withIcon(getDrawable(R.drawable.ic_octicon_repo)),
                        new PrimaryDrawerItem().withName("Issues").withIcon(getDrawable(R.drawable.ic_octicon_issue_24dp)),
                        new PrimaryDrawerItem().withName("Pull Requests").withIcon(getDrawable(R.drawable.ic_octicon_git_pull_request)),
                        new PrimaryDrawerItem().withName("People").withIcon(getDrawable(R.drawable.ic_people_black_24dp)),
                        new PrimaryDrawerItem().withName("Bookmarks").withIcon(getDrawable(R.drawable.ic_star_black_24dp)),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("Settings")
                )
                .build();
    }
}
