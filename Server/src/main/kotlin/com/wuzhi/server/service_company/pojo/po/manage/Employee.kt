package com.wuzhi.server.service_company.pojo.po.manage

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("data_employee")
class Employee(

    // 基础信息

    @TableId("id")
    val id: String? = null,

    @TableField("user_id")
    val userId: String,

    @TableField("name")
    val name: String,

    @TableField("company_id")
    val companyId: String,

    @TableField("department_id")
    val departmentId: String? = null,

    @TableField("status")
    val status: String,

    // 更新信息

    @TableField("create_time")
    val createTime: LocalDateTime,

    @TableField("update_time")
    val updateTime: LocalDateTime,

    @TableField("operator_emp_id")
    val operatorEmpId: String,
) {
    companion object{
        const val STATUS_ACTIVE = "active"
        const val STATUS_LEAVE = "leave"
        const val STATUS_BANNED = "banned"
    }
}