package com.wuzhi.server.service_company.pojo.vo.company

class CompanyNodeVO(
    val id: String,
    val name: String,
    val type: String,
    val children: List<CompanyNodeVO>
) {
}