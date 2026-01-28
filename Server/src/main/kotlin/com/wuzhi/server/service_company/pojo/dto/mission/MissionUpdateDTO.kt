package com.wuzhi.server.service_company.pojo.dto.mission

import jakarta.validation.constraints.NotBlank

class MissionUpdateDTO(

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    @NotBlank(message = "任务ID不能为空")
    val missionId: String,

    val marked: Boolean? = null,

    val name: String? = null,

    val description: String? = null,

    val startTime: String? = null,

    val endTime: String? = null,

    val status: String? = null,

    val departmentId: String? = null,

    val employeeId: String? = null,

    val publisherEmpId: String? = null,
) {
}