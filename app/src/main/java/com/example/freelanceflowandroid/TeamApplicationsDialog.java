package com.example.freelanceflowandroid;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.freelanceflowandroid.data.InMemoryRepo;

import java.util.ArrayList;

public class TeamApplicationsDialog {

    // Accept an AppCompatActivity so we have both Context and a LifecycleOwner
    public static void show(AppCompatActivity owner, String noticeId) {
        View v = LayoutInflater.from(owner).inflate(R.layout.dialog_team_applications, null, false);
        RecyclerView rv = v.findViewById(R.id.rv_applications);
        rv.setLayoutManager(new LinearLayoutManager(owner));
        TeamApplicationsAdapter adapter = new TeamApplicationsAdapter(new ArrayList<>(), (applicationId) -> {
            boolean ok = InMemoryRepo.get().hireApplicant(noticeId, applicationId);
            if (ok) Toast.makeText(owner, "Hired successfully", Toast.LENGTH_SHORT).show();
            else Toast.makeText(owner, "Could not hire", Toast.LENGTH_SHORT).show();
        });
        rv.setAdapter(adapter);

        AlertDialog dlg = new AlertDialog.Builder(owner)
                .setTitle("Applications")
                .setView(v)
                .setPositiveButton("Close", null)
                .create();

        // Use lifecycle-aware observation so it stops when the activity is destroyed
        InMemoryRepo.get().getApplicationsFor(noticeId).observe(owner, recruitmentApplications -> adapter.updateList(recruitmentApplications != null ? recruitmentApplications : new ArrayList<>()));

        dlg.show();
    }
}