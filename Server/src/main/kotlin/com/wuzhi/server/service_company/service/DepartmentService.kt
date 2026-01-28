package com.wuzhi.server.service_company.service

import com.baomidou.mybatisplus.extension.service.IService
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.pojo.dto.department.DepAddDTO
import com.wuzhi.server.service_company.pojo.dto.department.DepChangeDTO
import com.wuzhi.server.service_company.pojo.dto.department.DepEmpDTO
import com.wuzhi.server.service_company.pojo.dto.department.DepUpdateDTO
import com.wuzhi.server.service_company.pojo.po.manage.Department

interface DepartmentService: IService<Department> {

    /**
     * 添加部门
     */
    fun addDepartment(dto: DepAddDTO): ResponseResult

    /**
     * 删除部门
     */
    fun deleteDepartment(departmentId: String): ResponseResult

    /**
     * 更新部门
     */
    fun updateDepartment(dto: DepUpdateDTO): ResponseResult

    /**
     * 获取部门统计信息
     */
    fun getDepartmentStatistics(departmentId: String): ResponseResult

    /**
     * 修改部门的父部门
     */
    fun changeDepParent(dto: DepChangeDTO): ResponseResult

}