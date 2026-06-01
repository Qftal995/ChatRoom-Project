# Java ChatRoom — 基于 WebSocket 的实时聊天室

## 项目简介

一个 **Spring Boot + WebSocket + MyBatis** 的实时在线聊天系统，支持用户注册登录、好友管理、会话列表、历史消息和 WebSocket 实时通信。纯后端驱动前端（静态 HTML 页面），无额外中间件依赖。

## 技术栈

| 层次 | 技术 |
|------|------|
| 框架 | Spring Boot 3.5.8 |
| 持久层 | MyBatis 3.0.5 |
| 数据库 | MySQL 8.x |
| 实时通信 | Spring WebSocket (裸 WebSocket 协议) |
| 会话管理 | HttpSession |
| 前端 | 原生 HTML + CSS + JS |
| 工具 | Lombok, Jackson |

## 功能模块

**用户系统**
- 注册/登录（HttpSession 保持登录态）
- 登录时清理旧会话，防止冲突

**好友系统**
- 好友列表查询
- 好友关系存储在 `friend` 表（双向记录）

**会话系统**
- 会话列表（含最后一条消息预览）
- 创建新会话（指定好友发起对话）
- 会话-用户多对多关联

**实时消息**
- WebSocket 长连接，路径 `/webSocketMessage`
- 消息转发：服务端收到消息 -> 查询会话成员 -> 逐个推送在线用户
- 消息持久化到 MySQL，下线后重新上线可拉取历史消息

## 数据库设计（5 张表）

```
user                 friend              message_session
+----------+        +----------+        +--------------+
| userId   |<-------| userId   |        | sessionId    |
| username |        | friendId |        | lastTime     |
| password |        +----------+        +--------------+
+----------+                               |
     ^                          +----------+----------+
     |                message_session_user            message
     +------------------| sessionId + userId  |      +--------------+
                        +---------------------+      | messageId    |
                                                     | fromId ------+--> user
                                                     | sessionId ---+--> message_session
                                                     | content      |
                                                     | postTime     |
                                                     +--------------+
```

## 核心设计

**消息转发流程**（`WebSocketController.transferMessage`）

```
客户端A发送消息
    |
    v
WebSocket 收到 JSON -> 解析 MessageRequest
    |
    v
查询 message_session_user 获取会话所有成员
    |
    v
遍历成员，从 OnlineUserManager 获取对应 WebSocketSession
    |
    v
逐个 push MessageResponse（含发送人、内容、会话ID）
    |
    v
消息写入 message 表（历史消息持久化）
```

**在线用户管理**（`OnlineUserManager`）

`ConcurrentHashMap<Integer, WebSocketSession>` 管理在线用户映射。同一用户重复登录时拒绝新连接（防止多开）。连接关闭或出错时自动移除。

## 快速开始

**环境要求**：JDK 17+、MySQL 8.x、Maven 3.x

```bash
# 1. 执行 SQL 初始化
mysql -u root -p < src/main/java/com/example/book/java_chatroom/d.sql

# 2. 修改 application.yml 中的数据库连接信息（如需要）

# 3. 启动
mvn spring-boot:run
```

访问 `http://localhost:8080/login.html`，测试账号：`qft / 123`、`zhemu / 123`

## 项目结构

```
java_chatroom
├── pom.xml
└── src/main
    ├── java/com/example/book/java_chatroom
    │   ├── JavaChatroomApplication.java    # 启动类
    │   ├── api/
    │   │   ├── MessageRequest.java         # WebSocket 请求体
    │   │   └── MessageResponse.java        # WebSocket 响应体
    │   ├── component/
    │   │   └── OnlineUserManager.java      # 在线用户管理（ConcurrentHashMap）
    │   ├── config/
    │   │   └── WebSocketConfig.java        # WebSocket 注册 + 拦截器
    │   ├── controller/
    │   │   ├── UseController.java          # /login, /register, /userInfo
    │   │   ├── FriendController.java       # /friendList
    │   │   ├── MessageSessionController.java # /sessionlist, /session
    │   │   ├── MessageContoller.java       # /message（历史消息）
    │   │   └── WebSocketController.java    # /webSocketMessage（实时通信）
    │   ├── enums/ResultStatus.java
    │   ├── mapper/                         # MyBatis Mapper 接口
    │   ├── model/                          # User, Friend, Message, MessageSession
    │   ├── service/                        # 业务逻辑层
    │   └── d.sql                           # 建表 + 示例数据
    └── resources
        ├── application.yml                 # 数据库连接 + MyBatis 配置
        ├── mapper/                         # Mapper XML
        └── static/                         # 前端页面（login/register/client）
```

## API 一览

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/login` | 登录 |
| POST | `/register` | 注册 |
| GET | `/userInfo` | 当前用户信息 |
| GET | `/friendList` | 好友列表 |
| GET | `/sessionlist` | 会话列表 |
| POST | `/session` | 创建会话 |
| GET | `/message` | 历史消息 |
| WS | `/webSocketMessage` | WebSocket 实时通信 |
