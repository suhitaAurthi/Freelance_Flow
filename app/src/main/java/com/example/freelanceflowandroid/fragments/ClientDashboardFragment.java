package com.example.freelanceflowandroid.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.freelanceflowandroid.PrefsManager;
import com.example.freelanceflowandroid.R;
import com.example.freelanceflowandroid.data.FirestoreRepository;
import com.example.freelanceflowandroid.data.Resource;
import com.example.freelanceflowandroid.data.model.Job;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Client dashboard fragment: shows client's jobs and lets client create new posts and view applicants.
 */
public class ClientDashboardFragment extends Fragment {

    private RecyclerView rvJobs;
    private Button btnPostJob;
    private TextView tvActiveJobs;
    private FirestoreRepository repo;
    private JobAdapter adapter;
    private final List<Job> jobs = new ArrayList<>();

    // keep track of listener so we can stop it via repo.stopJobsListener()
    private String currentClientUid;

    public ClientDashboardFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvJobs = view.findViewById(R.id.rv_client_jobs);
        btnPostJob = view.findViewById(R.id.btn_post_job);
        tvActiveJobs = view.findViewById(R.id.tv_active_jobs);

        repo = new FirestoreRepository(requireContext());

        adapter = new JobAdapter(jobs);
        rvJobs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvJobs.setAdapter(adapter);

        currentClientUid = PrefsManager.getInstance(requireContext()).getUserUid();
        if (currentClientUid == null) {
            Toast.makeText(requireContext(), "No signed-in user found. Please login.", Toast.LENGTH_LONG).show();
            return;
        }

        // start listening for jobs created by this client
        repo.startJobsListener(currentClientUid, (value, error) -> {
            if (error != null) {
                Toast.makeText(requireContext(), "Failed to load jobs: " + error.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            if (value == null) {
                jobs.clear();
                adapter.notifyDataSetChanged();
                tvActiveJobs.setText("0");
                return;
            }

            jobs.clear();
            for (DocumentSnapshot ds : value.getDocuments()) {
                Job j = mapDocToJob(ds);
                jobs.add(j);
            }
            adapter.notifyDataSetChanged();
            tvActiveJobs.setText(String.valueOf(jobs.size()));
        });

        btnPostJob.setOnClickListener(v -> showPostJobDialog());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // stop any active listeners
        try { if (repo != null) repo.stopJobsListener(); } catch (Exception ignored) {}
    }

    private Job mapDocToJob(DocumentSnapshot ds) {
        Job j = new Job();
        j.id = ds.getId();
        j.title = ds.getString("title");
        j.description = ds.getString("description");
        j.clientId = ds.getString("clientId");
        Object b = ds.get("budget");
        if (b instanceof Number) {
            j.budget = ((Number) b).doubleValue();
        } else {
            j.budget = Double.NaN;
        }
        j.status = ds.getString("status");
        return j;
    }

    private void showPostJobDialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_post_job, null);

        final EditText etTitle = dialogView.findViewById(R.id.et_job_title);
        final EditText etDescription = dialogView.findViewById(R.id.et_job_description);
        final EditText etBudget = dialogView.findViewById(R.id.et_job_budget);

        final AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Post a new job")
                .setView(dialogView)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Post", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(v -> {
                String title = etTitle.getText() == null ? "" : etTitle.getText().toString().trim();
                String desc = etDescription.getText() == null ? "" : etDescription.getText().toString().trim();
                String bStr = etBudget.getText() == null ? "" : etBudget.getText().toString().trim();

                if (TextUtils.isEmpty(title)) {
                    etTitle.setError("Title required");
                    return;
                }
                double budget = Double.NaN;
                if (!TextUtils.isEmpty(bStr)) {
                    try {
                        budget = Double.parseDouble(bStr);
                    } catch (NumberFormatException ex) {
                        etBudget.setError("Enter a valid number");
                        return;
                    }
                }

                Job job = new Job();
                job.title = title;
                job.description = desc;
                job.clientId = currentClientUid;
                job.budget = budget;
                job.status = "OPEN";

