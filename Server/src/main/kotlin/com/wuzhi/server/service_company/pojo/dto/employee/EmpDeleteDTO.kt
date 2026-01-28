package com.wuzhi.server.service_company.pojo.dto.employee

import jakarta.validation.constraints.NotBlank

class EmpDeleteDTO(

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    @NotBlank(message = "员工ID不能为空")
    val employeeId: String
) {
}