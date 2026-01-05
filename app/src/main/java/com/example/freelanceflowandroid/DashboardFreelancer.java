package com.example.freelanceflowandroid;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardFreelancer extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dash_freelancer);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_freelancer);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Freelancer Dashboard");

        // host the fragment if not already added
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new com.example.freelanceflowandroid.fragments.FreelancerDashboardFragment())
                    .commit();
        }
    }
}