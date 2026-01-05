package com.example.freelanceflowandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.freelanceflowandroid.model.RecruitmentApplication;

import java.util.List;

public class TeamApplicationsAdapter extends RecyclerView.Adapter<TeamApplicationsAdapter.Holder> {

    public interface OnHireClick { void onHire(String applicationId); }

    private final List<RecruitmentApplication> items;
    private final OnHireClick hireClick;

    public TeamApplicationsAdapter(List<RecruitmentApplication> items, OnHireClick hireClick) {
        this.items = items;
        this.hireClick = hireClick;
    }

    public void updateList(List<RecruitmentApplication> newList) {
        items.clear();
        items.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_application, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        RecruitmentApplication a = items.get(position);
        holder.name.setText(a.freelancerName);
        holder.status.setText(a.status.name());
        holder.btnHire.setEnabled(a.status == RecruitmentApplication.Status.PENDING);
        holder.btnHire.setOnClickListener(v -> hireClick.onHire(a.id));
    }

    @Override public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name, status;
        Button btnHire;
        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_app_name);
            status = itemView.findViewById(R.id.tv_app_status);
            btnHire = itemView.findViewById(R.id.btn_hire);
        }
    }
}