package com.wuzhi.server.service_company.pojo.dto.schedule

import jakarta.validation.constraints.NotBlank

class ScheduleUpdateDTO(

    @NotBlank(message = "id不能为空")
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val time: String? = null
) {
}