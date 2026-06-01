package com.example.book.java_chatroom.controller;

import com.example.book.java_chatroom.model.Friend;
import com.example.book.java_chatroom.model.Result;
import com.example.book.java_chatroom.model.User;
import com.example.book.java_chatroom.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class FriendController {
    @Autowired
    private FriendService friendService;


    @GetMapping("/friendList")
    @ResponseBody
    public Object friendList(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if ((session==null))
        {
          log.error("会话不存在");
          return Result.fail("会话不存在");
        }
        User user = (User) session.getAttribute("user");
        if (user==null){
            log.error("用户不存在");
            return Result.fail("用户不存在");
        }
        List<Friend> friendList = friendService.selectFriendList(user.getUserId());
        return Result.success(friendList);

    }

}
