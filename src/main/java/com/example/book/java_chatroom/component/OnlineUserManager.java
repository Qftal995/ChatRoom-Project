package com.example.book.java_chatroom.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Component
public class OnlineUserManager {

    private ConcurrentHashMap<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();
    //1.用户上线，给这个哈希表插入键值对

    public void online( int userId, WebSocketSession session) {
        if(sessions.get(userId)!=null) {
            //防止多开，此时就说明用户已经在线了，就登陆失败，不会记录这个映射关系（通过映射关系来实现消息转发的），就收不到任何消息
            System.out.println("[ "+userId+"]已经被登录，登陆失败 ");
          return;
        }
        this.sessions.put(userId, session);
        log.info("[ "+userId+"]上线了");
    }
    //2.用户下线
   public void offline(int userId, WebSocketSession session) {
       WebSocketSession existSession = sessions.get(userId);
       if(existSession!=null && existSession.equals(session)) {
           sessions.remove(userId);
           log.info("[ "+userId+"]下线了");
       }

   }
   //3.根据userId获取WebSocketSession
   public WebSocketSession getSession(int userId) {
        return sessions.get(userId);
   }
}
