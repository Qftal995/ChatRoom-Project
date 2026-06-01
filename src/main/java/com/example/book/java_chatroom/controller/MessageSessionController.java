package com.example.book.java_chatroom.controller;

import com.example.book.java_chatroom.model.Message;
import com.example.book.java_chatroom.model.MessageSession;
import com.example.book.java_chatroom.model.Result;
import com.example.book.java_chatroom.model.User;
import com.example.book.java_chatroom.service.MessageSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class MessageSessionController {
     @Autowired
    private MessageSessionService messageSessionService;

     @GetMapping("/sessionlist")
     @ResponseBody
    public Object getMessageSessionList(HttpServletRequest req) {
         List<MessageSession> sessions = messageSessionService.getFriendsMessage(req);
         return Result.success(sessions);
     }



     @PostMapping("/session")
     @ResponseBody
     public Object addMessageSession(@RequestParam int toUserId, @SessionAttribute("user") User user) {
         try {
             int sessionId = messageSessionService.createSession(toUserId, user);
             Map<String, Integer> response = new HashMap<>();
             response.put("sessionId", sessionId);
             return Result.success(response);
         } catch (Exception e) {
             return Result.fail("创建会话失败: " + e.getMessage());
         }
     }
}
