package com.example.freelanceflowandroid.data.model;

import java.io.Serializable;

public class Message implements Serializable {
    public String id;
    public String senderId;
    public String text;
    public Long createdAt;

    public Message() {}

    public Message(String senderId, String text) {
        this.senderId = senderId;
        this.text = text;
    }
}

