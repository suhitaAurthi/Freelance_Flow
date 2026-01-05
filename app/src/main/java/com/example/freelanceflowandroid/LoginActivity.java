package com.example.freelanceflowandroid;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private android.view.View forgotPass;
    private android.view.View textViewRegister;
    private MaterialButton loginButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

            setContentView(R.layout.activity_main);

            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            etEmail = findViewById(R.id.etEmail);
            etPassword = findViewById(R.id.etPassword);
            forgotPass = findViewById(R.id.textView7);
            textViewRegister = findViewById(R.id.textView8);
            loginButton = findViewById(R.id.button);

            if (etEmail == null || etPassword == null || loginButton == null || textViewRegister == null) {
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
                    Toast.makeText(LoginActivity.this, "Registration activity not found. Check AndroidManifest.", Toast.LENGTH_SHORT).show();
                }
            });

            loginButton.setOnClickListener(v -> {
                String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
                String password = etPassword.getText() == null ? "" : etPassword.getText().toString().trim();
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                loginButton.setEnabled(false);
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            loginButton.setEnabled(true);
                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    String uid = user.getUid();
                                    PrefsManager.getInstance(LoginActivity.this).saveUserUid(uid);
                                    db.collection("users").document(uid).get()
                                            .addOnSuccessListener(documentSnapshot -> {
                                                String role = null;
                                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                                    role = documentSnapshot.getString("role");
                                                }
                                                if (role == null) {
                                                    role = DashboardClient.ROLE_CLIENT;
                                                }
                                                PrefsManager.getInstance(LoginActivity.this).saveUserRole(role);
                                                Intent i = new Intent(LoginActivity.this,
                                                        DashboardClient.ROLE_FREELANCER.equals(role)
                                                                ? DashboardFreelancer.class
                                                                : DashboardClient.class);
                                                i.putExtra(DashboardClient.EXTRA_USER_ROLE, role);
                                                startActivity(i);
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(LoginActivity.this, "Failed to read profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                PrefsManager.getInstance(LoginActivity.this).saveUserRole(DashboardClient.ROLE_CLIENT);
                                                Intent i = new Intent(LoginActivity.this, DashboardClient.class);
                                                i.putExtra(DashboardClient.EXTRA_USER_ROLE, DashboardClient.ROLE_CLIENT);
                                                startActivity(i);
                                                finish();
                                            });
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Login failed: " + (task.getException() == null ? "unknown" : task.getException().getMessage()), Toast.LENGTH_LONG).show();
                            }
                        });
            });

        } catch (Throwable t) {
            Log.e(TAG, "onCreate failed", t);
            Toast.makeText(this, "Startup error: " + t.getClass().getSimpleName() + " — check Logcat", Toast.LENGTH_LONG).show();
        }
    }
}
