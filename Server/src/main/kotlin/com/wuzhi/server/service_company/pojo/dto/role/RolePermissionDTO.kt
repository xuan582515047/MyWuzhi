package com.wuzhi.server.service_company.pojo.dto.role

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

class RolePermissionDTO(

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    @NotBlank(message = "角色ID不能为空")
    val roleId: String,

    @NotEmpty(message = "权限ID不能为空")
    val authIds: List<String>
) {
}