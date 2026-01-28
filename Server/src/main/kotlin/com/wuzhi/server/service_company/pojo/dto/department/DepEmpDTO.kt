package com.wuzhi.server.service_company.pojo.dto.department

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

class DepEmpDTO(

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    @NotBlank(message = "操作员的员工ID不能为空")
    val operatorEmpId: String,

    @NotBlank(message = "部门ID不能为空")
    val departmentId: String,

    @NotEmpty(message = "员工ID不能为空")
    val employeeIds: List<String>,
) {
}