package com.wuzhi.server.service_company.pojo.dto.role

import jakarta.validation.constraints.NotBlank

class EmployeeRoleDTO(
    @NotBlank(message = "员工ID不能为空")
    val employeeId: String,

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    @NotBlank(message = "角色ID不能为空")
    val roleIds: List<String>
) {
}