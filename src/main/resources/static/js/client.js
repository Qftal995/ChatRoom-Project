////////////////////////////////////////////
// 这里实现标签页的切换
////////////////////////////////////////////

function initSwitchTab() {
    // 1. 先获取到相关的元素(标签页的按钮, 会话列表, 好友列表)
    let tabSession = document.querySelector('.tab .tab-session');
    let tabFriend = document.querySelector('.tab .tab-friend');
    // querySelectorAll 可以同时选中多个元素. 得到的结果是个数组
    // [0] 就是会话列表
    // [1] 就是好友列表
    let lists = document.querySelectorAll('.list');
    // 2. 针对标签页按钮, 注册点击事件. 
    //    如果是点击 会话标签按钮, 就把会话标签按钮的背景图片进行设置. 
    //    同时把会话列表显示出来, 把好友列表隐藏
    //    如果是点击 好友标签按钮, 就把好友标签按钮的背景图片进行设置. 
    //    同时把好友列表显示出来, 把会话列表进行隐藏
    tabSession.onclick = function() {
        // a) 设置图标
        tabSession.style.backgroundImage = 'url(img/对话.png)';
        tabFriend.style.backgroundImage = 'url(img/用户2.png)';
        // b) 让会话列表显示出来, 让好友列表进行隐藏
        lists[0].classList = 'list';
        lists[1].classList = 'list hide';
    }

    tabFriend.onclick = function() {
        // a) 设置图标
        tabSession.style.backgroundImage = 'url(img/对话2.png)';
        tabFriend.style.backgroundImage = 'url(img/用户.png)'
        // b) 让好友列表显示, 让会话列表隐藏
        lists[0].classList = 'list hide';
        lists[1].classList = 'list';
    }
}

initSwitchTab();

/////////////////////////////////////////////////////
// 统一处理后端 Result 响应
/////////////////////////////////////////////////////

function parseResult(body) {
    if (!body) {
        return { status: 'error', message: '服务器无响应' };
    }
    if (body.code === 'SUCCESS') {
        return { status: 'ok', data: body.data };
    }
    if (body.code === 'NOLOGIN') {
        return { status: 'nologin', message: body.errMessage || '当前用户未登录' };
    }
    return { status: 'error', message: body.errMessage || '请求失败' };
}

/////////////////////////////////////////////////////
// 操作 websocket
/////////////////////////////////////////////////////

// 创建 websocket 实例
let wsProtocol = location.protocol === 'https:' ? 'wss://' : 'ws://';
let websocket = new WebSocket(wsProtocol + location.host + "/webSocketMessage");

websocket.onopen = function() {
    console.log("websocket 连接成功!");
}

websocket.onmessage = function(e) {
    console.log("websocket 收到消息! " + e.data);
    // 此时收到的 e.data 是个 json 字符串, 需要转成 js 对象
    let resp = JSON.parse(e.data);
    if (resp.type == 'message') {
        // 处理消息响应
        handleMessage(resp);
    } else {
        // resp 的 type 出错!
        console.log("resp.type 不符合要求!");
    }
}

websocket.onclose = function() {
    console.log("websocket 连接关闭!");
}

websocket.onerror = function() {
    console.log("websocket 连接异常!");
}

