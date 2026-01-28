package com.wuzhi.server.service_company.pojo.dto.schedule

import jakarta.validation.constraints.NotBlank

class ScheduleListDTO(

    @NotBlank(message = "companyId不能为空")
    val companyId: String,

    @NotBlank(message = "startDate不能为空")
    val startDate: String,

    @NotBlank(message = "endDate不能为空")
    val endDate: String
) {
}