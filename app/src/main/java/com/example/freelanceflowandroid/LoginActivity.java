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

        // defensive wrapper so we surface errors in a Toast instead of immediate crash during dev
        try {
            // Edge-to-edge (optional)
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

            // Make sure this points to your login layout (update if your layout file name differs)
            setContentView(R.layout.activity_main);

            // Handle window insets for safe area (optional)
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

            // IMPORTANT: these IDs must exist in activity_main.xml and point to the inner TextInputEditText
            etEmail = findViewById(R.id.etEmail);
            etPassword = findViewById(R.id.etPassword);
            forgotPass = findViewById(R.id.textView7);
            textViewRegister = findViewById(R.id.textView8);
            loginButton = findViewById(R.id.button);

            // Defensive null checks — will show log + toast and stop initialization if UI is miswired
            if (etEmail == null || etPassword == null || loginButton == null || textViewRegister == null) {
                Log.e(TAG, "One or more login views are null. Check activity_main.xml IDs.");
                Toast.makeText(this, "Login UI not initialized correctly. See Logcat for details.", Toast.LENGTH_LONG).show();
                return;
            }

            // IME action on password: press Done triggers login
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

            // Forgot password (optional)
            if (forgotPass != null) {
                forgotPass.setOnClickListener(v -> {
                    try {
                        Intent i = new Intent(LoginActivity.this, ForgetPassActivity.class);
                        startActivity(i);
                    } catch (ActivityNotFoundException ex) {
                        Toast.makeText(LoginActivity.this, "Forget password screen not found.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // "Don't have an account?" -> open saved-role registration or RoleChoice
            textViewRegister.setOnClickListener(v -> {
                String savedRole = PrefsManager.getInstance(LoginActivity.this).getUserRole();
                try {
                    if (RoleChoice.ROLE_FREELANCER.equals(savedRole)) {
                        startActivity(new Intent(LoginActivity.this, RegisterFreelancerActivity.class));
                    } else if (RoleChoice.ROLE_CLIENT.equals(savedRole)) {
                        startActivity(new Intent(LoginActivity.this, RegisterClientActivity.class));
                    } else {
                        // No saved role — let user choose
                        startActivity(new Intent(LoginActivity.this, RoleChoice.class));
                    }
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(LoginActivity.this, "Registration activity not found. Check AndroidManifest.", Toast.LENGTH_SHORT).show();
                }
            });

            // Login button (placeholder - replace with real auth)
            loginButton.setOnClickListener(v -> {
                try {
                    // TODO: validate credentials, call backend, store token, etc.
                    Intent i = new Intent(LoginActivity.this, DashboardClient.class);
                    startActivity(i);
                    finish();
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(LoginActivity.this, "DashboardActivity not found", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Throwable t) {
            // Log and show a toast so you can capture the stack trace and fix underlying error
            Log.e(TAG, "onCreate failed", t);
            Toast.makeText(this, "Startup error: " + t.getClass().getSimpleName() + " — check Logcat", Toast.LENGTH_LONG).show();
            // Optionally finish() so user doesn't stay on a broken screen:
            // finish();
        }
    }
}