package com.example.freelanceflowandroid.data.model;

import java.io.Serializable;
import java.util.List;

public class Job implements Serializable {
    public String id;
    public String title;
    public String description;
    public String clientId;
    public Double budget;
    public String status; // OPEN, IN_PROGRESS, CLOSED
    public Long createdAt;
    public List<String> attachments;

    public Job() {}

    public Job(String title, String description, String clientId, Double budget) {
        this.title = title;
        this.description = description;
        this.clientId = clientId;
        this.budget = budget;
        this.status = "OPEN";
    }
}

