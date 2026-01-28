package com.wuzhi.server.service_company.pojo.dto.role

import jakarta.validation.constraints.NotBlank

class RoleAddDTO(

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    @NotBlank(message = "角色名称不能为空")
    val name: String,

    val description: String? = null
) {
}