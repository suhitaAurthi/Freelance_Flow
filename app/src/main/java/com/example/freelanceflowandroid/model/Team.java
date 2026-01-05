package com.example.freelanceflowandroid.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Team {
    private final String id;
    private final String name;
    private final String adminId;
    private final Set<String> members = new HashSet<>();

    public Team(String id, String name, String adminId) {
        this.id = id;
        this.name = name;
        this.adminId = adminId;
        if (adminId != null) members.add(adminId);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAdminId() { return adminId; }

    public synchronized void addMember(String userId) { if (userId != null) members.add(userId); }
    public synchronized Set<String> getMembers() { return Collections.unmodifiableSet(members); }
    public synchronized boolean isMember(String userId) { return members.contains(userId); }
}