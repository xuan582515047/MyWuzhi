package com.wuzhi.server.service_company.pojo.dto.department

import jakarta.validation.constraints.NotBlank

class DepDeleteDTO(

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    @NotBlank(message = "部门ID不能为空")
    val departmentId: String
) {
}