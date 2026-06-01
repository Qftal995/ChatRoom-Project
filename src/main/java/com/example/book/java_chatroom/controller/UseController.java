package com.example.book.java_chatroom.controller;

import com.example.book.java_chatroom.model.Result;
import com.example.book.java_chatroom.model.User;
import com.example.book.java_chatroom.service.UseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;
import java.util.HashMap;

import java.util.DuplicateFormatFlagsException;

@Slf4j
@RestController
public class UseController {
    @Autowired
    private UseService useService;


    @PostMapping("/login")
    @ResponseBody
    public Result login(String username, String password
            , HttpServletRequest req) {
        //1.参数校验
        if (!StringUtils.hasLength(username) ||
                !StringUtils.hasLength(password)) {
            return Result.nologin();
        }
        //2清理旧会话
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        //3.查询判断
        User user = useService.selectByUsername(username);

        if (user == null) {
            log.error("用户不存在");
            return Result.fail("用户不存在");
        }
        if (!user.getPassword().equals(password))
            {
                log.error("密码错误");
                return Result.fail("密码错误");
            }
        //4.如果匹配的话,建立会话
        session = req.getSession(true);//避免空指针异常
        session.setAttribute("user", user);
        log.info("用户{}登陆成功,sessionId:{}",username,session.getId());
        return Result.success(user);
    }



    @PostMapping("/register")
    public Object register(@RequestBody User user) {  // 直接接收User对象

            try {
                // 打印接收到的数据
                System.out.println("接收到的用户数据：" + user);

                // 1. 参数验证
                if (user.getUsername() == null || user.getPassword() == null) {
                    return Result.fail("用户名和密码不能为空");
                }

                // 2. 检查用户名是否已存在
                User existUser = useService.selectByUsername(user.getUsername());
                if (existUser != null) {
                    System.out.println("用户名已存在：" + user.getUsername());
                    return Result.fail("用户名已存在");
                }

                // 3. 注册新用户
                System.out.println("开始注册新用户：" + user.getUsername());
                useService.insertUser(user);

                // 4. 返回成功（不包含密码）
                user.setPassword(null);
                return Result.success(user);

            } catch (Exception e) {
                System.out.println("注册失败：" + e.getMessage());
                e.printStackTrace();  // 打印完整错误信息
                return Result.fail("注册失败：" + e.getMessage());
            }
        }

        @GetMapping("/userInfo")
        @ResponseBody
        public  Object getUserInfo(HttpServletRequest req) {
           HttpSession session = req.getSession(false);

            if (session == null){
                log.error("当前获取不到session信息");
                return Result.nologin();
            }
            User user = (User) session.getAttribute("user");
            if (user == null)
            {
                log.error("当前获取不到user信息");
                return Result.nologin();
            }
                return Result.success(user);
        }
}