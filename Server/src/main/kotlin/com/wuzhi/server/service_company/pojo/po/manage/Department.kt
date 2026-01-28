package com.wuzhi.server.service_company.pojo.po.manage

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("data_department")
class Department(

    //基础信息

    @TableId("id")
    var id: String? = null,

    @TableField("name")
    var name: String,

    @TableField("description")
    val description: String? = null,

    @TableField("company_id")
    var companyId: String,

    // 更新信息

    @TableField("create_time")
    var createTime: LocalDateTime,

    @TableField("update_time")
    var updateTime: LocalDateTime,

    @TableField("operator_emp_id")
    var operatorEmpId: String,
) {
    override fun toString(): String {
        return "部门id：${id},部门名称：${name}，部门描述：${description}，创建时间：${createTime}，更新时间：${updateTime}"
    }
}