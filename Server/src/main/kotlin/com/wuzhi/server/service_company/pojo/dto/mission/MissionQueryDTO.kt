package com.wuzhi.server.service_company.pojo.dto.mission

import jakarta.validation.constraints.NotBlank

class MissionQueryDTO(

    @NotBlank(message = "公司ID不能为空")
    val companyId: String,

    val departmentId: String? = null,

    val employeeId: String? = null,

    val name: String? = null,

    val status: String? = null,

    val publisherEmpId: String? = null,

    val startTime: String? = null,

    val endTime: String? = null,

    val marked: Boolean? = null,

    val page: Long? = null,

    val pageSize: Long? = null
) {

}