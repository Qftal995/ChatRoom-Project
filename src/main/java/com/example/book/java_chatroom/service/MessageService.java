package com.example.book.java_chatroom.service;

import com.example.book.java_chatroom.mapper.MessageMapper;
import com.example.book.java_chatroom.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;
    public List<Message> getMessagesBySessionId(int sessionId) {
        return messageMapper.getMessagesBySessionId(sessionId);
    }
}
