package com.kaczmarkiewiczp.gitcracking;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import static xdroid.toaster.Toaster.toast;

/*
 * This is the activity that starts first on app startup.
 * If user is authenticated, it will redirect to main dashboard, otherwise the login screen will
 * be displayed.
 */
public class MainActivity extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        if (AccountUtils.isAuth(context))
            toast("Authenticated"); // TODO debugging
        setContentView(R.layout.activity_main);
    }

    /*
     * This will be called using the xml onClick attribute.
     * User has clicked the login button
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

    /*
     * Authenticate the user using GitHub API
     */
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

            // GitHub requires authorizations to have a unique note
            // Go through user's authorizations, and delete any authorization that contain our note
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
            auth.setUrl(getResources().getString(R.string.gitcracking_repo));
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
            AccountUtils.addAuthentication(context, authorization.getToken(), username);
            toast("Login successful"); // TODO debugging
            // TODO go to dashboard
        }
    }
}
