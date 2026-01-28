package com.wuzhi.server.service_company.pojo.dto.mission

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

class MissionDeleteDTO(

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    @NotEmpty(message = "任务ID不能为空")
    val missionIds: List<String>
) {
}