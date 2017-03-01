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
                        new PrimaryDrawerItem().withName("Dashboard"),
                        new PrimaryDrawerItem().withName("Repositories"),
                        new PrimaryDrawerItem().withName("Issues"),
                        new PrimaryDrawerItem().withName("Pull Requests"),
                        new PrimaryDrawerItem().withName("People"),
                        new PrimaryDrawerItem().withName("Bookmarks"),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("Settings")
                )
                .build();
    }
}
