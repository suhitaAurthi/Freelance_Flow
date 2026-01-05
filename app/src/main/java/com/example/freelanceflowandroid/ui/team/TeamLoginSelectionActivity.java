package com.example.freelanceflowandroid.ui.team;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.freelanceflowandroid.R;

public class TeamLoginSelectionActivity extends AppCompatActivity {
    private Button adminBtn, memberBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_login_selection);

        adminBtn = findViewById(R.id.btnAdmin);
        memberBtn = findViewById(R.id.btnMember);

        adminBtn.setOnClickListener(v -> startActivity(new Intent(this, TeamLoginActivity.class).putExtra("isAdmin", true)));
        memberBtn.setOnClickListener(v -> startActivity(new Intent(this, TeamLoginActivity.class).putExtra("isAdmin", false)));
    }
}

