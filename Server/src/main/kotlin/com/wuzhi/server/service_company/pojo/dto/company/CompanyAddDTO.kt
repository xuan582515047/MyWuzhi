package com.wuzhi.server.service_company.pojo.dto.company

import jakarta.validation.constraints.NotBlank

class CompanyAddDTO(

    val shortName: String? = null,

    val description: String? = null,

    @NotBlank(message = "公司名称不能为空")
    val name: String,

    @NotBlank(message = "联系电话不能为空")
    val phone: String,

    @NotBlank(message = "公司邮箱不能为空")
    val email: String,

    @NotBlank(message = "公司行业不能为空")
    val industry: String,
) {
}