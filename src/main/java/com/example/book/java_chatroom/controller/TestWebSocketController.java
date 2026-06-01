package com.example.book.java_chatroom.controller;

import com.example.book.java_chatroom.model.User;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//@Component
//public class TestWebSocketController extends TextWebSocketHandler {
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        //这个方法会在websocket连接建立成功后自动调用
//        System.out.println("TestAPI 连接成功！");
//        User user =(User) session.getAttributes().get("user");//前面注册了拦截器就可以
//        System.out.println("获取到的userId：" + user.getUserId());
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        //这个方法会在接收到消息后自动调用
//        System.out.println("TestAPI 收到消息：" + message.toString());
//       session.sendMessage(message);
//    }
//
//    @Override
//    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
//        //这个方法会在连接出错时自动调用
//        System.out.println("TestAPI 连接出错！");
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        //这个方法会在连接关闭时自动调用
//        System.out.println("TestAPI 连接关闭！");
//    }
//}
