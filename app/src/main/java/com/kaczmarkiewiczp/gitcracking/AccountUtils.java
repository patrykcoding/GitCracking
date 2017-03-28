package com.kaczmarkiewiczp.gitcracking;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.client.GitHubClient;

import java.util.HashSet;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class AccountUtils {
    private static final String SHARED_PREFERENCES = "GitCrackingPrefs";

    private Context context;
    private GitHubClient gitHubClient;
    private String token;
    private String userLogin;
    private String userName;
    private String userIconUrl;

    /*
     * Public constructor used when new user is being added
     */
    public AccountUtils(Context context, String userLogin, String userName, String userIconUrl, Authorization authorization) {
        this.context = context;
        this.token = authorization.getToken();
        this.userLogin = userLogin;
        this.userName = userName;
        this.userIconUrl = userIconUrl;
        gitHubClient = new GitHubClient();
        gitHubClient.setOAuth2Token(token);
        addAccount();
    }

    /*
     * Public constructor used when user is already authenticated
     */
    public AccountUtils(Context context) {
        this.context = context;
        retrieveDefaultAccount();
        this.gitHubClient = new GitHubClient();
        gitHubClient.setOAuth2Token(token);
    }

    public GitHubClient getGitHubClient() {
        return this.gitHubClient;
    }

    public String getUserLogin() {
        return this.userLogin;
    }

    public String getUserName(String login) {
        SharedPreferences sp = getSharedPreferences();
        return sp.getString(login + ":name", "");
    }

    public static String getUserIconUrl(Context context, String login) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return sp.getString(login + ":icon", "");
    }

    public static String getCurrentUser(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return sp.getString("defaultUser", "");
    }

    public static Set<String> getAccounts(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        return sp.getStringSet("accounts", new HashSet<String>());
    }

    public static void setDefaultUser(Context context, String userLogin) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString("defaultUser", userLogin);
        editor.apply();
    }

    public Boolean isAlreadyAUser(String userLogin) {
        Set<String> accounts = getAccounts(context);
        return accounts.contains(userLogin);
    }

    public static boolean isAuth(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        String defaultUser = sp.getString("defaultUser", "");
        if (defaultUser.isEmpty()) {
            return false;
        }
        String token = sp.getString(defaultUser + ":token", "");
        return !token.isEmpty();
    }

    private void retrieveDefaultAccount() {
        SharedPreferences sp = getSharedPreferences();
        this.userLogin = sp.getString("defaultUser", "");
        this.token = sp.getString(this.userLogin + ":token", "");
        this.userName = sp.getString(this.userLogin + ":name", "");
        this.userIconUrl = sp.getString(this.userLogin + ":icon", "");
        Log.i("###", userIconUrl);
    }

    private void addAccount() {
        SharedPreferences sp = getSharedPreferences();
        SharedPreferences.Editor editor = sp.edit();

        editor.putString("defaultUser", userLogin);
        editor.putString(userLogin + ":token", token);
        editor.putString(userLogin + ":name", userName);
        editor.putString(userLogin + ":icon", userIconUrl);

        // shared preferences won't detect change if we add to the same Set
        // so we have to create a new one
        Set<String> currentAccounts = sp.getStringSet("accounts", new HashSet<String>());
        if (!currentAccounts.contains(userLogin)) {
            Set<String> newAccounts = new HashSet<>(currentAccounts);
            newAccounts.add(userLogin);
            editor.putStringSet("accounts", newAccounts);
        }
        editor.apply();
    }

    private void updateAccount(String userLogin, String userName, String userIconUrl) {
        SharedPreferences sp = getSharedPreferences();
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(userLogin + ":name", userName);
        editor.putString(userLogin + ":icon", userIconUrl);

        // shared preferences won't detect change if we add to the same Set
        // so we have to create a new one
        Set<String> currentAccounts = sp.getStringSet("accounts", new HashSet<String>());
        if (!currentAccounts.contains(userLogin)) {
            Set<String> newAccounts = new HashSet<>(currentAccounts);
            newAccounts.add(userLogin);
            editor.putStringSet("accounts", newAccounts);
        }
        editor.apply();
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
    }

    // TODO validate users in AsyncTask<>
}
