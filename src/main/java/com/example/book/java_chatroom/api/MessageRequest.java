package com.example.book.java_chatroom.api;

import lombok.Data;

@Data
public class MessageRequest {
    private String type ="message";
    private int sessionId;
    private String content;
}
