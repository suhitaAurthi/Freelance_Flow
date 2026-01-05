package com.example.freelanceflowandroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class congratulationAct extends AppCompatActivity {
    private static final String TAG = "congratulationAct";
    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_USER_TYPE = "user_type";
    private static final long DELAY_MS = 2_000L; // changed to 2 seconds for faster navigation during testing

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable navigateRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                // Prefer centralized PrefsManager role, fall back to legacy key
                String userType = PrefsManager.getInstance(congratulationAct.this).getUserRole();
                if (userType == null) {
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    userType = prefs.getString(KEY_USER_TYPE, null);
                }
                Log.d(TAG, "congrats -> userType=" + userType);

                if (DashboardClient.ROLE_FREELANCER.equals(userType) || "freelancer".equals(userType)) {
                    Intent i = new Intent(congratulationAct.this, freelancerDetail.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } else if (DashboardClient.ROLE_CLIENT.equals(userType) || "client".equals(userType)) {
                    Intent i = new Intent(congratulationAct.this, clientDetail.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } else {
                    Intent i = new Intent(congratulationAct.this, RoleChoice.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error navigating after congratulations", e);
            } finally {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.congo_scene);
        Log.d(TAG, "Showing congratulations for " + (DELAY_MS / 1000) + "s");
        mainHandler.postDelayed(navigateRunnable, DELAY_MS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacks(navigateRunnable);
    }
}