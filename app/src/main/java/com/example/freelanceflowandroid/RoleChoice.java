package com.example.freelanceflowandroid;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

import java.io.PrintWriter;
import java.io.StringWriter;

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
        try {
            setContentView(R.layout.role_activity); // update if your layout filename differs

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
                StringWriter sw = new StringWriter();
                new NullPointerException("RoleChoice layout missing views").printStackTrace(new PrintWriter(sw));
                try {
                    android.content.Intent i = new android.content.Intent(this, CrashActivity.class);
                    i.putExtra("stack", sw.toString());
                    i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                } catch (Exception ex) { /* ignore */ }
                Toast.makeText(this, "UI not initialized correctly. Check layout IDs (see Logcat).", Toast.LENGTH_LONG).show();
                finish();
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

            // Login link -> route based on current selection:
            // - If user selected FREELANCER -> open freelancerOrTeam (choice scene)
            // - If user selected CLIENT     -> open LoginActivity (client login), but request to show login UI
            loginText.setClickable(true);
            loginText.setOnClickListener(v -> {
                // Persist the currently selected role (defensive; applyRole already saves it)
                PrefsManager.getInstance(RoleChoice.this).saveUserRole(selectedRole);

                try {
                    if (ROLE_FREELANCER.equals(selectedRole)) {
                        // Go directly to the freelancer/team choice screen
                        Intent intent = new Intent(RoleChoice.this, freelancerOrTeam.class);
                        startActivity(intent);
                    } else {
                        // Client -> go to login page (ask LoginActivity to show the login UI instead of auto-redirect)
                        Intent intent = new Intent(RoleChoice.this, LoginActivity.class);
                        intent.putExtra("initial_role", selectedRole);
                        intent.putExtra("show_login_ui", true);
                        startActivity(intent);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to start target activity from RoleChoice login link", ex);
                    Toast.makeText(RoleChoice.this, "Unable to open screen. Check AndroidManifest.", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Throwable t) {
            Log.e(TAG, "Startup exception in RoleChoice.onCreate", t);
            try {
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                android.content.Intent i = new android.content.Intent(this, CrashActivity.class);
                i.putExtra("stack", sw.toString());
                i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            } catch (Exception ex) { /* ignore */ }
            finish();
        }
    }

    private void applyRole(String role) {
        selectedRole = role;
        // persist selection using your PrefsManager
        PrefsManager.getInstance(this).saveUserRole(selectedRole);

        // palette: client=blue, freelancer=green, neutral=gray
        final int clientColor = Color.parseColor("#142081");
        final int freelancerColor = Color.parseColor("#00695C");
        final int neutralBg = Color.parseColor("#E0E0E0");
        final int neutralStroke = Color.parseColor("#9E9E9E");
        final int textOnSelected = Color.WHITE;
        final int textOnNeutral = Color.parseColor("#212121");

        if (ROLE_CLIENT.equals(role)) {
            // update UI for client (ensure drawable exists)
            imageView.setImageResource(R.drawable.client1);
            descriptionText.setText("Find, manage, pay talent — all in one place!");
            buttonClient.setChecked(true);
            buttonFreelancer.setChecked(false);
            styleButton(buttonClient, clientColor, clientColor, textOnSelected);
            styleButton(buttonFreelancer, neutralBg, neutralStroke, textOnNeutral);
            // align CTA color with client palette
            createAccountButton.setBackgroundTintList(ColorStateList.valueOf(clientColor));
            createAccountButton.setTextColor(textOnSelected);
        } else {
            imageView.setImageResource(R.drawable.free1);
            descriptionText.setText("Find projects, get hired and get paid!");
            buttonClient.setChecked(false);
            buttonFreelancer.setChecked(true);
            styleButton(buttonFreelancer, freelancerColor, freelancerColor, textOnSelected);
            styleButton(buttonClient, neutralBg, neutralStroke, textOnNeutral);
            // align CTA color with freelancer palette
            createAccountButton.setBackgroundTintList(ColorStateList.valueOf(freelancerColor));
            createAccountButton.setTextColor(textOnSelected);
        }
        // Do NOT call toggleGroup.check(...) here — that would re-trigger the listener
    }

    private void styleButton(MaterialButton button, int bgColor, int strokeColor, int textColor) {
        button.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        button.setStrokeColor(ColorStateList.valueOf(strokeColor));
        button.setTextColor(textColor);
    }
}