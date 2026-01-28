package com.wuzhi.server.service_company.service

import com.baomidou.mybatisplus.extension.service.IService
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_company.pojo.dto.company.CompanyAddDTO
import com.wuzhi.server.service_company.pojo.dto.company.CompanyAuthDTO
import com.wuzhi.server.service_company.pojo.dto.company.CompanyUpdateDTO
import com.wuzhi.server.service_company.pojo.po.manage.Company

interface CompanyService : IService<Company> {

    /**
     * 获取公司权限列表
     */
    fun getCompanyPermission(companyId: String): ResponseResult

    /**
     * 获取公司详情信息
     */
    fun getCompanyDetail(companyId: String): ResponseResult

    /**
     * 认证公司
     */
    fun authCompany(dto: CompanyAuthDTO): ResponseResult

    /**
     * 添加公司
     */
    fun addCompany(dto: CompanyAddDTO): ResponseResult

    /**
     * 删除公司
     */
    fun deleteCompany(companyId: String): ResponseResult

    /**
     * 更新公司
     */
    fun updateCompany(dto: CompanyUpdateDTO): ResponseResult

    /**
     * 获取公司列表
     */
    fun getCompanyList(): ResponseResult

    /**
     * 获取公司统计信息
     */
    fun getCompanyStatistics(companyId: String): ResponseResult

    /**
     * 获取公司的员工和部门树
     */
    fun getCompanyTree(companyId: String): ResponseResult


}