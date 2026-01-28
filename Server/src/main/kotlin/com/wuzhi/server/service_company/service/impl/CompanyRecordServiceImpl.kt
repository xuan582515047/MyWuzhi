package com.wuzhi.server.service_company.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.wuzhi.server.common.constant.ErrorMessages
import com.wuzhi.server.common.constant.OkMessages
import com.wuzhi.server.common.pojo.PageResult
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.mapper.CompanyRecordMapper
import com.wuzhi.server.service_company.pojo.dto.company.CompRecordQueryDTO
import com.wuzhi.server.service_company.pojo.po.manage.CompanyRecord
import com.wuzhi.server.service_company.pojo.vo.company.CompanyRecordVO
import com.wuzhi.server.service_company.pojo.vo.Employee.EmployeeVO
import com.wuzhi.server.service_company.service.CompanyRecordService
import com.wuzhi.server.service_company.service.DepartmentService
import com.wuzhi.server.service_company.service.EmployeeService
import com.wuzhi.server.service_company.service.MissionService
import com.wuzhi.server.service_company.service.RoleService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CompanyRecordServiceImpl(
    private val employeeService: EmployeeService,
    private val roleService: RoleService,
    private val departmentService: DepartmentService,
    private val missionService: MissionService
): CompanyRecordService, ServiceImpl<CompanyRecordMapper, CompanyRecord>(){
    override fun record(
        companyId: String,
        type: String,
        operateType: String,
        targetId: String,
        message: String,
        operatorUserId: String
    ) {
        val record = CompanyRecord(
            companyId = companyId,
            type = type,
            operateType = operateType,
            targetId = targetId,
            message = message,
            operatorUserId = operatorUserId,
            operateTime = LocalDateTime.now()
        )
        save(record)
    }

    override fun getCompanyRecord(dto: CompRecordQueryDTO): ResponseResult {
        // 查询操作人信息
        val emp = employeeService.getById(dto.operatorEmpId) ?:
            return ResponseResult.error(ErrorMessages.EMPLOYEE_NOT_EXIST)

        // 查询操作记录
        val wrapper = KtQueryWrapper(CompanyRecord::class.java)
            .eq(CompanyRecord::companyId, dto.companyId)
            .eq(CompanyRecord::type, dto.type)
            .eq(CompanyRecord::operateType, dto.operateType)
            .eq(CompanyRecord::operatorUserId, emp.userId)
            .ge(CompanyRecord::operateTime, dto.startTime)
            .le(CompanyRecord::operateTime, dto.endTime)
        val pageData = page(Page(dto.pageNum, dto.pageSize), wrapper)

        // 全部转换成VO对象
        val pageResult = pageData.records.map {
            val employee = employeeService.getById(it.operatorUserId)
            val department = departmentService.getById(employee.departmentId)
            val employeeVO = EmployeeVO.fromEmployee(
                employee,
                departmentService.getById(employee.departmentId)?.name,
                department?.name ?: "暂无部门")
            val target = when(it.type){
                CompanyRecord.TYPE_COMPANY -> null
                CompanyRecord.TYPE_ROLE -> roleService.getById(it.targetId)
                CompanyRecord.TYPE_DEPARTMENT -> departmentService.getById(it.targetId)
                CompanyRecord.TYPE_EMPLOYEE -> employeeService.getById(it.targetId)
                CompanyRecord.TYPE_MISSION -> missionService.getById(it.targetId)
                else -> null
            }
            CompanyRecordVO(
                id = it.id!!,
                type = it.type,
                operateType = it.operateType,
                target = target,
                message = it.message,
                operatorEmployee = employeeVO,
                operateTime = it.operateTime.toString()
            )
        }

        // 封装返回结果
        val result = PageResult(
            total = pageData.total,
            data = pageResult
        )
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, result)
    }
}