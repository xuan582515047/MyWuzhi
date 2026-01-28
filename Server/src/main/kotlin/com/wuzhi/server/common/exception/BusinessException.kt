package com.wuzhi.server.common.exception

/**
 * 业务异常类
 * 用于抛出可预期的业务逻辑错误
 */
class BusinessException(
    val code: Int = 500,
    message: String
) : RuntimeException(message)
