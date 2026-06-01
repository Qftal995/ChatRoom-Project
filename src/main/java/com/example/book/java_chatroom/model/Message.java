package com.example.book.java_chatroom.model;

import lombok.Data;
import java.util.Date;

@Data
public class Message {
    private int messageId;
    private int fromId;
    private String fromName;  // 用于前端显示，需要关联查询
    private int sessionId;
    private String content;
    private Date postTime;
}

