package com.example.freelanceflowandroid.data.model;

import java.io.Serializable;
import java.util.Objects;

public class UserProfile implements Serializable {
    public String uid;
    public String name;
    public String email;
    public String role; // CLIENT | FREELANCER | TEAM_ADMIN | TEAM_MEMBER
    public String teamId;

    public UserProfile() {}

    public UserProfile(String uid, String name, String email, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserProfile)) return false;
        UserProfile that = (UserProfile) o;
        return Objects.equals(uid, that.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }
}

