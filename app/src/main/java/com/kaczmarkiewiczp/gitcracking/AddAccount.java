package com.kaczmarkiewiczp.gitcracking;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.OAuthService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.Arrays;

import static xdroid.toaster.Toaster.toast;

/*
 * Adding new accounts
 */
public class AddAccount extends AppCompatActivity {

    private Context context;
    private AccountUtils accountUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;

        accountUtils = new AccountUtils(context);

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        toolbar.setTitle("GitCracking");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /* listen for the login button press on keyboard */
        EditText editText = (EditText) findViewById(R.id.et_password);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == 0 || actionId == EditorInfo.IME_ACTION_GO) {
                    loginButtonClicked(v.getRootView());
                    return true;
                }
                return false;
            }
        });
        Button loginButton = (Button) findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButtonClicked(v);
            }
        });
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

    public void loginButtonClicked(View view) {
        final AutoCompleteTextView etUsername = (AutoCompleteTextView) findViewById(R.id.et_username);
        final EditText etPassword = (EditText) findViewById(R.id.et_password);
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        Context context = getApplicationContext();

        if (username.length() == 0) {
            etUsername.requestFocus();
            Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.wiggle);
            findViewById(R.id.username_layout).startAnimation(shake);
            Toast toast = Toast.makeText(context, context.getString(R.string.please_provide_username), Toast.LENGTH_SHORT);
            toast.show();
            return;
        } else if (password.length() == 0) {
            etPassword.requestFocus();
            Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.wiggle);
            findViewById(R.id.password_layout).startAnimation(shake);
            Toast toast = Toast.makeText(context, context.getString(R.string.please_provide_password), Toast.LENGTH_SHORT);
            toast.show();
            return;
        } else if (accountUtils.isAlreadyAUser(username)) {
            userExists();
            etUsername.setText("");
            etPassword.setText("");
            return;
        }
        new LoginTask().execute(username, password);
    }

    private void userExists() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(context.getString(R.string.user_already_exists));
        alertDialog.setCancelable(true);
        alertDialog.setNeutralButton(context.getString(R.string.dismiss), null);
        alertDialog.show();
    }

    private void failedLogin() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(context.getString(R.string.login_failed));
        alertDialog.setCancelable(true);
        alertDialog.setNeutralButton(context.getString(R.string.dismiss), null);
        alertDialog.setPositiveButton(context.getString(R.string.forgot_password),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri webpage = Uri.parse(context.getString(R.string.github_reset_password_url));
                        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                });
        alertDialog.show();
    }

    /*
     * Authenticate the user using GitHub API
     */
    public class LoginTask extends AsyncTask<String, Void, Authorization> {

        private String userLogin;
        private String userName;
        private String userIconUrl;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle(context.getString(R.string.authenticating));
            progressDialog.setMessage(context.getString(R.string.please_wait));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected Authorization doInBackground(String... credentials) {
            String username = credentials[0];
            String password = credentials[1];
            GitHubClient client = new GitHubClient();

            String appName = getResources().getString(R.string.app_name);
            client.setCredentials(username, password);
            client.setUserAgent(appName);
            String description = appName + " - " + Build.MANUFACTURER + " " + Build.MODEL;

            OAuthService authService = new OAuthService(client);

            // GitHub requires authorizations to have a unique note
            // Go through user's authorizations, and delete any authorization that contain our note
            try {
                for (Authorization authorization : authService.getAuthorizations()) {
                    String note = authorization.getNote();

                    if (note != null && note.equals(description)) {
                        authService.deleteAuthorization(authorization.getId());
                    }
                }
            } catch (IOException e) {
                // TODO no internet
                return null;
            }

            Authorization auth = new Authorization();
            auth.setNote(description);
            auth.setUrl(getResources().getString(R.string.gitcracking_repo));
            auth.setScopes(Arrays.asList("user", "repo", "gist"));

            Authorization authorization;
            try {
                authorization = authService.createAuthorization(auth);
            } catch (IOException e) {
                e.printStackTrace();
                // TODO check for two factor authentication
                return null;
            }
            client.setOAuth2Token(authorization.getToken());
            UserService userService = new UserService(client);
            try {
                User user = userService.getUser();
                userLogin = user.getLogin();
                userName = user.getName();
                userIconUrl = user.getAvatarUrl();
            } catch (IOException e) {
                return null;
            }
            return authorization;
        }

        @Override
        protected void onPostExecute(Authorization authorization) {
            progressDialog.dismiss();

            if (authorization == null) {
                failedLogin();
                return;
            }
            new AccountUtils(context, userLogin, userName, userIconUrl, authorization);

            Intent intent = new Intent(getApplication(), Dashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
