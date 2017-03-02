package com.kaczmarkiewiczp.gitcracking;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class Dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_dashboard_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Dashboard");
        setSupportActionBar(toolbar);
        UiUtils.setupNavigationDrawer(this, toolbar);
    }
}
