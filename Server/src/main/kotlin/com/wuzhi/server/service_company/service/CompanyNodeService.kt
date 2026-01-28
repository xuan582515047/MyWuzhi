package com.wuzhi.server.service_company.service

import com.baomidou.mybatisplus.extension.service.IService
import com.wuzhi.server.service_company.pojo.po.manage.CompanyNode
import com.wuzhi.server.service_company.pojo.vo.company.CompanyNodeVO

interface CompanyNodeService: IService<CompanyNode> {

    /**
     * 获取公司的部门和员工树
     */
    fun getTree(companyId: String, departmentId: String): CompanyNodeVO
}