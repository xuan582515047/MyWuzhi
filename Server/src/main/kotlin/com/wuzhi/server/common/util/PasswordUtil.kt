package com.wuzhi.server.common.util

import org.mindrot.jbcrypt.BCrypt

object PasswordUtil {
    /**
     * 加密（传入原始密码，返回加密后的字符串）
     * @param password 原始密码字符串
     * @return 加密后的BCrypt哈希字符串
     */
    fun encrypt(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
        //return password;
    }

    /**
     * 验证（传入原始密码和加密后的字符串，返回是否匹配）
     * @param password 用户输入的原始密码
     * @param encryptedPassword 之前存储的加密字符串
     * @return 是否匹配
     */
    fun verify(password: String, encryptedPassword: String): Boolean {
        return BCrypt.checkpw(password, encryptedPassword)
        //return password == encryptedPassword
    }
}