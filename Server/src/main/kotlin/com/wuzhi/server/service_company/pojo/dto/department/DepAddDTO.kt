package com.wuzhi.server.service_company.pojo.dto.department

import jakarta.validation.constraints.NotBlank

class DepAddDTO(

    @NotBlank(message = "部门名称不能为空")
    val name: String,

    val description: String? = null,

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    @NotBlank(message = "父级id不能为空")
    val parentId: String,
) {
}