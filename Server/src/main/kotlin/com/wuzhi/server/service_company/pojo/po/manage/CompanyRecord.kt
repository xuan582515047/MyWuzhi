package com.wuzhi.server.service_company.pojo.po.manage

import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("record_company")
class CompanyRecord(
    val id: String? = null,
    val companyId: String,
    val type: String,
    val operateType: String,
    val targetId: String,
    val message: String,
    val operatorUserId: String,
    val operateTime: LocalDateTime
) {
    companion object{
        const val TYPE_ROLE = "role"
        const val TYPE_DEPARTMENT = "department"
        const val TYPE_EMPLOYEE = "employee"
        const val TYPE_MISSION = "mission"
        const val TYPE_COMPANY = "company"

        const val OPERATE_TYPE_ADD = "add"
        const val OPERATE_TYPE_DELETE = "delete"
        const val OPERATE_TYPE_UPDATE = "update"
    }
}