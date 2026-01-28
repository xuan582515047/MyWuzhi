package com.wuzhi.server.service_company.pojo.dto.company

import jakarta.validation.constraints.NotBlank

class CompanyAuthDTO(

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    @NotBlank(message = "公司名称不能为空")
    val name: String,

    @NotBlank(message = "公司法人不能为空")
    val creditCode: String,

    @NotBlank(message = "公司法人不能为空")
    val legalRepresentative: String,

    @NotBlank(message = "公司行业不能为空")
    val industry: String,
) {
}