package com.wuzhi.server.service_company.pojo.dto.employee

import jakarta.validation.constraints.NotBlank

class EmpUpdateDTO(

    @NotBlank(message = "公司id不能为空")
    val companyId: String,

    @NotBlank(message = "员工id不能为空")
    val employeeId: String,

    val name: String? = null,

    val status: String? = null,
) {
}