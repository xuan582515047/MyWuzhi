package com.wuzhi.server.common.util

import java.util.UUID

object UuidUtil {
    fun generateUuid(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }
}