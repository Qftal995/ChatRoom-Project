package com.example.book.java_chatroom.controller;

import com.example.book.java_chatroom.api.MessageRequest;
import com.example.book.java_chatroom.api.MessageResponse;
import com.example.book.java_chatroom.component.OnlineUserManager;
import com.example.book.java_chatroom.mapper.MessageMapper;
import com.example.book.java_chatroom.mapper.MessageSessionMapper;
import com.example.book.java_chatroom.model.Friend;
import com.example.book.java_chatroom.model.Message;
import com.example.book.java_chatroom.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class WebSocketController extends TextWebSocketHandler {

    @Autowired
    private OnlineUserManager onlineUserManager;
    @Autowired
    private MessageSessionMapper messageSessionMapper;
    @Autowired
    private MessageMapper messageMapper;

    private ObjectMapper objectMapper = new ObjectMapper();



    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //这个方法会在websocket连接建立成功后，比自动调用
        //这个方法会在websocket连接建立成功后自动调用

        log.info("TestAPI 连接成功！");
        User user =(User) session.getAttributes().get("user");//前面注册了拦截器就可以
        if (user==null){
            return;
        }
        log.info("获取到的UserId{}", user.getUserId());
        onlineUserManager.online(user.getUserId(), session);

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        log.info("TestAPI 收到消息：{}", message.toString());
        User user =(User) session.getAttributes().get("user");
        if (user==null){
            log.info("WebSocketController user==null!! 未登录用户，无法转发消息 ");
            return;
        }
        //针对请求进行解析，把json格式的字符串，转化为java中的一个对象
        MessageRequest req=objectMapper.readValue(message.getPayload(), MessageRequest.class);
        if(req.getType().equals("message")){
            //转发消息
            transferMessage(user,req);
        }else {
            log.info("WebSocketController 收到消息类型错误：{}", req.getType());
        }

    }
    //通过这个方法来进行实际的消息的转发工作
    private void transferMessage(User fromUser,MessageRequest req) throws IOException {
        //1.先构造一个代转发的响应对象
        MessageResponse resp = new MessageResponse();
        resp.setType("message");
        resp.setContent(req.getContent());
        resp.setFromName(fromUser.getUsername());
        resp.setFromId(fromUser.getUserId());
        resp.setSessionId(req.getSessionId());
        //2.转化为json
         String respJson= objectMapper.writeValueAsString(resp);
         log.info("[transferMessage] respJson：{}", respJson);
         //3.根据请求的sessionId，获取这个MessageSession有那些用户，通过数据库查询
        List<Friend> friends = messageSessionMapper.getFriendIdBySessionId(req.getSessionId(), fromUser.getUserId());
         //由于这个sql查询不包括自己，所以要把自己加进去
        Friend myself = new Friend();
        myself.setFriendId(fromUser.getUserId());
        myself.setFriendName(fromUser.getUsername());
        friends.add(myself);
        //4.遍历friends，把消息转发给每一个用户
        for (Friend friend : friends) {
            WebSocketSession webSocketSession = onlineUserManager.getSession(friend.getFriendId());
            if (webSocketSession==null){
                continue;
            }
            webSocketSession.sendMessage(new TextMessage(respJson));
        }
        //5.转发的消息还要放到数据库中，后续如果用户下线之后重新上线，还可以通过历史消息的方式去拿到之前的消息
        //向message表里去插入一条记录
        Message message = new Message();
        message.setFromId(fromUser.getUserId());
        message.setSessionId(req.getSessionId());
        message.setContent(req.getContent());
        messageMapper.add(message);

    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        //这个方法会在连接出错时自动调用
        log.info("TestAPI 连接出错！{}", exception.getMessage());
        User user =(User) session.getAttributes().get("user");
        if (user==null){
            return;
        }
        onlineUserManager.offline(user.getUserId(), session);

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //这个方法会在连接关闭时自动调用
        log.info("TestAPI 连接关闭！{}", status.toString());
        User user =(User) session.getAttributes().get("user");
        if (user==null){
            return;
        }
        onlineUserManager.offline(user.getUserId(), session);
    }
}
