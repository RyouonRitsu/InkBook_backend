package com.ryouonritsu.inkbook_backend.config

import com.google.gson.Gson
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import java.io.IOException


/**
 *
 * @author WuKunchao
 */
@Component
class DefaultHandler : WebSocketHandler {

    var users = mutableListOf<WebSocketSession>()

    /**
     * 建立连接
     * @param session
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun afterConnectionEstablished(session: WebSocketSession) {
        System.out.println("成功建立连接");
        session.sendMessage(TextMessage("成功建立socket连接"));
        users.add(session)
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
        System.out.println("0000" + message.payload)
        val message1 = TextMessage("server:$message")
        try {
            session.sendMessage(message1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val gson = Gson()
        println(message)
        val res = gson.fromJson(message.payload.toString(), Data::class.java)
        println(res)
        users.forEach {
            it.sendMessage(TextMessage(res.msg))
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
            session.close();
        }
        System.out.println("连接出错");
    }

    /**
     * 关闭连接
     * @param session
     * @param closeStatus
     * @throws Exception
     */
    @Throws(Exception::class)
    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        System.out.println("连接已关闭：" + closeStatus);
        users.remove(session)
    }

    override fun supportsPartialMessages(): Boolean {
        return false
    }
}

class Data {
    lateinit var msg: String
}