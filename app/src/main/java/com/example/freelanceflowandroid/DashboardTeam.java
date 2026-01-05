package com.example.freelanceflowandroid;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.freelanceflowandroid.data.InMemoryRepo;
import com.example.freelanceflowandroid.model.RecruitmentNotice;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays; // added for Arrays.asList

/**
 * Team dashboard that posts notices and views applications (connected using InMemoryRepo).
 */
public class DashboardTeam extends AppCompatActivity implements TeamNoticeAdapter.NoticeClickListener {

    private TeamNoticeAdapter adapter;
    // keep the import path matching the actual repo file location (com.example.freelanceflowandroid.data)
    private final InMemoryRepo repo = InMemoryRepo.get();

    private String currentTeamId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dash_team);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_team);
        setSupportActionBar(toolbar);

        // Determine team id from Intent extras or PrefsManager
        currentTeamId = getIntent() != null ? getIntent().getStringExtra("extra_team_id") : null;
        if (currentTeamId == null) {
            currentTeamId = PrefsManager.getInstance(this).getUserTeamId();
        }
        // fallback demo id
        if (currentTeamId == null) currentTeamId = "team_alpha";

        findViewById(R.id.btn_post_notice).setOnClickListener(v -> showPostDialog());

        RecyclerView rvNotices = findViewById(R.id.rv_notices);
        rvNotices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TeamNoticeAdapter(new ArrayList<>(), this);
        rvNotices.setAdapter(adapter);

        // Observe notices and filter to this team
        repo.getNotices().observe(this, recruitmentNotices -> {
            List<RecruitmentNotice> mine = new ArrayList<>();
            if (recruitmentNotices != null) {
                for (RecruitmentNotice n : recruitmentNotices) {
                    if (currentTeamId.equals(n.teamId)) mine.add(n);
                }
            }
            adapter.updateList(mine);
        });
    }

    private void showPostDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_post_notice, null, false);
        EditText etTitle = v.findViewById(R.id.et_title);
        EditText etDesc = v.findViewById(R.id.et_desc);

        new AlertDialog.Builder(this)
                .setTitle("Post Recruitment Notice")
                .setView(v)
                .setPositiveButton("Post", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String desc = etDesc.getText().toString().trim();
                    if (TextUtils.isEmpty(title)) {
                        Toast.makeText(DashboardTeam.this, "Title required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // uses repo.postNotice(...) — make sure InMemoryRepo implements this (see sample below)
                    repo.postNotice(currentTeamId, title, desc, Arrays.asList("team", "hiring"));
                    Toast.makeText(DashboardTeam.this, "Notice posted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onNoticeClicked(RecruitmentNotice notice) {
        TeamApplicationsDialog.show(this, notice.id);
    }
}