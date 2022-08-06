package com.ryouonritsu.inkbook_backend.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class MyWebsocketConfig : WebSocketConfigurer {
    @Autowired
    lateinit var myWebsocketHandler: DefaultHandler

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(myWebsocketHandler, "/ws").setAllowedOrigins("*")
    }
}