package com.example.freelanceflowandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.freelanceflowandroid.model.RecruitmentNotice;

import java.util.List;

public class TeamNoticeAdapter extends RecyclerView.Adapter<TeamNoticeAdapter.Holder> {

    public interface NoticeClickListener { void onNoticeClicked(RecruitmentNotice notice); }

    private final List<RecruitmentNotice> items;
    private final NoticeClickListener listener;

    public TeamNoticeAdapter(List<RecruitmentNotice> items, NoticeClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateList(List<RecruitmentNotice> newList) {
        items.clear();
        items.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        RecruitmentNotice n = items.get(position);
        holder.title.setText(n.title);
        holder.subtitle.setText(n.teamMemberCount + " members • " + n.tags);
        holder.itemView.setOnClickListener(v -> listener.onNoticeClicked(n));
    }

    @Override public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            subtitle = itemView.findViewById(android.R.id.text2);
        }
    }
}