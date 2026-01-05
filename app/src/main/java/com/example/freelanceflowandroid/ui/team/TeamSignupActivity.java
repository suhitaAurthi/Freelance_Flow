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
import com.example.freelanceflowandroid.data.model.Team;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TeamSignupActivity extends AppCompatActivity {
    private EditText nameEt;
    private Button createBtn;
    private ProgressBar progressBar;
    private TeamViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_signup);

        nameEt = findViewById(R.id.etTeamName);
        createBtn = findViewById(R.id.btnCreateTeam);
        progressBar = findViewById(R.id.progressBar);

        viewModel = new ViewModelProvider(this).get(TeamViewModel.class);

        createBtn.setOnClickListener(v -> onCreateTeam());
    }

    private void onCreateTeam() {
        String name = nameEt.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter team name", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You must be signed in to create a team", Toast.LENGTH_SHORT).show();
            return;
        }

        createBtn.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        Team team = new Team(name, user.getUid());
        viewModel.createTeam(team, user.getUid()).observe(this, res -> {
            if (res == null) return;
            if (res.status == Resource.Status.LOADING) {
                progressBar.setVisibility(View.VISIBLE);
                createBtn.setEnabled(false);
            } else if (res.status == Resource.Status.SUCCESS) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Team created", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                progressBar.setVisibility(View.GONE);
                createBtn.setEnabled(true);
                String msg = res.exception != null ? res.exception.getMessage() : "Failed to create team";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