function handleMessage(resp) {
    // 把客户端收到的消息, 给展示出来. 
    // 展示到对应的会话预览区域, 以及右侧消息列表中. 

    // 1. 根据响应中的 sessionId 获取到当前会话对应的 li 标签. 
    //    如果 li 标签不存在, 则创建一个新的
    let curSessionLi = findSessionLi(resp.sessionId);
    if (curSessionLi == null) {
        // 就需要创建出一个新的 li 标签, 表示新会话. 
        // 但首先需要从服务器获取会话信息，确保显示正确的对方名称
        // 这里先用 fromName 作为临时标题，后续可以通过刷新会话列表来更新
        curSessionLi = document.createElement('li');
        // 确保 sessionId 是数字类型
        let sessionId = parseInt(resp.sessionId);
        if (!isNaN(sessionId)) {
            curSessionLi.setAttribute('message-session-id', sessionId);
        } else {
            console.error('无效的 sessionId:', resp.sessionId);
            return;
        }
        // 使用发送者的名字作为临时标题
        // 注意：如果消息是自己发的，fromName 是自己的名字，需要从服务器获取正确的对方名字
        // 这里先使用 fromName，后续可以通过刷新会话列表来更新
        let displayName = resp.fromName || '未命名会话';
        curSessionLi.innerHTML = '<h3>' + displayName + '</h3>'
            + '<p></p>';
        // 给这个 li 标签也加上点击事件的处理
        curSessionLi.onclick = function() {
            clickSession(curSessionLi);
        }
        // 将新创建的会话添加到列表顶部
        let sessionListUL = document.querySelector('#session-list');
        sessionListUL.insertBefore(curSessionLi, sessionListUL.children[0]);
        // 延迟刷新会话列表以获取正确的会话信息（避免立即刷新导致闪烁）
        setTimeout(function() {
            getSessionList();
        }, 500);
        return; // 提前返回，避免下面的代码重复处理
    }
    // 2. 把新的消息, 显示到会话的预览区域 (li 标签里的 p 标签中)
    //    如果消息太长, 就需要进行截断. 
    let p = curSessionLi.querySelector('p');
    p.innerHTML = resp.content;
    if (p.innerHTML.length > 10) {
        p.innerHTML = p.innerHTML.substring(0, 10) + '...';
    }
    // 3. 把收到消息的会话, 给放到会话列表最上面. 
    let sessionListUL = document.querySelector('#session-list');
    sessionListUL.insertBefore(curSessionLi, sessionListUL.children[0]);
    // 4. 如果当前收到消息的会话处于被选中状态, 则把当前的消息给放到右侧消息列表中. 
    //    新增消息的同时, 注意调整滚动条的位置, 保证新消息虽然在底部, 但是能够被用户直接看到. 
    if (curSessionLi.className == 'selected') {
        // 把消息列表添加一个新消息. 
        let messageShowDiv = document.querySelector('.right .message-show');
        addMessage(messageShowDiv, resp);
        scrollBottom(messageShowDiv);
    }
    // 其他操作, 还可以在会话窗口上给个提示 (红色的数字, 有几条消息未读), 还可以播放个提示音.  
    // 这些操作都是纯前端的. 实现也不难, 不是咱们的重点工作. 暂时不做了. 
}

function findSessionLi(targetSessionId) {
    // 获取到所有的会话列表中的 li 标签
    let sessionLis = document.querySelectorAll('#session-list li');
    // 确保 targetSessionId 是数字类型用于比较
    let targetId = parseInt(targetSessionId);
    for (let li of sessionLis) {
        let sessionId = li.getAttribute('message-session-id');
        // 将字符串转换为数字进行比较
        if (sessionId && parseInt(sessionId) === targetId) {
            return li;
        }
    }
    // 啥时候会触发这个操作, 就比如如果当前新的用户直接给当前用户发送消息, 此时没存在现成的 li 标签
    return null;
}

/////////////////////////////////////////////////////
// 实现消息发送/接收逻辑
/////////////////////////////////////////////////////

function initSendButton() {
    // 1. 获取到发送按钮 和 消息输入框
    let sendButton = document.querySelector('.right .ctrl button');
    let messageInput = document.querySelector('.right .message-input');
    // 2. 给发送按钮注册一个点击事件
    sendButton.onclick = function() {
        // a) 先针对输入框的内容做个简单判定. 比如输入框内容为空, 则啥都不干
        if (!messageInput.value) {
            // value 的值是 null 或者 '' 都会触发这个条件
            return;
        }
        // b) 获取当前选中的 li 标签的 sessionId
        let selectedLi = document.querySelector('#session-list .selected');
        if (selectedLi == null) {
            // 当前没有 li 标签被选中. 
            return;
        }
        let sessionId = selectedLi.getAttribute('message-session-id');
        // 确保 sessionId 是数字类型
        sessionId = parseInt(sessionId);
        if (isNaN(sessionId)) {
            console.error('无效的 sessionId');
            return;
        }
        // c) 构造 json 数据
        let req = {
            type: 'message',
            sessionId: sessionId,
            content: messageInput.value
        };
        req = JSON.stringify(req);
        console.log("[websocket] send: " + req);
        if (websocket.readyState !== WebSocket.OPEN) {
            alert("当前 WebSocket 未连接, 消息发送失败");
            return;
        }
        // d) 通过 websocket 发送消息
        websocket.send(req);
        // e) 发送完成之后, 清空之前的输入框
        messageInput.value = '';
    }
}

