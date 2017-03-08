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

import static android.content.Context.MODE_PRIVATE;

class AccountUtils {

    public  static String CLIENT_SERVICE = "github.user";
    public static String LABEL_SERVICE = "github.label";
    public static String ISSUE_SERVICE = "github.issue";
    public static String COMMIT_SERVICE = "github.commit";
    public static String REPO_SERVICE = "github.repository";
    public static String USER_SERVICE = "github.user";
    public static String CONTENTS_SERVICE = "github.contents";
    public static String PULL_SERVICE = "github.pullrequest";
    public static String EVENT_SERVICE = "github.event";
    public static String MARKDOWN_SERVICE = "github.markdown";

    private Context context;
    private String token;
    private String login;
    private HashMap<String, GitHubService> gitHubServices;

    public AccountUtils(Context context, Authorization auth) {
        this.context = context;
        this.token = auth.getToken();
        setToken(token);

        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(this.token);
        setGitHubServices(client);
        UserService userService = (UserService) getGitHubService(USER_SERVICE);
        new GetLoginFromGitHub().execute(userService);
    }

    public AccountUtils(Context context) {
        this.context = context;
        SharedPreferences sharedPreferences = context.getSharedPreferences("GitCrackingPrefs", MODE_PRIVATE);
        this.token = sharedPreferences.getString("token", "");
        this.login = sharedPreferences.getString("login", "");

        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(token);
        setGitHubServices(client);
        UserService userService = (UserService) getGitHubService(USER_SERVICE);
        new GetLoginFromGitHub().execute(userService);
    }

    public String getToken() {
        return token;
    }

    private void setGitHubServices(GitHubClient client) {
        gitHubServices = new HashMap<>();
        gitHubServices.put(CLIENT_SERVICE, new UserService(client));
        gitHubServices.put(LABEL_SERVICE, new LabelService(client));
        gitHubServices.put(ISSUE_SERVICE, new IssueService(client));
        gitHubServices.put(COMMIT_SERVICE, new CommitService(client));
        gitHubServices.put(REPO_SERVICE, new RepositoryService(client));
        gitHubServices.put(USER_SERVICE, new UserService(client));
        gitHubServices.put(CONTENTS_SERVICE, new ContentsService(client));
        gitHubServices.put(PULL_SERVICE, new PullRequestService(client));
        gitHubServices.put(EVENT_SERVICE, new EventService(client));
        gitHubServices.put(MARKDOWN_SERVICE, new MarkdownService(client));
    }

    public GitHubService getGitHubService(String service) {
        return gitHubServices.get(service);
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
        editor.putString("defaultUser", login);
        editor.apply();
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
