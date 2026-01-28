package com.wuzhi.server.service_company.pojo.vo.department

class DepartmentTreeVO(
    val id: String,
    val name: String,
    val type: String,
    val description: String? = null,
    val children: List<DepartmentTreeVO>? = null
) {
}