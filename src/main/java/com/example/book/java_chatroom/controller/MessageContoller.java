package com.example.book.java_chatroom.controller;

import com.example.book.java_chatroom.model.Message;
import com.example.book.java_chatroom.model.Result;
import com.example.book.java_chatroom.model.User;
import com.example.book.java_chatroom.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MessageContoller {
    @Autowired
    private MessageService messageService;
    @GetMapping("/message")
    @ResponseBody
    public Object getMessages(@RequestParam int sessionId, HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return Result.nologin();
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.nologin();
        }
        List<Message> messages = messageService.getMessagesBySessionId(sessionId);
        // SQL 已经按 postTime asc 排序，不需要 reverse
        return Result.success(messages);
    }
}
