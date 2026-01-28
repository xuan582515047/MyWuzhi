package com.wuzhi.server.service_company.pojo.po.auth

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("data_role")
data class Role(
    @TableId("id")
    var id: String? = null,

    @TableField("company_id")
    val companyId: String,

    @TableField("name")
    var name: String,
    
    @TableField("description")
    var description: String? = null,

    @TableField("create_time")
    val createTime: LocalDateTime,

    @TableField("update_time")
    var updateTime: LocalDateTime,

    @TableField("operator_emp_id")
    var operatorEmpId: String
){
}