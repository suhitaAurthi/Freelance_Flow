package com.example.freelanceflowandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.freelanceflowandroid.PrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Minimal TeamLogin. On successful login (here: button click), saves role="team" and opens DashboardActivity.
 * Replace the TODO with real authentication logic.
 */
public class TeamLogin extends AppCompatActivity {

    private TextInputEditText etEmail, etPass;
    private MaterialButton btnLogin;
    private static final String ROLE_TEAM = "team";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_login);

        etEmail = findViewById(R.id.et_team_email);
        etPass = findViewById(R.id.et_team_pass);
        btnLogin = findViewById(R.id.btn_team_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: validate credentials with backend. For now accept any input.
                PrefsManager.getInstance(TeamLogin.this).saveUserRole(ROLE_TEAM);

                // Start central dashboard (assumes DashboardActivity reads PrefsManager.getUserRole())
                Intent i = new Intent(TeamLogin.this, com.example.freelanceflowandroid.DashboardActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}