package com.kaczmarkiewiczp.gitcracking;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.kaczmarkiewiczp.gitcracking.adapter.AccountsAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.Set;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class ManageAccounts extends AppCompatActivity implements AccountsAdapter.ListItemClickListener {

    private AccountUtils accountUtils;
    private FastScrollRecyclerView recyclerView;
    private AccountsAdapter accountsAdapter;
    private Boolean accountHasBeenModified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_accounts);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.manage_accounts));
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

        accountHasBeenModified = false;
        showAccounts();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("accountHasBeenModified", accountHasBeenModified);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAccounts() {
        Set<String> accounts = accountUtils.getAccounts();

        for (String user : accounts) {
            String name = accountUtils.getUserName(user);
            String iconUrl = accountUtils.getUserIconUrl(user);
            Boolean canRemove;
            if (accountUtils.accountsCount() == 1) {
                canRemove = true;
            } else {
                canRemove = !accountUtils.getUserLogin().equals(user);
            }
            accountsAdapter.addUser(user, name, iconUrl, canRemove);
        }
    }

    @Override
    public void onListItemClick(String userName) {
        final String user = userName;
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_dialog_alert_black_24dp)
                .setTitle(getString(R.string.confirm_deletion))
                .setMessage(getString(R.string.are_you_sure_delete_user))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeUser(user);
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .setCancelable(false)
                .show();
    }

    private void removeUser(String userName) {
        accountUtils.removeUser(userName);
        accountsAdapter.removeUser(userName);
        accountHasBeenModified = true;

        // if there's only one user left we have to refresh the list to allow for signing out for that user
        // and if there are no other users, send to main activity/sign in screen
        if (accountUtils.accountsCount() == 1) {
            accountsAdapter.removeUsers();
            showAccounts();
        } else if (accountUtils.accountsCount() < 1) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
