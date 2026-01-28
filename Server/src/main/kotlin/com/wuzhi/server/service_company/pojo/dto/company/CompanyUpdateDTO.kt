package com.wuzhi.server.service_company.pojo.dto.company

import jakarta.validation.constraints.NotBlank

class CompanyUpdateDTO(

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,
    val shorNme: String? = null,
    val name: String? = null,
    val description: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val industry: String? = null,
    val status: String? = null,
) {
}