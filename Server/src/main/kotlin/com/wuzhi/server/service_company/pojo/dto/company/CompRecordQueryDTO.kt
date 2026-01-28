package com.wuzhi.server.service_company.pojo.dto.company

import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

class CompRecordQueryDTO(

    @NotBlank(message = "公司id不能为空")
    val companyId: String,
    val type: String? = null,
    val operateType: String? = null,
    val operatorEmpId: String? = null,
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null,

    @NotBlank(message = "页码不能为空")
    val pageNum: Long,

    @NotBlank(message = "页大小不能为空")
    val pageSize: Long
) {
}