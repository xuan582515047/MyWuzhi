package com.wuzhi.server.service_tool.pojo.vo

import com.wuzhi.server.service_tool.pojo.po.Database

class DatabaseTreeVO(
    val id: String,
    val name: String,
    val status: Boolean,
    val type: String,
    val children: List<DatabaseTreeVO>? = null
) {
}