initSendButton();


/////////////////////////////////////////////////////
// 从服务器获取到用户登录数据
/////////////////////////////////////////////////////

function getUserInfo() {
    $.ajax({
        type: 'get',
        url: 'userInfo',
        success: function(body) {
            let result = parseResult(body);
            if (result.status === 'ok' && result.data) {
                let userDiv = document.querySelector('.main .left .user');
                userDiv.innerHTML = result.data.username;
                userDiv.setAttribute("user-id", result.data.userId);
            } else if (result.status === 'nologin') {
                alert(result.message);
                location.assign('login.html');
            } else {
                alert(result.message || '获取用户信息失败');
                location.assign('login.html');
            }
        },
        error: function() {
            alert('获取用户信息失败!');
            location.assign('login.html');
        }
    });
}

getUserInfo();

function getFriendList() {
    $.ajax({
        type: 'get',
        url: 'friendList',
        success: function(body) {
            let result = parseResult(body);
            if (result.status !== 'ok') {
                if (result.status === 'nologin') {
                    alert(result.message);
                    location.assign('login.html');
                } else {
                    alert(result.message || '获取好友列表失败!');
                }
                return;
            }
            let friendListUL = document.querySelector('#friend-list');
            friendListUL.innerHTML = '';
            let friends = result.data || [];
            for (let friend of friends) {
                let li = document.createElement('li');
                li.innerHTML = '<h4>' + friend.friendName + '</h4>';
                li.setAttribute('friend-id', friend.friendId);
                friendListUL.appendChild(li);
                li.onclick = function() {
                    clickFriend(friend);
                }
            }
        },
        error: function() {
            console.log('获取好友列表失败!');
        }
    });
}

getFriendList();

function getSessionList() {
    $.ajax({
        type: 'get',
        url: 'sessionlist',
        success: function(body) {
            let result = parseResult(body);
            if (result.status !== 'ok') {
                if (result.status === 'nologin') {
                    alert(result.message);
                    location.assign('login.html');
                } else {
                    alert(result.message || '获取会话列表失败!');
                }
                return;
            }
            let sessionListUL = document.querySelector('#session-list');
            sessionListUL.innerHTML = '';
            let sessions = result.data || [];
            console.log('获取到的会话列表:', sessions);
            console.log('会话数量:', sessions.length);
            // 去重：使用 Map 来存储已处理的 sessionId
            let processedSessionIds = new Map();
            for (let session of sessions) {
                console.log('处理会话:', session);
                // 检查是否已经处理过这个 sessionId
                if (processedSessionIds.has(session.sessionId)) {
                    console.log('跳过重复的会话:', session.sessionId);
                    continue;
                }
                processedSessionIds.set(session.sessionId, true);
                // 检查 sessionId 是否存在
                if (session.sessionId === null || session.sessionId === undefined) {
                    console.error('会话数据缺少 sessionId:', session);
                    continue;
                }
                let previewMessage = session.lastMessage || '';
                console.log('会话最后一条消息:', previewMessage, 'sessionId:', session.sessionId);
                if (previewMessage && previewMessage.length > 10) {
                    previewMessage = previewMessage.substring(0, 10) + '...';
                }
                let friendName = '未命名会话';
                if (session.friends && session.friends.length > 0 && session.friends[0].friendName) {
                    friendName = session.friends[0].friendName;
                }
                let li = document.createElement('li');
                // 确保 sessionId 是数字类型
                let sessionId = parseInt(session.sessionId);
                if (!isNaN(sessionId) && sessionId > 0) {
                    li.setAttribute('message-session-id', sessionId);
                } else {
                    console.error('无效的 sessionId:', session.sessionId, '原始数据:', session);
                    continue;
                }
                li.innerHTML = '<h3>' + friendName + '</h3>' 
                    + '<p>' + previewMessage + '</p>';
                sessionListUL.appendChild(li);
                li.onclick = function() {
                    clickSession(li);
                }
            }
        }
    });
}

