package com.example.freelanceflowandroid.model;

import java.io.Serializable;

public class RecruitmentApplication implements Serializable {
    public enum Status { PENDING, HIRED, REJECTED }

    public final String id;
    public final String noticeId;
    public final String freelancerId;
    public final String freelancerName;
    public final long appliedAt;
    public Status status;

    public RecruitmentApplication(String id, String noticeId, String freelancerId, String freelancerName, long appliedAt) {
        this.id = id;
        this.noticeId = noticeId;
        this.freelancerId = freelancerId;
        this.freelancerName = freelancerName;
        this.appliedAt = appliedAt;
        this.status = Status.PENDING;
    }
}