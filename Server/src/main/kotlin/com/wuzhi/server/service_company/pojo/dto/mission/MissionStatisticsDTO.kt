package com.wuzhi.server.service_company.pojo.dto.mission

import jakarta.validation.constraints.NotBlank

class MissionStatisticsDTO(

    @NotBlank(message = "公司ID不能为空")
    val companyId: String
) {
}