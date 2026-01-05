package com.example.freelanceflowandroid.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.freelanceflowandroid.model.Team;
import com.example.freelanceflowandroid.model.RecruitmentNotice;
import com.example.freelanceflowandroid.model.RecruitmentApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Simple in-memory repo. Put this class in:
 * app/src/main/java/com/example/freelanceflowandroid/data/model/InMemoryRepo.java
 *
 * NOTE: adapt postNotice(...) creation of RecruitmentNotice to match your model's constructor.
 */
public class InMemoryRepo {

    private static InMemoryRepo instance;
    public static synchronized InMemoryRepo get() {
        if (instance == null) instance = new InMemoryRepo();
        return instance;
    }

    private final Map<String, Team> teams = new HashMap<>();
    private final MutableLiveData<List<RecruitmentNotice>> noticesLive = new MutableLiveData<>(new ArrayList<>());
    private final Map<String, MutableLiveData<List<RecruitmentApplication>>> applicationsMap = new HashMap<>();

    public LiveData<List<RecruitmentNotice>> getNotices() {
        return noticesLive;
    }

    public synchronized LiveData<List<RecruitmentApplication>> getApplicationsFor(String noticeId) {
        MutableLiveData<List<RecruitmentApplication>> ml = applicationsMap.get(noticeId);
        if (ml == null) {
            ml = new MutableLiveData<>(new ArrayList<>());
            applicationsMap.put(noticeId, ml);
        }
        return ml;
    }

    public synchronized boolean createTeam(String teamId, String teamName, String adminId) {
        if (teamId == null || teamId.trim().isEmpty()) return false;
        if (teams.containsKey(teamId)) return false;
        Team t = new Team(teamId, teamName != null ? teamName : teamId, adminId);
        teams.put(teamId, t);
        return true;
    }

    public synchronized boolean addTeamMember(String teamId, String userId) {
        Team t = teams.get(teamId);
        if (t == null) return false;
        if (t.isMember(userId)) return false;
        t.addMember(userId);
        return true;
    }

    public synchronized boolean isAdmin(String teamId, String userId) {
        Team t = teams.get(teamId);
        if (t == null) return false;
        String admin = t.getAdminId();
        return admin != null && admin.equals(userId);
    }

    public synchronized Team getTeam(String teamId) {
        return teams.get(teamId);
    }

    /**
     * Post a recruitment notice for a team.
     * This code assumes your RecruitmentNotice class has a constructor or public fields that can be set.
     * If your RecruitmentNotice has a different API, adapt this block accordingly.
     */
    public synchronized void postNotice(String teamId, String title, String description, List<String> tags) {
        String id = UUID.randomUUID().toString();
        int teamSize = 0;
        Team team = teams.get(teamId);
        if (team != null) teamSize = team.getMembers().size();

        RecruitmentNotice notice = new RecruitmentNotice(id, teamId, title, description, tags, System.currentTimeMillis());
        notice.teamMemberCount = teamSize <= 0 ? 1 : teamSize;

        List<RecruitmentNotice> current = noticesLive.getValue();
        if (current == null) current = new ArrayList<>();
        current.add(0, notice); // newest first
        noticesLive.postValue(current);
        applicationsMap.putIfAbsent(id, new MutableLiveData<>(new ArrayList<>()));
    }

    // simple applyToNotice (adds applicant id/name) — adapt to your model if it stores applicants differently
    public synchronized boolean applyToNotice(String noticeId, String freelancerId, String freelancerName) {
        MutableLiveData<List<RecruitmentApplication>> ml = (MutableLiveData<List<RecruitmentApplication>>) getApplicationsFor(noticeId);
        List<RecruitmentApplication> current = ml.getValue();
        if (current == null) current = new ArrayList<>();
        for (RecruitmentApplication a : current) {
            if (a != null && freelancerId != null && freelancerId.equals(a.freelancerId)) {
                return false;
            }
        }
        RecruitmentApplication app = new RecruitmentApplication(UUID.randomUUID().toString(), noticeId, freelancerId, freelancerName, System.currentTimeMillis());
        current.add(0, app);
        ml.postValue(current);
        return true;
    }

    public synchronized boolean withdrawApplication(String noticeId, String freelancerId) {
        MutableLiveData<List<RecruitmentApplication>> ml = (MutableLiveData<List<RecruitmentApplication>>) getApplicationsFor(noticeId);
        List<RecruitmentApplication> current = ml.getValue();
        if (current == null || current.isEmpty()) return false;
        boolean removed = false;
        for (int i = current.size() - 1; i >= 0; i--) {
            RecruitmentApplication a = current.get(i);
            if (a != null && freelancerId != null && freelancerId.equals(a.freelancerId)) {
                current.remove(i);
                removed = true;
            }
        }
        if (removed) ml.postValue(current);
        return removed;
    }

    public synchronized boolean hireApplicant(String noticeId, String applicationId) {
        MutableLiveData<List<RecruitmentApplication>> ml = (MutableLiveData<List<RecruitmentApplication>>) getApplicationsFor(noticeId);
        List<RecruitmentApplication> current = ml.getValue();
        if (current == null || current.isEmpty()) return false;
        boolean updated = false;
        for (RecruitmentApplication a : current) {
            if (a != null && applicationId != null && applicationId.equals(a.id)) {
                a.status = RecruitmentApplication.Status.HIRED;
                updated = true;
                break;
            }
        }
        if (updated) ml.postValue(current);
        return updated;
    }
}