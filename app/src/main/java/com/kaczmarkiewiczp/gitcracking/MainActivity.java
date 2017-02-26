package com.kaczmarkiewiczp.gitcracking;

import android.content.Context;
import android.os.AsyncTask;
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
    }

    private void shake(EditText element) {
        Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        element.startAnimation(shake);
    }
}
