package com.wuzhi.server.service_tool.pojo.vo


class DatabaseDetailVO(
    var id: String? = null,
    var name: String,
    var description: String? = null,
    var type: String,
    var url: String? = null,
    val text: String? = null,
) {
}