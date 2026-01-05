package com.example.freelanceflowandroid.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.freelanceflowandroid.DashboardTeam;
import com.example.freelanceflowandroid.PrefsManager;
import com.example.freelanceflowandroid.R;
import com.example.freelanceflowandroid.RecruitmentAdapter;
import com.example.freelanceflowandroid.data.InMemoryRepo;
import com.example.freelanceflowandroid.model.RecruitmentNotice;

import java.util.ArrayList;
import java.util.List;

/**
 * Freelancer dashboard fragment — shows recruitment notices and lets freelancers apply / join/create team.
 */
public class FreelancerDashboardFragment extends Fragment implements RecruitmentAdapter.ApplyListener {

    private RecyclerView rvNotices;
    private TextView tvEmpty;
    private RecruitmentAdapter adapter;
    private final InMemoryRepo repo = InMemoryRepo.get();

    // demo freelancer identity - swap with real logged-in user info when ready
    private final String freelancerId = "freelancer_1";
    private final String freelancerName = "Khalid Uzzal";

    public FreelancerDashboardFragment() { /* empty constructor */ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_freelancer_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SearchView searchView = view.findViewById(R.id.search_view);
        rvNotices = view.findViewById(R.id.rv_notices);
        tvEmpty = view.findViewById(R.id.tv_empty);
        Button joinBtn = view.findViewById(R.id.btn_join_team);
        Button createBtn = view.findViewById(R.id.btn_create_team);

        rvNotices.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RecruitmentAdapter(new ArrayList<>(), this, repo, freelancerId);
        rvNotices.setAdapter(adapter);

        // Observe notices (use view lifecycle owner)
        repo.getNotices().observe(getViewLifecycleOwner(), recruitmentNotices -> {
            List<RecruitmentNotice> list = new ArrayList<>();
            if (recruitmentNotices != null) list.addAll(recruitmentNotices);
            adapter.updateList(list);
            updateEmptyState();
        });

        updateEmptyState();

        if (joinBtn != null) {
            joinBtn.setOnClickListener(v -> showJoinTeamDialog());
        }

        if (createBtn != null) {
            createBtn.setOnClickListener(v -> showCreateTeamDialog());
        }

        // Optional: simple search hook (filters by title/tags if you implement filtering)
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String query) { return false; }
                @Override
                public boolean onQueryTextChange(String newText) {
                    // if your adapter supports filtering, call it here
                    // adapter.filter(newText);
                    return false;
                }
            });
        }
    }

    private void showJoinTeamDialog() {
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_join_team, null, false);
        EditText etTeam = v.findViewById(R.id.et_team_id);
        new AlertDialog.Builder(requireContext())
                .setTitle("Join a Team")
                .setView(v)
                .setPositiveButton("Join", (d, which) -> {
                    String teamId = etTeam.getText() != null ? etTeam.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(teamId)) {
                        Toast.makeText(requireContext(), "Please enter a team id", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    PrefsManager.getInstance(requireContext()).saveUserTeamId(teamId);
                    repo.addTeamMember(teamId, freelancerId);
                    Intent i = new Intent(requireContext(), DashboardTeam.class);
                    i.putExtra("extra_team_id", teamId);
                    startActivity(i);
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCreateTeamDialog() {
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_team, null, false);
        EditText etTeamId = v.findViewById(R.id.et_new_team_id);
        EditText etTeamName = v.findViewById(R.id.et_new_team_name);

        new AlertDialog.Builder(requireContext())
                .setTitle("Create a Team")
                .setView(v)
                .setPositiveButton("Create", (d, which) -> {
                    String teamId = etTeamId.getText() != null ? etTeamId.getText().toString().trim() : "";
                    String teamName = etTeamName.getText() != null ? etTeamName.getText().toString().trim() : "";

                    if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty(teamName)) {
                        Toast.makeText(requireContext(), "Please enter both team id and name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean created;
                    try {
                        created = repo.createTeam(teamId, teamName, freelancerId);
                    } catch (NoSuchMethodError | RuntimeException ex) {
                        created = false;
                        try {
                            repo.addTeamMember(teamId, freelancerId);
                            created = true;
                        } catch (Exception e) {
                            // keep created as false
                        }
                    }

                    if (!created) {
                        Toast.makeText(requireContext(), "Team already exists or cannot be created", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    PrefsManager.getInstance(requireContext()).saveUserTeamId(teamId);
                    repo.addTeamMember(teamId, freelancerId);

                    Intent i = new Intent(requireContext(), DashboardTeam.class);
                    i.putExtra("extra_team_id", teamId);
                    startActivity(i);
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateEmptyState() {
        boolean empty = adapter == null || adapter.getItemCount() == 0;
        if (tvEmpty != null) tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (rvNotices != null) rvNotices.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onApply(String noticeId) {
        if (TextUtils.isEmpty(freelancerId)) {
            Toast.makeText(requireContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String freelancerName = "Khalid Uzzal";
        boolean ok = repo.applyToNotice(noticeId, freelancerId, freelancerName);
        if (ok) Toast.makeText(requireContext(), "Applied", Toast.LENGTH_SHORT).show();
        else Toast.makeText(requireContext(), "Already applied", Toast.LENGTH_SHORT).show();
    }
}