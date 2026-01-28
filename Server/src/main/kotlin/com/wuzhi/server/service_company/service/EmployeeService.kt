package com.wuzhi.server.service_company.service

import com.baomidou.mybatisplus.extension.service.IService
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.pojo.dto.employee.EmpAddDTO
import com.wuzhi.server.service_company.pojo.dto.employee.EmpChangeDTO
import com.wuzhi.server.service_company.pojo.dto.employee.EmpDetailDTO
import com.wuzhi.server.service_company.pojo.dto.employee.EmpQueryDTO
import com.wuzhi.server.service_company.pojo.dto.employee.EmpUpdateDTO
import com.wuzhi.server.service_company.pojo.po.manage.Employee

interface EmployeeService: IService<Employee> {

    /**
     * 获取员工权限
     */
    fun getEmpPermission(employeeId: String): ResponseResult

    /**
     * 添加员工
     */
    fun addEmployee(dto: EmpAddDTO): ResponseResult

    /**
     * 删除员工
     */
    fun deleteEmployee(employeeId: String): ResponseResult

    /**
     * 更新员工
     */
    fun updateEmployee(dto: EmpUpdateDTO): ResponseResult

    /**
     * 获取员工列表
     */
    fun queryEmployeeList(dto: EmpQueryDTO): ResponseResult

    /**
     * 获取员工统计信息
     */
    fun getEmployeeStatistics(employeeId: String): ResponseResult

    /**
     * 修改员工的父部门
     */
    fun changeEmpParent(dto: EmpChangeDTO): ResponseResult

    /**
     * 获取员工详情
     */
    fun getEmployeeDetail(dto: EmpDetailDTO): ResponseResult
}