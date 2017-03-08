package com.kaczmarkiewiczp.gitcracking;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;

import static android.content.Context.MODE_PRIVATE;

class AccountUtils {

    private Context context;
    private String token;
    private String login;

    public AccountUtils(Context context, Authorization auth) {
        this.context = context;
        this.token = auth.getToken();
        setToken(token);

        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(this.token);
        UserService userService = new UserService(client);
        new GetLoginFromGitHub().execute(userService);
    }

    private void setToken(String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("GitCrackingPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    private void setLogin(String login) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("GitCrackingPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("login", login);
        editor.apply();
    }

    public static boolean isAuth(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("GitCrackingPrefs", MODE_PRIVATE);
        String savedToken = sharedPreferences.getString("token", "");
        String savedLogin = sharedPreferences.getString("login", "");
        return !savedToken.isEmpty() && !savedLogin.isEmpty();
    }

    public class GetLoginFromGitHub extends AsyncTask<UserService, Void, User> {

        @Override
        protected User doInBackground(UserService... params) {
            UserService userService = params[0];
            try {
                User user = userService.getUser();
                return user;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            if (user == null)
                return;
            setLogin(user.getLogin());
        }
    }
}
