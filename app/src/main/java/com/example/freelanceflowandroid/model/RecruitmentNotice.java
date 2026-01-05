package com.example.freelanceflowandroid.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecruitmentNotice implements Serializable {
    public final String id;
    public final String teamId;      // id of team posting
    public String title;
    public String description;
    public List<String> tags = new ArrayList<>();
    public long createdAt;
    public int teamMemberCount = 1; // initial team size (team admin included)

    public RecruitmentNotice(String id, String teamId, String title, String description, List<String> tags, long createdAt) {
        this.id = id;
        this.teamId = teamId;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.createdAt = createdAt;
    }
}