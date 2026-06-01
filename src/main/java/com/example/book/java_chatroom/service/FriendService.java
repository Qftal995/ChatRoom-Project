package com.example.book.java_chatroom.service;

import com.example.book.java_chatroom.mapper.FriendMapper;
import com.example.book.java_chatroom.model.Friend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FriendService {
    @Autowired
    private FriendMapper friendMapper;

    public List<Friend> selectFriendList(int userId) {
        return  friendMapper.selectFriendList(userId);
    }
}
