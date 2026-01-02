package com.example.freelanceflowandroid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.freelanceflowandroid.LoginActivity;
import com.example.freelanceflowandroid.PrefsManager;
import com.example.freelanceflowandroid.R;

public class ClientDashboardFragment extends Fragment {

    public ClientDashboardFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_client_dashboard, container, false);
        // use getIdentifier to avoid static layout lint mismatch if resources differ
        int signOutId = getResources().getIdentifier("btnSignOut", "id", requireContext().getPackageName());
        if (signOutId != 0) {
            Button btn = v.findViewById(signOutId);
            if (btn != null) {
                btn.setOnClickListener(view -> {
                    // clear role and go back to login
                    PrefsManager.getInstance(requireContext()).saveUserRole("");
                    Intent i = new Intent(requireContext(), LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    requireActivity().finish();
                });
            }
        }
        return v;
    }
}