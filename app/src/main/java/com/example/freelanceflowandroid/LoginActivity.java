/*package com.example.freelanceflowandroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Apply window insets to the root container (safe, with null-check)
        android.view.View root = findViewById(R.id.root_container);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Included scene containers (the include ids from activity_main)
        final android.view.View includeLogin = findViewById(R.id.include_login);
        final android.view.View includeRegister = findViewById(R.id.include_register);

        // --- Login scene controls (ids from scene_login.xml) ---
        TextView forgotPass = null;
        TextView textViewRegister = null;
        Button loginButton = null;
        if (includeLogin != null) {
            forgotPass = includeLogin.findViewById(R.id.textViewForgot);
            textViewRegister = includeLogin.findViewById(R.id.textViewRegister);
            loginButton = includeLogin.findViewById(R.id.loginButton);
        }

        if (forgotPass != null) {
            forgotPass.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgetPassActivity.class)));
        }

        if (textViewRegister != null) {
            textViewRegister.setOnClickListener(v -> {
                if (includeLogin != null && includeRegister != null) {
                    includeLogin.setVisibility(android.view.View.GONE);
                    includeRegister.setVisibility(android.view.View.VISIBLE);
                }
            });
        }

        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                finish();
            });
        }

        // --- Register scene controls (ids from register.xml) ---
        TextView backToLogin = null;
        Button registerSubmit = null;
        if (includeRegister != null) {
            backToLogin = includeRegister.findViewById(R.id.textViewBackToLogin);
            registerSubmit = includeRegister.findViewById(R.id.registerSubmitButton);
        }

        if (backToLogin != null) {
            backToLogin.setOnClickListener(v -> {
                if (includeLogin != null && includeRegister != null) {
                    includeRegister.setVisibility(android.view.View.GONE);
                    includeLogin.setVisibility(android.view.View.VISIBLE);
                }
            });
        }

        if (registerSubmit != null) {
            registerSubmit.setOnClickListener(v -> {
                // TODO: implement registration logic (validation, API/db call)
                // For now, switch back to login after (placeholder behavior)
                if (includeLogin != null && includeRegister != null) {
                    includeRegister.setVisibility(android.view.View.GONE);
                    includeLogin.setVisibility(android.view.View.VISIBLE);
                }
            });
        }
    }
}*/
package com.example.freelanceflowandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private TextView forgotPass;
    private TextView textViewRegister;
    private MaterialButton loginButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge and allow handling insets manually
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);

        // root is the NestedScrollView with id "scroll"
        View root = findViewById(R.id.scroll);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                // apply system bar insets as padding so content is not hidden under status/nav bars
                WindowInsetsCompat systemBars = insets;
                int left = systemBars.getInsets(WindowInsetsCompat.Type.systemBars()).left;
                int top = systemBars.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                int right = systemBars.getInsets(WindowInsetsCompat.Type.systemBars()).right;
                int bottom = systemBars.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                v.setPadding(left, top, right, bottom);
                return insets;
            });
        }

        // Find views (IDs match in both portrait and landscape layouts)
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        forgotPass = findViewById(R.id.textView8);
        textViewRegister = findViewById(R.id.textView9);
        loginButton = findViewById(R.id.loginButton);

        // IME action: when user presses Done on keyboard while in password field, trigger login
        if (etPassword != null) {
            etPassword.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (loginButton != null && loginButton.isEnabled()) {
                        loginButton.performClick();
                    }
                    return true;
                }
                return false;
            });
        }

        // Click listeners
        if (forgotPass != null) {
            forgotPass.setOnClickListener(v -> {
                // Open ForgetPassActivity if implemented
                Intent i = new Intent(LoginActivity.this, ForgetPassActivity.class);
                startActivity(i);
            });
        }

        if (textViewRegister != null) {
            textViewRegister.setOnClickListener(v -> {
                // Open RegisterActivity if implemented
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            });
        }

        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                // TODO: replace with real authentication logic (validation, network call)
                // For now navigate to DashboardActivity
                Intent i = new Intent(LoginActivity.this, DashboardActivity.class);
                startActivity(i);
                finish();
            });
        }
    }
}