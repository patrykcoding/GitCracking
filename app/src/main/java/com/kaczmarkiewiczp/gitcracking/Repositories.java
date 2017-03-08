package com.kaczmarkiewiczp.gitcracking;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ProgressBar;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;

public class Repositories extends AppCompatActivity {

    private ProgressBar loadingIndicator;
    private AccountUtils accountUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repositories);
        loadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_repositories_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Repositories");
        setSupportActionBar(toolbar);
        new NavBarUtils(this, toolbar, 2);
        accountUtils = new AccountUtils(this);

        new RetrieveData().execute();
    }

    public class RetrieveData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            //RepositoryService service = (RepositoryService) accountUtils.getGitHubService(AccountUtils.REPO_SERVICE);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i("~~~~~~", "Hey, I'm here");
        }
    }
}
