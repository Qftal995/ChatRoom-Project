package com.example.book.java_chatroom.api;

import lombok.Data;

@Data
public class MessageResponse {
    private String type ="message";
    private int fromId;
    private String fromName;
    private int sessionId;
    private String content;
}
