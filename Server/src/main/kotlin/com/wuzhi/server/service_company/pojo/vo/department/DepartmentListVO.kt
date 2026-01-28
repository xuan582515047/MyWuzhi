package com.wuzhi.server.service_company.pojo.vo.department

import com.wuzhi.server.service_company.pojo.po.manage.Department

class DepartmentListVO(
    val id: String,
    val name: String,
) {
    companion object{
        fun fromDepartment(department: Department): DepartmentListVO {
            return DepartmentListVO(
                id = department.id!!,
                name = department.name
            )
        }
    }
}