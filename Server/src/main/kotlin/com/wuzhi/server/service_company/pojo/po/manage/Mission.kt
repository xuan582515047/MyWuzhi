package com.wuzhi.server.service_company.pojo.po.manage

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("data_mission")
class Mission(

    @TableId("id")
    val id: String? = null,

    @TableField("name")
    val name: String,

    @TableField("description")
    val description: String? = null,

    @TableField("start_time")
    val startTime: LocalDateTime,

    @TableField("end_time")
    val endTime: LocalDateTime,

    @TableField("status")
    val status: String,

    @TableField("company_id")
    val companyId: String,

    @TableField("department_id")
    val departmentId: String,

    @TableField("employee_id")
    val employeeId: String? = null,

    @TableField("operator_emp_id")
    val operatorEmpId: String,

    @TableField("publisher_emp_id")
    val publisherEmpId: String,

    @TableField("marked")
    val marked: Boolean,

    @TableField("create_time")
    val createTime: LocalDateTime,

    @TableField("update_time")
    val updateTime: LocalDateTime,
) {
    companion object{
        const val STATUS_COMPLETED = "completed"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_NOT_START = "not_started"
        const val STATUS_PAUSED = "paused"
        const val STATUS_CANCELED = "canceled"
        const val STATUS_OVERDUE = "overdue"
    }
}