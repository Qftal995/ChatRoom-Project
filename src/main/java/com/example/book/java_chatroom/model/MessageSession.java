package com.example.book.java_chatroom.model;

import lombok.Data;

import java.awt.*;
import java.util.List;
@Data
//用这个类来表示一个会话
public class MessageSession {
    private  int sessionId;
    private List<Friend> friends;
    private String lastMessage;
}
