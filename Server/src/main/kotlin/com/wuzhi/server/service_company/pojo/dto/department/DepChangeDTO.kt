package com.wuzhi.server.service_company.pojo.dto.department

import jakarta.validation.constraints.NotBlank

class DepChangeDTO(

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    @NotBlank(message = "部门ID不能为空")
    val nodeId: String,

    @NotBlank(message = "父部门ID不能为空")
    val parentId: String
) {
}