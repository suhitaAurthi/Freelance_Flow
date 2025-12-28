package com.example.freelanceflowandroid;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;

public class PrefsManager {
    private static final String PREFS_NAME = "freelanceflow_prefs_v1";
    private static final String KEY_USER_ROLE = "key_user_role";
    private static final String KEY_AUTH_TOKEN = "key_auth_token";
    private static final String KEY_USER_ID = "key_user_id";

    private static PrefsManager instance;
    private final SharedPreferences prefs;

    private PrefsManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new PrefsManager(context);
        }
        return instance;
    }
    public void saveUserRole(String role) {
        prefs.edit().putString(KEY_USER_ROLE, role).apply();
    }

    @Nullable
    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, null);
    }
    public void saveAuthToken(String token) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    @Nullable
    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }
    public void saveUserId(long id) {
        prefs.edit().putLong(KEY_USER_ID, id).apply();
    }
    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }
    public void clearSession() {
        prefs.edit().clear().apply();
    }
}