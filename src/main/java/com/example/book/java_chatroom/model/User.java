package com.example.book.java_chatroom.model;


import lombok.Data;

import java.util.Date;


@Data
public class User {
    private int userId;
    private String username;
    private String password;
    private Date createTime;
    private Date updateTime;

}
