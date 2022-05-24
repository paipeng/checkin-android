package com.paipeng.checkin.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.paipeng.checkin.data.model.LoggedInUser;

public class CommonUtil {
    private static final String TAG = CommonUtil.class.getSimpleName();
    private static final String SHARED_PREFS = "sharedPrefs";

    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String LOGIN_USER = "LOGIN_USER";
    public static final String USER_TOKEN = "USER_TOKEN";

    public static final String SERVER_URL = "http://114.115.137.22";

    public static boolean isUserExists(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LOGIN_USER, null) != null;
    }

    public static LoggedInUser getLoggedInUser(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String data = sharedPreferences.getString(LOGIN_USER, null);
        Log.d(TAG, "getLoggedInUser: " + data);
        if (data != null) {
            Gson gson = new Gson();
            return gson.fromJson(data, LoggedInUser.class);
        } else {
            return null;
        }
    }

    public static void saveUser(Activity activity, LoggedInUser user) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(user);
        Log.d(TAG, "saveUser: " + json);
        editor.putString(LOGIN_USER, json);
        editor.commit();
        editor.apply();
    }

    public static LoggedInUser verifyLoggedInUser(Activity activity, LoggedInUser user) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String data = sharedPreferences.getString(LOGIN_USER, null);
        if (data != null) {
            Gson gson = new Gson();
            LoggedInUser loggedInUser =  gson.fromJson(data, LoggedInUser.class);
            if (user.getUsername().equals(loggedInUser.getUsername()) && user.getPassword().equals(loggedInUser.getPassword())) {
                return loggedInUser;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String getUserToken(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(USER_TOKEN, null);
    }

    public static void setUserToken(Activity activity, String token) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Log.d(TAG, "setUserToken: " + token);
        editor.putString(USER_TOKEN, token);
        editor.commit();
        editor.apply();

    }

}
