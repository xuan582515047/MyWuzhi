package com.wuzhi.server.service_company.pojo.dto.company

import jakarta.validation.constraints.NotBlank

class CompanyStatisticsDTO(

    @NotBlank(message = "公司ID不能为空")
    val companyId: String
) {
}