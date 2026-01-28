package com.wuzhi.server.common.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt.config")
data class JwtProperties (
    // 访问token密钥
    val accessKey: String = "w5vBvSBUZXh0IGZvciB0aGUgc2VjcmV0IGtleSBuZWVkIHRvIGJlIGF0IGxlYXN0IDMyIGNoYXJhY3RlcnM=",
    // 刷新token密钥
    val refreshKey: String = "w5vBvSBUZXh0IGZvciB0aGUgc2VjcmV0IGtleSBuZWVkIHRvIGJlIGF0IGxlYXN0IDMyIGNoYXJhY3RlcnM=",
    // 访问令牌有效期，默认1天
    val accessTtlMillis: Long = 1000 * 60 * 60 * 24,
    // 刷新令牌有效期，默认7天
    val refreshTtlMillis: Long = 1000 * 60 * 60 * 24 * 7,
    // 令牌请求头名称，默认为 Authorization
    val tokenHeader: String = "Authorization",
    // 用户id的key
    val userIdKey: String = "userId"
)
