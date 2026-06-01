package com.example.book.java_chatroom.service;
import com.example.book.java_chatroom.mapper.UserMapper;
import com.example.book.java_chatroom.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service

@Slf4j
public class UseService {
    @Autowired
    private UserMapper userMapper;

    public User selectByUsername(String username) {
        return userMapper.selectByUsername(username);
    }


    public boolean insertUser(User user) {

        try {
            Integer result = userMapper.insertUser(user);
            if (result==1){
                return true;
            }
        } catch (Exception e) {
            log.error("用户注册失败", e);
        }
            return false;
        }



}

