package com.wuzhi.server.service_company.pojo.dto.employee

import jakarta.validation.constraints.NotBlank

class EmpDetailDTO(

    @NotBlank(message = "节点id不能为空")
    val nodeId: String,

    @NotBlank(message = "公司id不能为空")
    val companyId: String
) {
}