package com.wuzhi.server.service_company.pojo.po.auth

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableName

@TableName("link_employee_with_role")
data class EmployeeWithRole(

    @TableField("employee_id")
    val employeeId: String? = null,

    @TableField("company_id")
    val companyId: String? = null,
    
    @TableField("role_id")
    val roleId: String? = null,

)