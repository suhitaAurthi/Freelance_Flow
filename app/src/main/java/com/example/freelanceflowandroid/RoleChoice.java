package com.example.freelanceflowandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;


public class RoleChoice extends AppCompatActivity {

    private static final String TAG = "RoleChoice";

    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton buttonClient;
    private MaterialButton buttonFreelancer;
    private ImageView imageView;
    private TextView descriptionText;
    private TextView loginText;
    private Button createAccountButton;

    public static final String ROLE_CLIENT = "client";
    public static final String ROLE_FREELANCER = "freelancer";

    private String selectedRole = ROLE_CLIENT; // default
    private boolean initializing = true; // suppress listener while setting default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.role_activity); // update if your layout filename differs

        try {
            toggleGroup = findViewById(R.id.toggleButton);
            buttonClient = findViewById(R.id.button1);
            buttonFreelancer = findViewById(R.id.button2);
            imageView = findViewById(R.id.imageView);
            descriptionText = findViewById(R.id.textView11);
            loginText = findViewById(R.id.textView10);
            createAccountButton = findViewById(R.id.button4);

            // Basic null-checks to surface layout mismatches early
            if (toggleGroup == null || buttonClient == null || buttonFreelancer == null
                    || imageView == null || descriptionText == null || loginText == null
                    || createAccountButton == null) {
                Log.e(TAG, "One or more views are null. Check your layout IDs and setContentView.");
                Toast.makeText(this, "UI not initialized correctly. Check layout IDs (see Logcat).", Toast.LENGTH_LONG).show();
                return;
            }

            // Ensure single-selection behaviour
            toggleGroup.setSingleSelection(true);

            // Load saved role from PrefsManager (if any)
            String saved = PrefsManager.getInstance(this).getUserRole();
            if (ROLE_FREELANCER.equals(saved)) {
                selectedRole = ROLE_FREELANCER;
            } else {
                selectedRole = ROLE_CLIENT;
            }

            // Apply initial state without firing listener
            initializing = true;
            if (ROLE_FREELANCER.equals(selectedRole)) {
                toggleGroup.check(R.id.button2);
            } else {
                toggleGroup.check(R.id.button1);
            }
            applyRole(selectedRole);
            initializing = false;

            // Listen for toggles
            toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
                @Override
                public void onButtonChecked(MaterialButtonToggleGroup group, @IdRes int checkedId, boolean isChecked) {
                    if (initializing) return;
                    if (!isChecked) return; // only handle checked events
                    if (checkedId == R.id.button1) {
                        applyRole(ROLE_CLIENT);
                    } else if (checkedId == R.id.button2) {
                        applyRole(ROLE_FREELANCER);
                    }
                }
            });

            // Create account -> launch the appropriate registration screen
            createAccountButton.setOnClickListener(v -> {
                try {
                    if (ROLE_CLIENT.equals(selectedRole)) {
                        startActivity(new Intent(RoleChoice.this, RegisterClientActivity.class));
                    } else {
                        startActivity(new Intent(RoleChoice.this, RegisterFreelancerActivity.class));
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to start registration activity", ex);
                    Toast.makeText(RoleChoice.this, "Registration screen not found. Check AndroidManifest.", Toast.LENGTH_SHORT).show();
                }
            });

            // Login link -> open LoginActivity
            loginText.setClickable(true);
            loginText.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(RoleChoice.this, LoginActivity.class));
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to start LoginActivity", ex);
                    Toast.makeText(RoleChoice.this, "Login screen not found.", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Throwable t) {
            Log.e(TAG, "Startup exception in RoleChoice.onCreate", t);
            Toast.makeText(this, "Startup error: check Logcat for details.", Toast.LENGTH_LONG).show();
        }
    }

    private void applyRole(String role) {
        selectedRole = role;
        // persist selection using your PrefsManager
        PrefsManager.getInstance(this).saveUserRole(selectedRole);

        if (ROLE_CLIENT.equals(role)) {
            // update UI for client (ensure drawable exists)
            imageView.setImageResource(R.drawable.client1);
            descriptionText.setText("Find, manage, pay talent — all in one place!");
            // optionally update visual states for the buttons (e.g., selected color)
            buttonClient.setChecked(true);
            buttonFreelancer.setChecked(false);
        } else {
            imageView.setImageResource(R.drawable.free1);
            descriptionText.setText("Find projects, get hired and get paid!");
            buttonClient.setChecked(false);
            buttonFreelancer.setChecked(true);
        }
        // Do NOT call toggleGroup.check(...) here — that would re-trigger the listener
    }
}