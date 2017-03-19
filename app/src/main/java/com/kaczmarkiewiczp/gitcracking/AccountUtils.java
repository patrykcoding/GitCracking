package com.kaczmarkiewiczp.gitcracking;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.EventService;
import org.eclipse.egit.github.core.service.GitHubService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MarkdownService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

class AccountUtils {


    private Context context;
    private GitHubClient gitHubClient;
    private String token;
    private String login;

    public AccountUtils(Context context, String username, Authorization auth) {
        this.context = context;
        this.login = username;
        this.token = auth.getToken();
        setToken(token);
        setLogin(username);

        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(this.token);
    }

    public AccountUtils(Context context) {
        this.context = context;
        SharedPreferences sharedPreferences = context.getSharedPreferences("GitCrackingPrefs", MODE_PRIVATE);
        this.token = sharedPreferences.getString("token", "");
        this.login = sharedPreferences.getString("login", "");
        Log.i("#ACCOUNTUTILS", token);
        gitHubClient = new GitHubClient();
        gitHubClient.setOAuth2Token(token);
    }

    public String getToken() {
        return token;
    }

    public GitHubClient getGitHubClient() {
        return gitHubClient;
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
        Set<String> currentUserList = sharedPreferences.getStringSet("accounts", new HashSet<String>());
        if (!login.isEmpty() && !currentUserList.contains(login)) {
            Set<String> newUserList = new HashSet<>(currentUserList);
            newUserList.add(login);
            editor.putStringSet("accounts", newUserList);
        }
        editor.putString("login", login);
        editor.putString("defaultUser", login);
        editor.apply();
    }

    public static Set<String> getAccounts(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("GitCrackingPrefs", MODE_PRIVATE);
        return sharedPreferences.getStringSet("accounts", new HashSet<String>());
    }

    public static void logout(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("GitCrackingPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", "");
        editor.putString("login", "");
        editor.putString("defaultUser", "");
        editor.apply();
    }

    public static boolean isAuth(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("GitCrackingPrefs", MODE_PRIVATE);
        String savedToken = sharedPreferences.getString("token", "");
        String savedLogin = sharedPreferences.getString("login", "");
        return !savedToken.isEmpty() && !savedLogin.isEmpty();
    }

}