                // create job using repository
                repo.createJob(job).observe(getViewLifecycleOwner(), resource -> {
                    if (resource == null) return;
                    if (resource.status == Resource.Status.LOADING) {
                        return;
                    }
                    if (resource.status == Resource.Status.SUCCESS) {
                        Toast.makeText(requireContext(), "Job posted", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else if (resource.status == Resource.Status.ERROR) {
                        Toast.makeText(requireContext(), "Failed to post job: " + (resource.exception != null ? resource.exception.getMessage() : "Unknown"), Toast.LENGTH_LONG).show();
                    }
                });
            });
        });

        dialog.show();
    }

    // Adapter + ViewHolder

    private class JobAdapter extends RecyclerView.Adapter<JobViewHolder> {
        private final List<Job> items;
        JobAdapter(List<Job> items) { this.items = items; }

        @NonNull
        @Override
        public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_job, parent, false);
            return new JobViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
            Job job = items.get(position);
            holder.bind(job);
        }

        @Override
        public int getItemCount() { return items.size(); }
    }

    private class JobViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvBudget, tvStatus;
        Button btnViewApplicants;

        JobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.item_job_title);
            tvBudget = itemView.findViewById(R.id.item_job_budget);
            tvStatus = itemView.findViewById(R.id.item_job_status);
            btnViewApplicants = itemView.findViewById(R.id.btn_view_applicants);
        }

        void bind(Job job) {
            tvTitle.setText(job.title == null ? "(no title)" : job.title);
            tvBudget.setText(Double.isNaN(job.budget) ? "" : String.format(Locale.US, "$%.2f", job.budget));
            tvStatus.setText(job.status == null ? "OPEN" : job.status);

            btnViewApplicants.setOnClickListener(v -> showApplicantsDialog(job));
        }
    }

    private void showApplicantsDialog(Job job) {
        View listView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_applicants_list, null);
        RecyclerView rv = listView.findViewById(R.id.rv_applicants);
        List<RecruitmentApplication> apps = new ArrayList<>();
        final ApplicantsAdapter applicantsAdapter = new ApplicantsAdapter(apps);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(applicantsAdapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Applicants")
                .setView(listView)
                .setNegativeButton("Close", null)
                .create();

        // load applications once
        repo.getFirestore().collection("applications")
                .whereEqualTo("postId", job.id)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(qs -> {
                    apps.clear();
                    for (DocumentSnapshot ds : qs.getDocuments()) {
                        RecruitmentApplication a = new RecruitmentApplication();
                        a.id = ds.getId();
                        a.postId = ds.getString("postId");
                        a.applicantId = ds.getString("applicantId");
                        a.applicantTeamId = ds.getString("applicantTeamId");
                        a.coverLetter = ds.getString("coverLetter");
                        a.status = ds.getString("status");
                        apps.add(a);
                    }
                    applicantsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to load applicants: " + e.getMessage(), Toast.LENGTH_LONG).show());

        dialog.show();
    }

    // Adapter for applicants list shown to client
    private class ApplicantsAdapter extends RecyclerView.Adapter<ApplicantsAdapter.VH> {
        private final List<RecruitmentApplication> items;
        ApplicantsAdapter(List<RecruitmentApplication> items) { this.items = items; }
        @NonNull @Override public ApplicantsAdapter.VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ApplicantsAdapter.VH(v);
        }
        @Override public void onBindViewHolder(@NonNull ApplicantsAdapter.VH holder, int position) {
            RecruitmentApplication a = items.get(position);
            holder.title.setText(getString(R.string.applicant_title_status, a.applicantId == null ? "unknown" : a.applicantId, a.status == null ? "" : a.status));
            holder.subtitle.setText(a.coverLetter == null ? "" : a.coverLetter);
            holder.itemView.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                    .setTitle("Update application")
                    .setMessage(getString(R.string.applicant_status_message, a.applicantId, a.status))
                    .setPositiveButton("Hire", (d, w) -> updateApplicationStatus(a.id, "HIRED"))
                    .setNegativeButton("Reject", (d, w) -> updateApplicationStatus(a.id, "REJECTED"))
                    .setNeutralButton("Close", null)
                    .show());
        }
        @Override public int getItemCount() { return items.size(); }
        class VH extends RecyclerView.ViewHolder {
            android.widget.TextView title, subtitle;
            VH(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(android.R.id.text1);
                subtitle = itemView.findViewById(android.R.id.text2);
            }
        }
    }

    private void updateApplicationStatus(String applicationId, String newStatus) {
        repo.getFirestore().collection("applications").document(applicationId)
                .update("status", newStatus, "updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp())
                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Application updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // lightweight POJO used locally for applicants if your app doesn't already define it
    public static class RecruitmentApplication {
        public String id;
        public String postId;
        public String applicantId;
        public String applicantTeamId;
        public String coverLetter;
        public String status;
    }
}