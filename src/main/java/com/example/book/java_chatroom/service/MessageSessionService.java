package com.example.book.java_chatroom.service;

import com.example.book.java_chatroom.mapper.MessageMapper;
import com.example.book.java_chatroom.mapper.MessageSessionMapper;
import com.example.book.java_chatroom.model.Friend;
import com.example.book.java_chatroom.model.MessageSession;
import com.example.book.java_chatroom.model.MessageSessionUserItem;
import com.example.book.java_chatroom.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Service

public class MessageSessionService {
    @Autowired
    private MessageSessionMapper messageSessionMapper;
    @Autowired
    private MessageMapper messageMapper;

    public List<MessageSession> getFriendsMessage(HttpServletRequest req) {


        List<MessageSession> messageSessionList = new ArrayList<>();
        HttpSession session = req.getSession(false);
        if ((session == null)) {
            log.error("会话不存在");
            return messageSessionList;
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            log.error("用户不存在");
            return messageSessionList;
        }
        //根据用户id查询数据库，查出来看有什么会话id
        List<Integer> sessionIdList = messageSessionMapper.getSessionIdByUserId(user.getUserId());
        // 使用 Map 来存储每个好友对应的最新会话（按好友ID分组）
        Map<Integer, MessageSession> friendSessionMap = new HashMap<>();
        
        for (Integer sessionId : sessionIdList) {
            //3.遍历会话id，查询出每个会话涉及的好友都有谁
            List<Friend> friends = messageSessionMapper.getFriendIdBySessionId(sessionId, user.getUserId());
            if (friends == null || friends.isEmpty()) {
                continue;
            }
            // 获取第一个好友（单聊场景，每个会话只有一个对方好友）
            Friend friend = friends.get(0);
            // 将 friendId 转换为整数用于比较
            int friendId = friend.getFriendId();
            
            //4.查询每个会话的最新消息
            String lastMessage = messageMapper.getLastMessageBySessionId(sessionId);
            //新会话可能没消息
            if (lastMessage == null || lastMessage.trim().isEmpty()) {
                lastMessage = "";
            }
            log.debug("会话 {} 的最后一条消息: {}", sessionId, lastMessage);
            
            // 检查这个好友是否已经有会话了
            if (friendSessionMap.containsKey(friendId)) {
                // 如果已有会话，比较两个会话，保留有消息的或最新的
                MessageSession existingSession = friendSessionMap.get(friendId);
                String existingLastMessage = existingSession.getLastMessage();
                // 如果当前会话有消息，而已有会话没有消息，则替换
                // 或者如果两个都有消息，保留第一个（因为已经按时间排序）
                if (!lastMessage.isEmpty() && (existingLastMessage == null || existingLastMessage.isEmpty())) {
                    // 当前会话有消息，已有会话没有，替换
                    MessageSession messageSession = new MessageSession();
                    messageSession.setSessionId(sessionId);
                    messageSession.setFriends(friends);
                    messageSession.setLastMessage(lastMessage);
                    friendSessionMap.put(friendId, messageSession);
                }
                // 否则保留已有的（因为已经按时间排序，第一个就是最新的）
            } else {
                // 新好友，创建会话对象
                MessageSession messageSession = new MessageSession();
                messageSession.setSessionId(sessionId);
                messageSession.setFriends(friends);
                messageSession.setLastMessage(lastMessage);
                friendSessionMap.put(friendId, messageSession);
            }
        }
        // 将 Map 中的值转换为 List
        messageSessionList.addAll(friendSessionMap.values());
        return messageSessionList;
        }


       @Transactional
        public int createSession(int toUserId, @SessionAttribute("user") User user) {
        //1.给message_session表插入记录,使其能获得sessionId
        MessageSession messageSession = new MessageSession();
        messageSessionMapper.addMessageSession(messageSession);
        //2.给message_session_user表插入记录
        MessageSessionUserItem item1 = new MessageSessionUserItem();
        item1.setSessionId(messageSession.getSessionId());
        item1.setUserId(user.getUserId());
        messageSessionMapper.addMessageSessionUser(item1);
        //3.给message_session_user表插入记录
        MessageSessionUserItem item2 = new MessageSessionUserItem();
        item2.setSessionId(messageSession.getSessionId());
        item2.setUserId(toUserId);
        messageSessionMapper.addMessageSessionUser(item2);

        return messageSession.getSessionId();

        }
    


}
