package com.wuzhi.server.common.util

import com.wuzhi.server.common.property.JwtProperties
import org.springframework.stereotype.Component

@Component
class TokenUtil (
    private val jwtProperties: JwtProperties
){
    fun createAccessToken(secretKey: String, claims: Map<String, Any>): String{
        return JwtUtil.createJwt(secretKey, jwtProperties.accessTtlMillis, claims)
    }

    fun createAccessToken(claims: Map<String, Any>): String{
        return JwtUtil.createJwt(jwtProperties.accessKey, jwtProperties.accessTtlMillis, claims)
    }

    fun createRefreshToken(secretKey: String, claims: Map<String, Any>): String{
        return JwtUtil.createJwt(secretKey, jwtProperties.refreshTtlMillis, claims)
    }

    fun createRefreshToken(claims: Map<String, Any>): String{
        return JwtUtil.createJwt(jwtProperties.refreshKey, jwtProperties.refreshTtlMillis, claims)
    }
}