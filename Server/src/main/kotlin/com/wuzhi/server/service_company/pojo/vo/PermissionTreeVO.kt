package com.wuzhi.server.service_company.pojo.vo

class PermissionTreeVO(
    val id: String,
    val name: String,
    val code: String,
    val description: String? = null,
    val children: List<PermissionTreeVO>? = null
) {
}