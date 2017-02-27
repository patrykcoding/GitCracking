package com.kaczmarkiewiczp.gitcracking;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;
import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.OAuthService;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import xdroid.toaster.Toaster;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*
     * This will be called using the xml onClick attribute
     */
    public void loginButtonClicked(View view) {
        final EditText usernameView = (EditText) findViewById(R.id.et_username);
        final EditText passwordView = (EditText) findViewById(R.id.et_password);
        String username = usernameView.getText().toString().trim();
        String password = passwordView.getText().toString();

        Context context = getApplicationContext();
        if (username.length() == 0) {
            shake(usernameView);
            Toast toast = Toast.makeText(context, "Please provide your username", Toast.LENGTH_SHORT);
            toast.show();
            return;
        } else if (password.length() == 0) {
            shake(passwordView);
            Toast toast = Toast.makeText(context, "Please provide your password", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        new LoginTask().execute(username, password);
    }

    /*
     * Animate an element
     */
    private void shake(EditText element) {
        Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        element.startAnimation(shake);
    }

    public class LoginTask extends AsyncTask<String, Void, Authorization> {

        private String username;
        private String password;

        @Override
        protected Authorization doInBackground(String... credentials) {
            this.username = credentials[0];
            this.password = credentials[1];
            GitHubClient client = new GitHubClient();

            String appName = getResources().getString(R.string.app_name);
            client.setCredentials(username, password);
            client.setUserAgent(appName);
            String description = appName + " - " + Build.MANUFACTURER + " " + Build.MODEL;

            OAuthService authService = new OAuthService(client);

            try {
                for (Authorization authorization : authService.getAuthorizations()) {
                    String note = authorization.getNote();

                    if (note != null && note.startsWith(appName)) {
                        authService.deleteAuthorization(authorization.getId());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Authorization auth = new Authorization();
            auth.setNote(description);
            auth.setUrl("https://github.com/kaczmarkiewiczp/GitCracking"); // TODO strings.xml
            auth.setScopes(Arrays.asList("user", "repo", "gist"));

            try {
                return authService.createAuthorization(auth);
            } catch (IOException e) {
                e.printStackTrace();
                // TODO check for two factor authentication
                return null;
            }
        }

        @Override
        protected void onPostExecute(Authorization authorization) {
            if (authorization == null) {
                // TODO
                return;
            }

            // TODO pref name should be a public constant in pref class
            SharedPreferences sharedPreferences = getSharedPreferences("GitCrackingPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            // TODO create interface constants for 'token' and 'login'
            editor.putString("token", authorization.getToken());
            editor.putString("login", username);
            editor.apply();
        }
    }
}
