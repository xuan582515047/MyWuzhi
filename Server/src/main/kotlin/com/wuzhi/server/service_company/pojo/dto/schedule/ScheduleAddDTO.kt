package com.wuzhi.server.service_company.pojo.dto.schedule

import jakarta.validation.constraints.NotBlank

class ScheduleAddDTO(

    @NotBlank(message = "companyId不能为空")
    val companyId: String,

    @NotBlank(message = "title不能为空")
    val title: String,

    @NotBlank(message = "description不能为空")
    val description: String,

    @NotBlank(message = "time不能为空")
    val time: String
) {
}