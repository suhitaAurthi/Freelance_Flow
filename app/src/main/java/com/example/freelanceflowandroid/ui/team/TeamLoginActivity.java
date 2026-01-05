package com.example.freelanceflowandroid.ui.team;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.freelanceflowandroid.R;
import com.example.freelanceflowandroid.data.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TeamLoginActivity extends AppCompatActivity {
    private EditText teamIdEt;
    private Button joinBtn;
    private ProgressBar progressBar;
    private TeamViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_login);

        boolean isAdmin = getIntent().getBooleanExtra("isAdmin", false);
        teamIdEt = findViewById(R.id.etTeamId);
        joinBtn = findViewById(R.id.btnJoin);
        progressBar = findViewById(R.id.progressBar);

        viewModel = new ViewModelProvider(this).get(TeamViewModel.class);

        joinBtn.setOnClickListener(v -> {
            String teamId = teamIdEt.getText().toString().trim();
            if (teamId.isEmpty()) { Toast.makeText(this, "Enter team ID", Toast.LENGTH_SHORT).show(); return; }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "You must be signed in to join a team", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable UI while joining
            progressBar.setVisibility(View.VISIBLE);
            joinBtn.setEnabled(false);

            viewModel.joinTeam(teamId, user.getUid()).observe(this, res -> {
                if (res == null) return;
                if (res.status == Resource.Status.LOADING) {
                    progressBar.setVisibility(View.VISIBLE);
                    joinBtn.setEnabled(false);
                } else if (res.status == Resource.Status.SUCCESS) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Joined team successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    progressBar.setVisibility(View.GONE);
                    joinBtn.setEnabled(true);
                    String msg = res.exception != null ? res.exception.getMessage() : "Failed to join team";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