getSessionList();

function clickSession(currentLi) {
    // 1. 设置高亮
    let allLis = document.querySelectorAll('#session-list>li');
    activeSession(allLis, currentLi);
    // 2. 获取指定会话的历史消息 TODO
    let sessionId = currentLi.getAttribute("message-session-id");
    // 确保 sessionId 是数字类型
    sessionId = parseInt(sessionId);
    if (isNaN(sessionId)) {
        console.error('无效的 sessionId');
        return;
    }
    getHistoryMessage(sessionId);
}

function activeSession(allLis, currentLi) {
    // 这里的循环遍历, 更主要的目的是取消未被选中的 li 标签的 className
    for (let li of allLis) {
        if (li == currentLi) {
            li.className = 'selected';
        } else {
            li.className = '';
        }
    }
}

// 这个函数负责获取指定会话的历史消息. 
function getHistoryMessage(sessionId) {
    // 检查 sessionId 是否有效
    if (sessionId === null || sessionId === undefined || isNaN(parseInt(sessionId))) {
        console.error("无效的 sessionId:", sessionId);
        alert('无法获取历史消息：会话ID无效');
        return;
    }
    console.log("获取历史消息 sessionId=" + sessionId);
    // 1. 先清空右侧列表中的已有内容
    let titleDiv = document.querySelector('.right .title');
    titleDiv.innerHTML = '';
    let messageShowDiv = document.querySelector('.right .message-show');
    messageShowDiv.innerHTML = '';

    // 2. 重新设置会话的标题. 从选中的会话列表中获取标题
    let selectedH3 = document.querySelector('#session-list .selected>h3');
    if (selectedH3) {
        titleDiv.innerHTML = selectedH3.innerHTML;
    } else {
        // 如果找不到选中的会话，尝试从当前点击的 li 中获取
        let selectedLi = document.querySelector('#session-list .selected');
        if (selectedLi) {
            let h3 = selectedLi.querySelector('h3');
            if (h3) {
                titleDiv.innerHTML = h3.innerHTML;
            }
        }
    }
    // 3. 发送 ajax 请求给服务器, 获取到该会话的历史消息. 
    $.ajax({
        type: 'get',
        url: 'message?sessionId=' + sessionId,
        success: function(body) {
            console.log('历史消息响应:', body);
            let result = parseResult(body);
            console.log('解析后的结果:', result);
            if (result.status === 'ok' && Array.isArray(result.data)) {
                console.log('历史消息数量:', result.data.length);
                if (result.data.length === 0) {
                    console.log('该会话没有历史消息');
                }
                for (let message of result.data) {
                    console.log('添加消息:', message);
                    addMessage(messageShowDiv, message);
                }
                scrollBottom(messageShowDiv);
            } else if (result.status === 'nologin') {
                alert(result.message);
                location.assign('login.html');
            } else {
                alert(result.message || '获取历史消息失败!');
            }
        },
        error: function() {
            console.log('获取历史消息失败!');
        }
    });
}

function addMessage(messageShowDiv, message) {
    // 使用这个 div 表示一条消息
    let messageDiv = document.createElement('div');
    // 此处需要针对当前消息是不是用户自己发的, 决定是靠左还是靠右. 
    let selfUsername = document.querySelector('.left .user').innerHTML;
    if (selfUsername == message.fromName) {
        // 消息是自己发的. 靠右
        messageDiv.className = 'message message-right';
    } else {
        // 消息是别人发的. 靠左
        messageDiv.className = 'message message-left';
    }
    messageDiv.innerHTML = '<div class="box">' 
        + '<h4>' + message.fromName + '</h4>'
        + '<p>' + message.content + '</p>'
        + '</div>';
    messageShowDiv.appendChild(messageDiv);
}

