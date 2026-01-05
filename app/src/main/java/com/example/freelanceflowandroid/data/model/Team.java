package com.example.freelanceflowandroid.data.model;

import java.io.Serializable;
import java.util.List;

public class Team implements Serializable {
    public String id;
    public String name;
    public String adminId;
    public List<String> memberIds;

    public Team() {}

    public Team(String name, String adminId) {
        this.name = name;
        this.adminId = adminId;
    }
}

