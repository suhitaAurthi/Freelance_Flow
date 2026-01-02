package com.example.freelanceflowandroid;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import android.util.Patterns;

/**
 * LoginActivity
 *
 * Behavior:
 * - Immediately redirect if initial_role extra or saved role indicates client or freelancer,
 *   unless caller passed show_login_ui=true (in which case show the login UI).
 *   client -> DashboardClient
 *   freelancer -> freelancerOrTeam
 * - Otherwise show login UI.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private View forgotPass;
    private View textViewRegister;
    private MaterialButton loginButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Check Intent extra first, then PrefsManager
        try {
            String roleFromIntent = null;
            boolean showLoginUI = false;
            if (getIntent() != null) {
                if (getIntent().hasExtra("initial_role")) {
                    roleFromIntent = getIntent().getStringExtra("initial_role");
                }
                // callers can request login UI to be shown by passing this flag
                showLoginUI = getIntent().getBooleanExtra("show_login_ui", false);
            }

            // If caller requested to show the login UI, DO NOT auto-redirect.
            if (!showLoginUI) {
                String savedRole = roleFromIntent != null ? roleFromIntent : PrefsManager.getInstance(this).getUserRole();

                if (RoleChoice.ROLE_CLIENT.equals(savedRole)) {
                    // Redirect immediately to client dashboard
                    try {
                        startActivity(new Intent(this, DashboardClient.class));
                    } catch (ActivityNotFoundException ex) {
                        Toast.makeText(this, "Client dashboard not found.", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                    return;
                } else if (RoleChoice.ROLE_FREELANCER.equals(savedRole)) {
                    // Redirect immediately to freelancer/team choice
                    try {
                        startActivity(new Intent(this, freelancerOrTeam.class));
                    } catch (ActivityNotFoundException ex) {
                        Toast.makeText(this, "Freelancer/team choice screen not found.", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                    return;
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to resolve redirect role (showing login UI)", t);
            // continue to show login UI
        }

        // 2) No immediate redirect -> show login UI
        try {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

            setContentView(R.layout.activity_main); // your login layout

            View root = findViewById(android.R.id.content);
            if (root != null) {
                ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                    WindowInsetsCompat systemBars = insets;
                    v.setPadding(
                            systemBars.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                            systemBars.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                            systemBars.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                            systemBars.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                    );
                    return insets;
                });
            }

            etEmail = findViewById(R.id.etEmail);
            etPassword = findViewById(R.id.etPassword);
            forgotPass = findViewById(R.id.textView7);
            textViewRegister = findViewById(R.id.textView8);
            loginButton = findViewById(R.id.button);

            if (etEmail == null || etPassword == null || loginButton == null || textViewRegister == null) {
                Log.e(TAG, "One or more login views are null. Check activity_main.xml IDs.");
                Toast.makeText(this, "Login UI not initialized correctly. See Logcat for details.", Toast.LENGTH_LONG).show();
                return;
            }

            etPassword.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (loginButton != null && loginButton.isEnabled()) {
                        loginButton.performClick();
                    }
                    return true;
                }
                return false;
            });

            if (forgotPass != null) {
                forgotPass.setOnClickListener(v -> {
                    try {
                        Intent i = new Intent(LoginActivity.this, ForgetPassActivity.class);
                        // prefill email in forgot-pass screen if user entered one
                        if (etEmail != null && etEmail.getText() != null) {
                            String email = etEmail.getText().toString().trim();
                            if (!email.isEmpty()) i.putExtra("prefill_email", email);
                        }
                        startActivity(i);
                    } catch (ActivityNotFoundException ex) {
                        Toast.makeText(LoginActivity.this, "Forget password screen not found.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            textViewRegister.setOnClickListener(v -> {
                String savedRole = PrefsManager.getInstance(LoginActivity.this).getUserRole();
                try {
                    if (RoleChoice.ROLE_FREELANCER.equals(savedRole)) {
                        startActivity(new Intent(LoginActivity.this, RegisterFreelancerActivity.class));
                    } else if (RoleChoice.ROLE_CLIENT.equals(savedRole)) {
                        startActivity(new Intent(LoginActivity.this, RegisterClientActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, RoleChoice.class));
                    }
                } catch (ActivityNotFoundException ex) {
                    Log.e(TAG, "Registration activity not found", ex);
                    Toast.makeText(LoginActivity.this, "Registration activity not found. Check AndroidManifest.", Toast.LENGTH_SHORT).show();
                }
            });

            // Login button: after successful auth, route according to saved role in PrefsManager
            loginButton.setOnClickListener(v -> {
                try {
                    // Validate email & password before proceeding (simple client-side checks)
                    String email = (etEmail != null && etEmail.getText() != null) ? etEmail.getText().toString().trim() : "";
                    String password = (etPassword != null && etPassword.getText() != null) ? etPassword.getText().toString() : "";

                    boolean ok = true;
                    if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        etEmail.setError("Enter a valid email");
                        ok = false;
                    } else {
                        etEmail.setError(null);
                    }

                    if (password.length() < 8) {
                        etPassword.setError("Password must be at least 8 characters");
                        ok = false;
                    } else {
                        etPassword.setError(null);
                    }

                    if (!ok) return; // don't proceed

                    // TODO: replace with real authentication and save token
                    String savedRoleNow = PrefsManager.getInstance(LoginActivity.this).getUserRole();
                    if (RoleChoice.ROLE_CLIENT.equals(savedRoleNow)) {
                        startActivity(new Intent(LoginActivity.this, DashboardClient.class));
                    } else if (RoleChoice.ROLE_FREELANCER.equals(savedRoleNow)) {
                        // after auth, freelancer chooses team or freelancer flow
                        startActivity(new Intent(LoginActivity.this, freelancerOrTeam.class));
                    } else {
                        // fallback
                        startActivity(new Intent(LoginActivity.this, RoleChoice.class));
                    }
                    finish();
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(LoginActivity.this, "Destination activity not found", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Throwable t) {
            Log.e(TAG, "onCreate failed", t);
            Toast.makeText(this, "Startup error: " + t.getClass().getSimpleName() + " — check Logcat", Toast.LENGTH_LONG).show();
        }
    }
}