create  database if not exists java_chatroom charset utf8;

use java_chatroom;
-- 表1
drop table if exists user;
     create table user(
         userId int primary key auto_increment,
         username varchar(20) unique ,
         password varchar(20),
         createTime datetime,
         updateTime datetime
     );
insert into user values(1,'qft','123',now(),now());
insert into user values(2,'zhemu','123',now(),now());
insert into user values(3,'zhangsan','1234',now(),now());
insert into user values(4,'lisi','1234',now(),now());

-- 表2
drop table if exists friend;
create table friend(
    userId int,
    friendId int
);
insert into friend values(1,2);
insert into friend values(2,1);
insert into friend values(1,3);
insert into friend values(3,1);
insert into friend values(1,4);
insert into friend values(4,1);

-- 表3:会话表
drop table if exists message_session;
create table message_session(
    sessionId int primary key auto_increment,
    lastTime datetime
);
insert into message_session values(1,'2015-01-01 00:00:00');
insert into message_session values(2,'2015-03-01 00:00:00');



-- 表4:创建会话和用户的关联表
drop table if exists message_session_user;
create table message_session_user
(
    sessionId int,
    userId int
);
insert into message_session_user values(1,1),(1,2);
insert into message_session_user values(2,1),(2,3);

--表5:消息表
drop table if exists message;
create table message(
    messageId int primary key auto_increment,
    fromId int,
    sessionId int,
    content varchar(2048),
    postTime datetime
);
-- 用户1和用户2在会话1消息
insert into message values(1,1,1,'今晚吃啥','2015-01-01 12:01:00');
insert into message values(2,2,1,'随便','2015-01-01 12:02:00');
insert into message values(3,1,1,'火锅','2015-01-01 12:03:00');
insert into message values(4,2,1,'不吃','2015-01-01 12:04:00');
insert into message values(5,1,1,'那你想吃什么','2015-01-01 12:05:00');
insert into message values(6,2,1,'随便','2015-01-01 12:06:00');
-- 用户1和用户3在会话2消息
insert into message values(7,1,2,'晚上一起吃','2015-01-01 12:07:00');




