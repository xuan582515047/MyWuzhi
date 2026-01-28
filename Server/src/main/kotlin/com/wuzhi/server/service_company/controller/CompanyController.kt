package com.wuzhi.server.service_company.controller

import com.wuzhi.server.common.constant.BaseUrlConstant
import com.wuzhi.server.common.context.UserContext
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.pojo.dto.company.CompRecordQueryDTO
import com.wuzhi.server.service_company.pojo.dto.company.CompanyAddDTO
import com.wuzhi.server.service_company.pojo.dto.company.CompanyAuthDTO
import com.wuzhi.server.service_company.pojo.dto.company.CompanyDeleteDTO
import com.wuzhi.server.service_company.pojo.dto.company.CompanyUpdateDTO
import com.wuzhi.server.service_company.pojo.dto.company.CompanyStatisticsDTO
import com.wuzhi.server.service_company.pojo.po.manage.CompanyRecord
import com.wuzhi.server.service_company.service.CompanyRecordService
import com.wuzhi.server.service_company.service.CompanyService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/company")
class CompanyController(
    private val companyService: CompanyService,
    private val companyRecordService: CompanyRecordService
) {

    /**
     * 获取公司部门和员工树
     */
    @GetMapping("/tree/{companyId}")
    fun getCompanyTree(@PathVariable("companyId") companyId: String): ResponseResult {
        return companyService.getCompanyTree(companyId)
    }

    /**
     * 查询公司操作记录
     */
    @PostMapping("/record")
    fun getCompanyRecord(@RequestBody @Valid dto: CompRecordQueryDTO): ResponseResult {
        return companyRecordService.getCompanyRecord(dto)
    }

    /**
     * 获取公司权限列表
     */
    @GetMapping("/permission/{companyId}")
    fun getCompanyPermission(@PathVariable("companyId") companyId: String): ResponseResult {
        return companyService.getCompanyPermission(companyId)
    }

    /**
     * 获取公司详情信息
     */
    @GetMapping("/detail/{companyId}")
    fun getCompanyDetail(@PathVariable("companyId") companyId: String): ResponseResult {
        return companyService.getCompanyDetail(companyId)
    }

    /**
     * 公司认证
     */
    @PostMapping("/auth")
    fun authCompany(@Valid @RequestBody dto: CompanyAuthDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_COMPANY,
            operateType = CompanyRecord.OPERATE_TYPE_UPDATE,
            targetId = dto.companyId,
            message = "公司认证",
            operatorUserId = UserContext.getUserId()
        )
        return companyService.authCompany(dto)
    }

    /**
     * 添加公司
     */
    @PostMapping("/add")
    fun addCompany(@Valid @RequestBody dto: CompanyAddDTO): ResponseResult {
        return companyService.addCompany(dto)
    }

    /**
     * 删除公司
     */
    @PostMapping("/delete")
    fun deleteCompany(@RequestBody @Valid dto: CompanyDeleteDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_COMPANY,
            operateType = CompanyRecord.OPERATE_TYPE_DELETE,
            targetId = dto.companyId,
            message = "删除公司：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return companyService.deleteCompany(dto.companyId)
    }

    /**
     * 更新公司
     */
    @PutMapping("/update")
    fun updateCompany(@Valid @RequestBody dto: CompanyUpdateDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_COMPANY,
            operateType = CompanyRecord.OPERATE_TYPE_UPDATE,
            targetId = dto.companyId,
            message = "更新公司信息：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return companyService.updateCompany(dto)
    }


    /**
     * 获取公司列表
     */
    @GetMapping("/list")
    fun getCompanyList(): ResponseResult {
        return companyService.getCompanyList()
    }

    /**
     * 获取公司统计信息
     */
    @PostMapping("/statistics")
    fun getCompanyStatistics(@RequestBody @Valid dto: CompanyStatisticsDTO): ResponseResult {
        return companyService.getCompanyStatistics(dto.companyId)
    }

}