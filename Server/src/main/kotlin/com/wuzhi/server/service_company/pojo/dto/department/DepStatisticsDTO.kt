package com.wuzhi.server.service_company.pojo.dto.department

import jakarta.validation.constraints.NotBlank

class DepStatisticsDTO(

    @NotBlank(message = "部门ID不能为空")
    val departmentId: String
) {
}