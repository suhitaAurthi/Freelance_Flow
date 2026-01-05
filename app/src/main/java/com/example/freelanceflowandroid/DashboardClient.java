package com.example.freelanceflowandroid;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DashboardClient extends AppCompatActivity {

    public static final String EXTRA_USER_ROLE = "extra_user_role";
    public static final String ROLE_FREELANCER = "freelancer";
    public static final String ROLE_CLIENT = "client";

    private static final String TAG = "DashboardClient";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.dash_client);

            // set up toolbar provided by activity layout
            Toolbar toolbar = findViewById(R.id.toolbar_client);
            if (toolbar != null) {
                // Clear default title — we'll use a centered serif TextView so title appears centered.
                toolbar.setTitle("");
                setSupportActionBar(toolbar);

                // Add a centered TextView to simulate a centered toolbar title with serif font
                TextView titleTv = new TextView(this);
                titleTv.setText(getString(R.string.client_dashboard_title));
                titleTv.setTextColor(Color.WHITE);
                titleTv.setTypeface(Typeface.SERIF, Typeface.BOLD);
                // Keep default-ish size; if you have a specific size resource, swap here to "keep text size"
                titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                Toolbar.LayoutParams lp = new Toolbar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER
                );
                titleTv.setLayoutParams(lp);
                toolbar.addView(titleTv);
            }

            // host the ClientDashboardFragment inside the fragment container
            if (savedInstanceState == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container_client,
                                new com.example.freelanceflowandroid.fragments.ClientDashboardFragment())
                        .commit();
            }

            String role = getIntent().getStringExtra(EXTRA_USER_ROLE);
            Log.i(TAG, "Started DashboardClient with role=" + role);

        } catch (Throwable t) {
            Log.e(TAG, "onCreate failed", t);
        }
    }
}