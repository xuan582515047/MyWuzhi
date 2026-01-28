package com.wuzhi.server.service_company.pojo.dto.employee

import jakarta.validation.constraints.NotBlank

class EmpQueryDTO(
    @NotBlank(message = "公司id不能为空")
    val companyId: String,
    val name: String? = null,
    val status: String? = null,
    val departmentId: String? = null,
    val pageNum: Long? = null,
    val pageSize: Long? = null
){
}