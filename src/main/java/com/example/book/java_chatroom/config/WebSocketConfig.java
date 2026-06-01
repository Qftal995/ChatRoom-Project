package com.example.book.java_chatroom.config;



import com.example.book.java_chatroom.controller.WebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
//    @Autowired
//    private TestWebSocketController testWebSocketController;
    @Autowired
    private WebSocketController webSocketController;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        //通过这个方法关联把Handler类给注册到具体的路径上
        //此时当浏览器，websocket的请求路径是/test时，就会调用testWebSocketController来处理
        //registry.addHandler(testWebSocketController, "/test");
        registry.addHandler( webSocketController, "/webSocketMessage")
                //通过注册这个特定的HttpSession拦截器，就可以把用户名给HttpSession中添加的Attribute键值
                // 对往webSocketSession也添加一份
                .addInterceptors(new HttpSessionHandshakeInterceptor());

    }
}
