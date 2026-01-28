package com.wuzhi.server.service_company.controller

import com.wuzhi.server.common.constant.BaseUrlConstant
import com.wuzhi.server.common.context.UserContext
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.pojo.dto.employee.EmpAddDTO
import com.wuzhi.server.service_company.pojo.dto.employee.EmpChangeDTO
import com.wuzhi.server.service_company.pojo.dto.employee.EmpDeleteDTO
import com.wuzhi.server.service_company.pojo.dto.employee.EmpDetailDTO
import com.wuzhi.server.service_company.pojo.dto.employee.EmpQueryDTO
import com.wuzhi.server.service_company.pojo.dto.employee.EmpUpdateDTO
import com.wuzhi.server.service_company.pojo.dto.employee.EmpStatisticsDTO
import com.wuzhi.server.service_company.pojo.po.manage.CompanyRecord
import com.wuzhi.server.service_company.service.CompanyRecordService
import com.wuzhi.server.service_company.service.EmployeeService
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
@RequestMapping("/employee")
class EmployeeController(
    private val employeeService: EmployeeService,
    private val companyRecordService: CompanyRecordService
) {
    /**
     * 获取员工的权限
     */
    @GetMapping("/permission/{employeeId}")
    fun getEmpPermission(@PathVariable("employeeId") employeeId: String): ResponseResult {
        return employeeService.getEmpPermission(employeeId)
    }

    /**
     * 添加员工
     */
    @PostMapping("/add")
    fun addEmployee(@RequestBody @Valid dto: EmpAddDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_EMPLOYEE,
            operateType = CompanyRecord.OPERATE_TYPE_ADD,
            targetId = dto.companyId,
            message = "添加员工：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return employeeService.addEmployee(dto)
    }

    /**
     * 删除员工
     */
    @PostMapping("/delete")
    fun deleteEmployee(@RequestBody @Valid dto: EmpDeleteDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_EMPLOYEE,
            operateType = CompanyRecord.OPERATE_TYPE_DELETE,
            targetId = dto.companyId,
            message = "删除员工：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return employeeService.deleteEmployee(dto.employeeId)
    }

    /**
     * 更新员工
     */
    @PutMapping("/update")
    fun updateEmployee(@RequestBody @Valid dto: EmpUpdateDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_EMPLOYEE,
            operateType = CompanyRecord.OPERATE_TYPE_UPDATE,
            targetId = dto.companyId,
            message = "更新员工信息：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return employeeService.updateEmployee(dto)
    }

    /**
     * 获取员工列表
     */
    @PostMapping("/list")
    fun queryEmployeeList(@RequestBody @Valid dto: EmpQueryDTO): ResponseResult {
        return employeeService.queryEmployeeList(dto)
    }

    /**
     * 获取员工统计信息
     */
    @PostMapping("/statistics")
    fun getEmployeeStatistics(@RequestBody @Valid dto: EmpStatisticsDTO): ResponseResult {
        return employeeService.getEmployeeStatistics(dto.employeeId)
    }

    /**
     * 更换员工的父级部门
     */
    @PostMapping("/change/parent")
    fun changeEmployeeParent(@RequestBody @Valid dto: EmpChangeDTO): ResponseResult {
        companyRecordService.record(
            companyId = dto.companyId,
            type = CompanyRecord.TYPE_EMPLOYEE,
            operateType = CompanyRecord.OPERATE_TYPE_UPDATE,
            targetId = dto.companyId,
            message = "更换员工的父级部门：$dto",
            operatorUserId = UserContext.getUserId()
        )
        return employeeService.changeEmpParent(dto)
    }

    /**
     * 根据节点id查询员工详情
     */
    @PostMapping("/detail")
    fun getEmployeeDetail(@RequestBody @Valid dto: EmpDetailDTO): ResponseResult {
        return employeeService.getEmployeeDetail(dto)
    }
}