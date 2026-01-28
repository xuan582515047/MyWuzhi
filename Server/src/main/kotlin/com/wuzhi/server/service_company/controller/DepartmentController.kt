package com.wuzhi.server.service_company.controller

import com.wuzhi.server.common.context.UserContext
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.pojo.dto.department.DepAddDTO
import com.wuzhi.server.service_company.pojo.dto.department.DepChangeDTO
import com.wuzhi.server.service_company.pojo.dto.department.DepDeleteDTO
import com.wuzhi.server.service_company.pojo.dto.department.DepUpdateDTO
import com.wuzhi.server.service_company.pojo.dto.department.DepStatisticsDTO
import com.wuzhi.server.service_company.pojo.po.manage.CompanyRecord
import com.wuzhi.server.service_company.service.CompanyRecordService
import com.wuzhi.server.service_company.service.DepartmentService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/department")
class DepartmentController(
    private val departmentService: DepartmentService,
    private val companyRecordService: CompanyRecordService
){
    /**
     * 添加部门
     */
    @PostMapping("/add")
    fun addDepartment(@RequestBody @Valid dto: DepAddDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_DEPARTMENT,
            operateType = CompanyRecord.OPERATE_TYPE_ADD,
            targetId = dto.companyId,
            message = "添加部门：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return departmentService.addDepartment(dto)
    }

    /**
     * 删除部门
     */
    @PostMapping("/delete")
    fun deleteDepartment(@RequestBody @Valid dto: DepDeleteDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_DEPARTMENT,
            operateType = CompanyRecord.OPERATE_TYPE_DELETE,
            targetId = dto.departmentId,
            message = "删除部门：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return departmentService.deleteDepartment(dto.departmentId)
    }

    /**
     * 更新部门
     */
    @PutMapping("/update")
    fun updateDepartment(@RequestBody @Valid dto: DepUpdateDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_DEPARTMENT,
            operateType = CompanyRecord.OPERATE_TYPE_UPDATE,
            targetId = dto.companyId,
            message = "更新部门信息：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return departmentService.updateDepartment(dto)
    }

    /**
     * 获取部门统计信息
     */
    @PostMapping("/statistics")
    fun getDepartmentStatistics(@RequestBody @Valid dto: DepStatisticsDTO): ResponseResult {
        return departmentService.getDepartmentStatistics(dto.departmentId)
    }

    /**
     * 更换部门的父级
     */
    @PostMapping("/change/parent")
    fun changeDepartmentParent(@RequestBody @Valid dto: DepChangeDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_DEPARTMENT,
            operateType = CompanyRecord.OPERATE_TYPE_UPDATE,
            targetId = dto.nodeId,
            message = "更换部门的父级：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return departmentService.changeDepParent(dto)
    }
}