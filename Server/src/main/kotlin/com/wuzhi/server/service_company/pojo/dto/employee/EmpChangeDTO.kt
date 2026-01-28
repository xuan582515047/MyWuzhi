package com.wuzhi.server.service_company.pojo.dto.employee

import jakarta.validation.constraints.NotBlank

class EmpChangeDTO(

    @NotBlank(message = "公司id不能为空")
    val companyId: String,

    @NotBlank(message = "部门id不能为空")
    val nodeId: String,

    @NotBlank(message = "父部门id不能为空")
    val parentId: String
){
}