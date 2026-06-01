package com.example.book.java_chatroom.mapper;

import com.example.book.java_chatroom.model.Friend;
import com.example.book.java_chatroom.model.MessageSession;
import com.example.book.java_chatroom.model.MessageSessionUserItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface MessageSessionMapper {

    //1.根据userId获取该用户在那些会话存在，返回是一组sessionId
    List<Integer> getSessionIdByUserId(int userId);

    //2.根据sessionId来获取这个会话中包含那些用户（除了自己）
    List<Friend> getFriendIdBySessionId(int sessionId, int selfUserId);

    int addMessageSession(MessageSession messageSession);

    //message_session_user表新增对应的记录
    void addMessageSessionUser(MessageSessionUserItem messageSessionUserItem);

}
