package com.example.book.java_chatroom.model;

import lombok.Data;

import java.util.Date;

@Data
public  class Friend {
    private int friendId;
    private String friendName;
    private Date CreateTime;
    private String UpdateTime;
}