// 把 messageShowDiv 里的内容滚动到底部. 
function scrollBottom(elem) {
    // 1. 获取到可视区域的高度
    let clientHeight = elem.offsetHeight;
    // 2. 获取到内容的总高度
    let scrollHeight = elem.scrollHeight;
    // 3. 进行滚动操作, 第一个参数是水平方向滚动的尺寸. 第二个参数是垂直方向滚动的尺寸
    elem.scrollTo(0, scrollHeight - clientHeight);
}

// 点击好友列表项, 触发的函数
function clickFriend(friend) {
    // 1. 先判定一下当前这个好友是否有对应的会话. 
    //    使用一个单独的函数来实现. 这个函数参数是用户的名字. 返回值是一个 li 标签. 找到了就是返回了对应会话列表里的 li; 如果没找到, 返回 null
    let sessionLi = findSessionByName(friend.friendName);
    let sessionListUL = document.querySelector('#session-list');
    if (sessionLi) {
        // 2. 如果存在匹配的结果, 就把这个会话设置成选中状态, 获取历史消息, 并且置顶. 
        //    insertBefore 把这个找到的 li 标签放到最前面去. 
        sessionListUL.insertBefore(sessionLi, sessionListUL.children[0]);
        //    此处设置会话选中状态, 获取历史消息, 这俩功能其实在上面的 clickSession 中已经有了. 
        //    此处直接调用 clickSession 即可
        //    clickSession(sessionLi);
        //    或者还可以模拟一下点击操作. 
        sessionLi.click();
    } else {
        // 3. 如果不存在匹配的结果, 就创建个新会话(创建 li 标签 + 通知服务器)
        sessionLi = document.createElement('li');
        //    构造 li 标签内容. 由于新会话没有 "最后一条消息", p 标签内容就设为空即可
        sessionLi.innerHTML = '<h3>' + friend.friendName + '</h3>' + '<p></p>';
        //    把标签进行置顶
        sessionListUL.insertBefore(sessionLi, sessionListUL.children[0]);
        sessionLi.onclick = function() {
            clickSession(sessionLi);
        }
        sessionLi.click();
        //     发送消息给服务器, 告诉服务器当前新创建的会话是啥样的. 
        // 确保 friendId 是数字类型
        let friendId = parseInt(friend.friendId);
        if (!isNaN(friendId)) {
            createSession(friendId, sessionLi);
        } else {
            console.error('无效的 friendId:', friend.friendId);
        }
    }
    // 4. 还需要把标签页给切换到 会话列表. 
    //    实现方式很容易, 只要找到会话列表标签页按钮, 模拟一个点击操作即可. 
    let tabSession = document.querySelector('.tab .tab-session');
    tabSession.click();
}

function findSessionByName(username) {
    // 先获取到会话列表中所有的 li 标签
    // 然后依次遍历, 看看这些 li 标签谁的名字和要查找的名字一致. 
    let sessionLis = document.querySelectorAll('#session-list>li');
    for (let sessionLi of sessionLis) {
        // 获取到该 li 标签里的 h3 标签, 进一步得到名字
        let h3 = sessionLi.querySelector('h3');
        if (h3.innerHTML == username) {
            return sessionLi;
        }
    }
    return null;
}

// friendId 是构造 HTTP 请求时必备的信息
function createSession(friendId, sessionLi) {
    $.ajax({
        type: 'post',
        url: 'session?toUserId=' + friendId,
        success: function(body) {
            let result = parseResult(body);
            if (result.status === 'ok' && result.data) {
                let sessionId = result.data.sessionId || result.data;
                // 确保 sessionId 是数字类型
                sessionId = parseInt(sessionId);
                if (!isNaN(sessionId)) {
                    sessionLi.setAttribute('message-session-id', sessionId);
                    console.log("会话创建成功! sessionId = " + sessionId);
                } else {
                    console.error('无效的 sessionId:', result.data);
                }
            } else {
                console.log(result.message || '会话创建失败!');
            }
        }, 
        error: function() {
            console.log('会话创建失败!');
        }
    });
}