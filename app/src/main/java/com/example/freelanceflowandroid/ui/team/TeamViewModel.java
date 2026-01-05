package com.example.freelanceflowandroid.ui.team;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.freelanceflowandroid.data.FirestoreRepository;
import com.example.freelanceflowandroid.data.Resource;
import com.example.freelanceflowandroid.data.model.Team;

public class TeamViewModel extends AndroidViewModel {
    private final FirestoreRepository repo;

    public TeamViewModel(@NonNull Application application) {
        super(application);
        repo = new FirestoreRepository(application.getApplicationContext());
    }

    public LiveData<Resource<String>> createTeam(Team team, String adminUid) {
        return repo.createTeam(team, adminUid);
    }

    public LiveData<Resource<Boolean>> joinTeam(String teamId, String userId) {
        return repo.joinTeam(teamId, userId);
    }
}
