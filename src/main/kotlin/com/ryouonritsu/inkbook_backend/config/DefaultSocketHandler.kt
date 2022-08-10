package com.ryouonritsu.inkbook_backend.config

import com.google.gson.Gson
import org.springframework.stereotype.Component
import org.springframework.web.socket.*


/**
 * {
 * token:
 * user_id:
 * type:
 * id:
 *
 * }
 * @author WuKunchao
 */
@Component
class DefaultHandler : WebSocketHandler {

    var users = HashMap<WebSocketSession, Int>()

    /**
     * 建立连接
     * @param session
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun afterConnectionEstablished(session: WebSocketSession) {
        println("成功建立连接")
        // session.sendMessage(TextMessage("成功建立socket连接"));
        // users.add(session)
        // session.sendMessage(TextMessage("发送一波消息"));
        // System.out.println("关闭。。。。。。。。。。。");
        // session.close(); // 发送完就关闭
        // System.out.println("关闭成功");
    }

    /**
     * 接收消息
     * @param session
     * @param message
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val msg = message.payload.toString()
        if ("token" in msg) {
            val gson = Gson()
            val res = gson.fromJson(msg, Info::class.java)
            val type = res.type
            var id = res.id.toInt()
            if (type == "axure") {
                id += 0x3f3f3f3f
            }
            users.put(session, id)
            println("用户喜加一：" + res.user_id)
            println("目前在线用户数：" + users.size)
            return
        }
        println(msg)

        val gson = Gson()
        val res = gson.fromJson(msg, Data::class.java)
        if (res.type == "doc") {
            println("开始同步文档！")
            users.forEach {
                if (res.id.toInt() == it.value && session != it.key) {
                    it.key.sendMessage(TextMessage(gson.toJson(res)))
                }
            }
        } else if (res.type == "axure") {
            println("开始同步原型！")
            val axureId = res.id.toInt() + 0x3f3f3f3f
            users.forEach {
                if (axureId == it.value && session != it.key) {
                    it.key.sendMessage(TextMessage(gson.toJson(res)))
                }
            }
        }
    }

    /**
     * 发生错误
     * @param session
     * @param exception
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        if (session.isOpen()) {
            session.close()
        }
        println("连接出错")
    }

    /**
     * 关闭连接
     * @param session
     * @param closeStatus
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        println("连接已关闭：" + closeStatus)
        users.remove(session)
    }

    override fun supportsPartialMessages(): Boolean {
        return false
    }
}

class Info {
    lateinit var token: String
    lateinit var user_id: String
    lateinit var type: String // axure of doc
    lateinit var id: String // if axure: id += 0x3f3f3f3f
}

class Data {
    lateinit var type: String // axure of doc
    lateinit var id: String // if axure: id += 0x3f3f3f3f

    // 原型
    lateinit var config: String
    lateinit var item: String
    // operation: add 增加组件, drag 拖拽组件, update 更新组件属性
    // copy 复制组件集合, replace 去除组件, bg 背景, syn 同步所有协作者
    lateinit var op: String

    // lateinit var config_id: String
    // 文档
    lateinit var name: String
    lateinit var content: String
}