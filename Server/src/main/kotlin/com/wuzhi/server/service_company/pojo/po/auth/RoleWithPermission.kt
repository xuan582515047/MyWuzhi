package com.wuzhi.server.service_company.pojo.po.auth

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler

@TableName("link_role_with_permission")
data class RoleWithPermission(

    @TableField("company_id")
    val companyId: String,

    @TableField("role_id")
    val roleId: String,
    
    @TableField(value = "permission_id", typeHandler = JacksonTypeHandler::class)
    val permissionId: String
)