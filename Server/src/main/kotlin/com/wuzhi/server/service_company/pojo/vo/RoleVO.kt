package com.wuzhi.server.service_company.pojo.vo

import com.wuzhi.server.service_company.pojo.po.auth.Role

class RoleVO(
    val id: String,
    val name: String,
    val description: String? = null,
    val createTime: String,
    val updateTime: String
) {
    companion object{
        fun fromRole(role: Role): RoleVO {
            return RoleVO(
                id = role.id!!,
                name = role.name,
                description = role.description,
                createTime = role.createTime.toString(),
                updateTime = role.updateTime.toString()
            )
        }
    }
}