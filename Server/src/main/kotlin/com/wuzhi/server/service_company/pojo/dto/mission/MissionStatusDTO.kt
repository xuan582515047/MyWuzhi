package com.wuzhi.server.service_company.pojo.dto.mission

import jakarta.validation.constraints.NotBlank

class MissionStatusDTO(

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    @NotBlank(message = "任务ID不能为空")
    val missionId: String,

    @NotBlank(message = "任务状态不能为空")
    val status: String,

    @NotBlank(message = "操作人ID不能为空")
    val operatorEmpId: String
) {
}