package com.example.freelanceflowandroid;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.freelanceflowandroid.fragments.ClientDashboardFragment;
import com.example.freelanceflowandroid.fragments.FreelancerDashboardFragment;
import com.example.freelanceflowandroid.fragments.TeamDashboardFragment;

public class DashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.central_dashboard);

        String role = PrefsManager.getInstance(this).getUserRole(); // "client","freelancer","team"

        // load appropriate fragment (only once)
        if (savedInstanceState == null) {
            if ("client".equals(role)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ClientDashboardFragment())
                        .commit();
            } else if ("freelancer".equals(role)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new FreelancerDashboardFragment())
                        .commit();
            } else if ("team".equals(role)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TeamDashboardFragment())
                        .commit();
            }
        }

        // example: show FAB only for roles that can post
        // central_dashboard may not declare a FAB; resolve id at runtime to avoid compile-time error if missing.
        int fabId = getResources().getIdentifier("fab_primary", "id", getPackageName());
        if (fabId != 0) {
            View fab = findViewById(fabId);
            if (fab != null) {
                fab.setVisibility(("client".equals(role) || "team".equals(role)) ? View.VISIBLE : View.GONE);
            }
        }
    }
}