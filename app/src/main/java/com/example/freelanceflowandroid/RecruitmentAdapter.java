package com.example.freelanceflowandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.freelanceflowandroid.R;
import com.example.freelanceflowandroid.data.InMemoryRepo;
import com.example.freelanceflowandroid.model.RecruitmentNotice;
import com.example.freelanceflowandroid.model.RecruitmentApplication;

import java.util.List;

public class RecruitmentAdapter extends RecyclerView.Adapter<RecruitmentAdapter.Holder> {
    public interface ApplyListener { void onApply(String noticeId); }

    private final List<RecruitmentNotice> items;
    private final ApplyListener listener;
    private final InMemoryRepo repo;
    private final String freelancerId;

    public RecruitmentAdapter(List<RecruitmentNotice> items, ApplyListener listener, InMemoryRepo repo, String freelancerId) {
        this.items = items;
        this.listener = listener;
        this.repo = repo;
        this.freelancerId = freelancerId;
    }

    /** Replace the current items and refresh the list. */
    public void updateList(List<RecruitmentNotice> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recruitment_notice, parent, false);
        return new Holder(view, listener, repo, freelancerId);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        RecruitmentNotice notice = items.get(position);
        holder.bind(notice);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView descView;
        private final TextView tagsView;
        private final TextView memberCountView;
        private final Button applyButton;
        private final ApplyListener listener;
        private final InMemoryRepo repo;
        private final String freelancerId;

        public Holder(@NonNull View itemView, ApplyListener listener, InMemoryRepo repo, String freelancerId) {
            super(itemView);
            this.listener = listener;
            this.repo = repo;
            this.freelancerId = freelancerId;
            titleView = itemView.findViewById(R.id.notice_title);
            descView = itemView.findViewById(R.id.notice_desc);
            tagsView = itemView.findViewById(R.id.notice_tags);
            memberCountView = itemView.findViewById(R.id.notice_member_count);
            applyButton = itemView.findViewById(R.id.apply_button);
        }

        public void bind(RecruitmentNotice notice) {
            titleView.setText(notice.title);
            descView.setText(notice.description);
            tagsView.setText(notice.tags != null ? String.join(", ", notice.tags) : "");
            memberCountView.setText(itemView.getContext().getString(R.string.team_member_count, notice.teamMemberCount));

            applyButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApply(notice.id);
                }
            });

            // Check if already applied
            List<RecruitmentApplication> applications = repo.getApplicationsFor(notice.id).getValue();
            boolean isApplied = false;
            if (applications != null) {
                for (RecruitmentApplication app : applications) {
                    if (app.freelancerId.equals(freelancerId)) {
                        isApplied = true;
                        break;
                    }
                }
            }
            applyButton.setEnabled(!isApplied);
        }
    }
}