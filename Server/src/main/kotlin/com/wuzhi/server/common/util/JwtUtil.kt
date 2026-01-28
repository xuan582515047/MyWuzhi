package com.wuzhi.server.common.util

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.nio.charset.StandardCharsets
import java.util.Date

object JwtUtil {
    fun createJwt(secretKey: String, ttlMillis: Long, claims: Map<String, Any>): String{
        // 设置签名算法
        val signatureAlgorithm = SignatureAlgorithm.HS256;
        // 设置过期时间
        val expMillis = System.currentTimeMillis() + ttlMillis;
        val exp = Date(expMillis);
        // 生成Token，并返回
        val builder = Jwts.builder()
            // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
            .setClaims(claims)
            // 设置签名使用的签名算法和签名使用的秘钥
            .signWith(signatureAlgorithm, secretKey.toByteArray(StandardCharsets.UTF_8))
            // 设置过期时间
            .setExpiration(exp);
        return builder.compact();
    }

    fun parseJwt(secretKey: String, jwt: String): Map<String, Any>?{
        val claims = Jwts.parser()
            .setSigningKey(secretKey.toByteArray(StandardCharsets.UTF_8))
            .parseClaimsJws(jwt)
            .body
        return claims
    }
}