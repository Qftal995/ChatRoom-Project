package com.example.book.java_chatroom.mapper;

import com.example.book.java_chatroom.model.Message;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    String getLastMessageBySessionId(int sessionId);
    
    List<Message> getMessagesBySessionId(int sessionId);

    @Insert("INSERT INTO message(sessionId, content,fromId,postTime) " +
            "VALUES(#{sessionId}, #{content}, #{fromId},now())")
    void add(Message message);
}
