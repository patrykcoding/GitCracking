package com.kaczmarkiewiczp.gitcracking;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

public class Dashboard extends AppCompatActivity {

    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        loadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_dashboard_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Dashboard");
        setSupportActionBar(toolbar);
        new NavBarUtils(this, toolbar);
        new GitHubDashBoardTask().execute(); // DEBUG
    }

    public class GitHubDashBoardTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            SystemClock.sleep(8000); // DEBUG
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadingIndicator.setVisibility(View.INVISIBLE);
        }
    }
}
