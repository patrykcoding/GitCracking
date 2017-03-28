package com.kaczmarkiewiczp.gitcracking;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.kaczmarkiewiczp.gitcracking.adapter.AccountsAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Set;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class ManageAccounts extends AppCompatActivity implements AccountsAdapter.ListItemClickListener {

    private AccountUtils accountUtils;
    private FastScrollRecyclerView recyclerView;
    private AccountsAdapter accountsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_accounts);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Manage Accounts");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        accountUtils = new AccountUtils(this);
        recyclerView = (FastScrollRecyclerView) findViewById(R.id.rv_accounts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        accountsAdapter = new AccountsAdapter(this);
        recyclerView.setAdapter(accountsAdapter);
        recyclerView.setItemAnimator(new SlideInUpAnimator());
        recyclerView.getItemAnimator().setAddDuration(1000);
        recyclerView.setVisibility(View.VISIBLE);

        showAccounts();
    }

    private void showAccounts() {
        Set<String> accounts = AccountUtils.getAccounts(this);

        for (String user : accounts) {
            String name = accountUtils.getUserName(user);
            String iconUrl = AccountUtils.getUserIconUrl(this, user);
            Boolean canRemove;
            canRemove = !accountUtils.getUserLogin().equals(user);
            accountsAdapter.addUser(user, name, iconUrl, canRemove);
        }
    }

    @Override
    public void onListItemClick(String userName) {
        Log.i("#ManageAccounts", userName);
    }
}
