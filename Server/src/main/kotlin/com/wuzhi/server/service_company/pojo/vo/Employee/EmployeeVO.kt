package com.wuzhi.server.service_company.pojo.vo.Employee

import com.wuzhi.server.service_company.pojo.po.manage.Employee

class EmployeeVO(

    val id: String? = null,
    val name: String,
    val departmentId: String?,
    val departmentName: String,
    val status: String,
    val createTime: String,
    val updateTime: String,
) {
    companion object{
        fun fromEmployee(employee: Employee, departmentId: String?, departmentName: String?): EmployeeVO{
            return EmployeeVO(
                id = employee.id,
                name = employee.name,
                departmentId = departmentId,
                departmentName = departmentName ?: "",
                status = employee.status,
                createTime = employee.createTime.toString(),
                updateTime = employee.updateTime.toString(),
            )
        }
    }
}