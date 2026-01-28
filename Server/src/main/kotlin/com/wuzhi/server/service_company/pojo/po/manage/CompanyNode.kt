package com.wuzhi.server.service_company.pojo.po.manage

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName

@TableName("node_company")
class CompanyNode(

    @TableId("id")
    val id: String? = null,

    @TableField("parent_id")
    val parentId: String? = null,

    @TableField("company_id")
    val companyId: String,

    @TableField("level")
    val level: Int,

    @TableField("target_id")
    val targetId: String,

    @TableField("name")
    val name: String,

    @TableField("type")
    val type: String,

){
    companion object {
        const val TYPE_DEPARTMENT = "department"
        const val TYPE_EMPLOYEE = "employee"
    }
}