package com.wuzhi.server.service_company.pojo.dto.mission

import jakarta.validation.constraints.NotBlank

class MissionMarkDTO(

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    @NotBlank(message = "任务ID不能为空")
    val missionId: String,

    @NotBlank(message = "标记状态不能为空")
    val marked: Boolean
) {
}