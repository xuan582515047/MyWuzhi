package com.wuzhi.server.service_company.pojo.dto.department

import jakarta.validation.constraints.NotBlank

class DepUpdateDTO(

    @NotBlank(message = "公司id不能为空")
    val companyId: String,

    @NotBlank(message = "部门id不能为空")
    val depId: String,

    val name: String? = null,
    val description: String? = null,
) {
}