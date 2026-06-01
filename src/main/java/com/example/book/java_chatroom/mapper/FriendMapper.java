package com.example.book.java_chatroom.mapper;

import com.example.book.java_chatroom.model.Friend;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface FriendMapper {

    List<Friend> selectFriendList(int userId);
    //从登录的会话中获取useId，根据此查询好友列表
}
