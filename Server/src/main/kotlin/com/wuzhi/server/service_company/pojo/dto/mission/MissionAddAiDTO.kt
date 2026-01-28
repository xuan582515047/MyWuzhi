package com.wuzhi.server.service_company.pojo.dto.mission

import jakarta.validation.constraints.NotBlank

class MissionAddAiDTO(

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    @NotBlank(message = "部门ID不能为空")
    val departmentId: String,

    @NotBlank(message = "员工ID不能为空")
    val employeeId: String,
) {
}