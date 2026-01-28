package com.wuzhi.server.service_company.service

import com.baomidou.mybatisplus.extension.service.IService
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.pojo.dto.company.CompRecordQueryDTO
import com.wuzhi.server.service_company.pojo.dto.mission.MissionMarkDTO
import com.wuzhi.server.service_company.pojo.po.manage.CompanyRecord

interface CompanyRecordService: IService<CompanyRecord> {

    /**
     * 记录操作记录
     * @param companyId 公司id
     * @param type 操作类型
     * @param operateType 操作类型
     * @param targetId 操作目标id
     * @param message 操作信息
     * @param operatorUserId 操作人id
     */
    fun record(companyId: String,
               type: String,
               operateType: String,
               targetId: String,
               message: String,
               operatorUserId: String
    )

    /**
     * 查询公司操作记录
     */
    fun getCompanyRecord(dto: CompRecordQueryDTO): ResponseResult

}