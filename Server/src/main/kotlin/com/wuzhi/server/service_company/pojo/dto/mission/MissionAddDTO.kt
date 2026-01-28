package com.wuzhi.server.service_company.pojo.dto.mission

import jakarta.validation.constraints.NotBlank

class MissionAddDTO(

    @NotBlank(message = "任务名称不能为空")
    val name: String,

    val description: String? = null,

    @NotBlank(message = "开始时间不能为空")
    val startTime: String,

    @NotBlank(message = "结束时间不能为空")
    val endTime: String,

    @NotBlank(message = "任务状态不能为空")
    val status: String,

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    @NotBlank(message = "部门ID不能为空")
    val departmentId: String,

    val employeeId: String? = null,

){
}