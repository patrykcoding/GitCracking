package com.kaczmarkiewiczp.gitcracking;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
                findViewById(R.id.action_refresh).startAnimation(rotate);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class RetrieveData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
