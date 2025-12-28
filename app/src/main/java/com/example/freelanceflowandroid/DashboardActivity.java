package com.example.freelanceflowandroid;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.freelanceflowandroid.fragments.ClientDashboardFragment;
import com.example.freelanceflowandroid.fragments.FreelancerDashboardFragment;
import com.example.freelanceflowandroid.fragments.TeamDashboardFragment;

public class DashboardActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ROLE = "extra_user_role";
    public static final String ROLE_FREELANCER = "FREELANCER";
    public static final String ROLE_TEAM_ADMIN = "TEAM_ADMIN";
    public static final String ROLE_CLIENT = "CLIENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        String role = getIntent().getStringExtra(EXTRA_USER_ROLE);
        if (role == null) {
            role = PrefsManager.getInstance(this).getUserRole();
        }
        if (role == null) role = ROLE_FREELANCER; // default

        Fragment fragment;
        switch (role) {
            case ROLE_TEAM_ADMIN:
                fragment = new TeamDashboardFragment();
                break;
            case ROLE_CLIENT:
                fragment = new ClientDashboardFragment();
                break;
            case ROLE_FREELANCER:
            default:
                fragment = new FreelancerDashboardFragment();
                break;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}