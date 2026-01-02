package com.example.freelanceflowandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.freelanceflowandroid.PrefsManager;
import com.google.android.material.button.MaterialButton;

/**
 * Kept class name 'freelancerOrTeam' as requested.
 * When user arrives here:
 *  - "Yes" -> mark role "team" and open team login (TeamLogin)
 *  - "No"  -> mark role "freelancer" and open DashboardFreelancer
 */
public class freelancerOrTeam extends AppCompatActivity {

    private MaterialButton btnJoinedTeam;
    private MaterialButton btnWantJoin;

    // role strings consistent with PrefsManager usage (RoleChoice uses "client"/"freelancer")
    private static final String ROLE_TEAM = "team";
    private static final String ROLE_FREELANCER = "freelancer";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.freelancer_role);

        btnJoinedTeam = findViewById(R.id.btn_joined_team);
        btnWantJoin = findViewById(R.id.btn_want_join);

        btnJoinedTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Save role = team
                PrefsManager.getInstance(freelancerOrTeam.this).saveUserRole(ROLE_TEAM);

                // Open TeamLogin activity (team-specific login screen)
                Intent intent = new Intent(freelancerOrTeam.this, TeamLogin.class);
                startActivity(intent);
                finish();
            }
        });

        btnWantJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Save role = freelancer
                PrefsManager.getInstance(freelancerOrTeam.this).saveUserRole(ROLE_FREELANCER);

                // Open Freelancer dashboard activity
                Intent intent = new Intent(freelancerOrTeam.this, DashboardFreelancer.class);
                startActivity(intent);
                finish();
            }
        });
    }
}