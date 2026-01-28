package com.wuzhi.server.service_company.pojo.dto.role

import jakarta.validation.constraints.NotBlank

class RoleUpdateDTO(
    @NotBlank(message = "角色ID不能为空")
    val roleId: String,

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    val name: String? = null,

    val description: String? = null
) {
}