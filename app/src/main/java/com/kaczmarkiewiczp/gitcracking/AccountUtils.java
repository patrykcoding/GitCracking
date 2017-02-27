package com.kaczmarkiewiczp.gitcracking;


import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

class AccountUtils {

    static void addAuthentication(Context context, String token, String username) {
        // TODO pref name should be a public constant in pref class
        SharedPreferences sharedPreferences = context.getSharedPreferences("GitCrackingPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // TODO create interface constants for 'token' and 'login'
        editor.putString("token", token);
        editor.putString("login", username);
        editor.apply();
    }

    static String getToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("GitCrackingPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("token", "");
    }

    static String getLogin(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("GitCrackingPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("login", "");
    }

    static boolean isAuth(Context context) {
        return !getLogin(context).isEmpty() && !getToken(context).isEmpty();
    }
}
