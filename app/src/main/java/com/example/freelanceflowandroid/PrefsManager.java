package com.example.freelanceflowandroid;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;

@SuppressWarnings("unused")
public class PrefsManager {
    private static final String PREFS_NAME = "freelanceflow_prefs_v1";
    private static final String KEY_USER_ROLE = "key_user_role";
    private static final String KEY_AUTH_TOKEN = "key_auth_token";
    private static final String KEY_USER_ID = "key_user_id"; // legacy numeric id (long)
    private static final String KEY_USER_TEAM_ID = "key_user_team_id"; // added earlier
    private static final String KEY_UID = "key_uid"; // Firebase UID (string)

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

    // legacy numeric id helpers (keep if your backend uses numeric ids)
    public void saveUserId(long id) {
        prefs.edit().putLong(KEY_USER_ID, id).apply();
    }
    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    // Firebase UID (string) helpers
    public void saveUserUid(@Nullable String uid) {
        if (uid == null) prefs.edit().remove(KEY_UID).apply();
        else prefs.edit().putString(KEY_UID, uid).apply();
    }

    @Nullable
    public String getUserUid() {
        return prefs.getString(KEY_UID, null);
    }

    // --- team id helpers ---
    public void saveUserTeamId(@Nullable String teamId) {
        if (teamId == null) prefs.edit().remove(KEY_USER_TEAM_ID).apply();
        else prefs.edit().putString(KEY_USER_TEAM_ID, teamId).apply();
    }

    @Nullable
    public String getUserTeamId() {
        return prefs.getString(KEY_USER_TEAM_ID, null);
    }

    public boolean isUserInTeam() {
        return getUserTeamId() != null;
    }

    public void clearUserTeamId() {
        prefs.edit().remove(KEY_USER_TEAM_ID).apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}