package com.ryouonritsu.inkbook_backend.utils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisUtils {
    @Autowired
    lateinit var redisTemplate: RedisTemplate<String, String>
}