package com.example.freelanceflowandroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Runnable Freelancer dashboard.
 * - Shows a searchable list (RecyclerView) of Team offers (sample data).
 * - Replace sample data with your repository / adapter as needed.
 *
 * Keep class name DashboardFreelancer as requested.
 */
public class DashboardFreelancer extends AppCompatActivity {

    private RecyclerView rvJobs;
    private TextView tvEmpty;
    private androidx.appcompat.widget.SearchView searchView;
    private OfferAdapter adapter;
    private final List<Offer> allOffers = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dash_freelancer);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_freelancer);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Freelancer Dashboard");

        rvJobs = findViewById(R.id.rv_freelancer_jobs);
        tvEmpty = findViewById(R.id.tv_empty);
        searchView = findViewById(R.id.search_view);

        rvJobs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OfferAdapter(new ArrayList<Offer>());
        rvJobs.setAdapter(adapter);

        // Sample data (replace with real repo data)
        populateSampleOffers();
        adapter.updateList(new ArrayList<>(allOffers));
        updateEmptyState();

        // Search handling (simple filtering)
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String q) {
        if (TextUtils.isEmpty(q)) {
            adapter.updateList(new ArrayList<>(allOffers));
        } else {
            String lower = q.toLowerCase();
            List<Offer> filtered = new ArrayList<>();
            for (Offer o : allOffers) {
                if ((o.title != null && o.title.toLowerCase().contains(lower))
                        || (o.description != null && o.description.toLowerCase().contains(lower))
                        || (o.tags != null && o.tags.toLowerCase().contains(lower))) {
                    filtered.add(o);
                }
            }
            adapter.updateList(filtered);
        }
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvJobs.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvJobs.setVisibility(View.VISIBLE);
        }
    }

    private void populateSampleOffers() {
        allOffers.clear();
        allOffers.add(new Offer("o1", "Android Developer for Team", "Join our Android team for a 3-month project", "Android • Team"));
        allOffers.add(new Offer("o2", "Full-stack Team Looking", "Looking for frontend + backend freelancers", "React • Node • Team"));
        allOffers.add(new Offer("o3", "iOS Specialist Needed", "Help our team deliver iOS features", "iOS • Swift • Team"));
    }

    // Simple model for demo
    private static class Offer {
        final String id;
        final String title;
        final String description;
        final String tags;
        Offer(String id, String title, String description, String tags) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.tags = tags;
        }
    }

    // Simple adapter (you can replace with ListAdapter + DiffUtil)
    private class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.Holder> {
        private final List<Offer> items;

        OfferAdapter(List<Offer> list) { this.items = list; }

        void updateList(List<Offer> newList) {
            items.clear();
            items.addAll(newList);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Offer o = items.get(position);
            holder.title.setText(o.title);
            holder.subtitle.setText(o.tags);
            holder.itemView.setOnClickListener(v -> {
                // TODO: open offer detail screen. Replace with your freelancerDetail activity.
                // Example:
                try {
                    Intent i = new Intent(DashboardFreelancer.this, freelancerDetail.class);
                    i.putExtra("offer_id", o.id);
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(DashboardFreelancer.this, "Detail screen not found", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        class Holder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView subtitle;
            Holder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(android.R.id.text1);
                subtitle = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}