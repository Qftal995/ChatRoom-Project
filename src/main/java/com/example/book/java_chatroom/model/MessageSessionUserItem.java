package com.example.book.java_chatroom.model;

import lombok.Data;

@Data
//用这个类来表示message_session_user表的一个记录
public class MessageSessionUserItem {
    private int sessionId;
    private int userId;
}
