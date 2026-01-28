package com.wuzhi.server.service_company.pojo.dto.employee

import jakarta.validation.constraints.NotBlank

class EmpAddDTO(

    @NotBlank(message = "用户id不能为空")
    val userId: String,

    @NotBlank(message = "员工名称不能为空")
    val name: String,

    @NotBlank(message = "公司id不能为空")
    val companyId: String,

    @NotBlank(message = "员工状态不能为空")
    val status: String,

    @NotBlank(message = "部门id不能为空")
    val departmentNodeId: String,

    @NotBlank(message = "操作员id不能为空")
    val operatorEmpId: String
) {
}