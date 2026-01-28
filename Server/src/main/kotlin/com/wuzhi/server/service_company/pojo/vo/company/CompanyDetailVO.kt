package com.wuzhi.server.service_company.pojo.vo.company

import com.wuzhi.server.service_company.pojo.po.manage.Company

class CompanyDetailVO(
    val shortName: String? = null,
    val name: String,
    val description: String? = null,
    val creditCode: String? = null,
    val legalRepresentative: String? = null,
    val phone: String,
    val email: String,
    val industry: String,
    val status: String,
    val approveStatus: String,
    val createTime: String,
) {
    companion object{
        fun fromCompany(company: Company): CompanyDetailVO {
            return CompanyDetailVO(
                shortName = company.shortName,
                name = company.name,
                description = company.description,
                creditCode = company.creditCode,
                legalRepresentative = company.legalRepresentative,
                phone = company.phone,
                email = company.email,
                industry = company.industry,
                status = company.status,
                approveStatus = company.approveStatus,
                createTime = company.createTime.toString(),
            )
        }
    }
}