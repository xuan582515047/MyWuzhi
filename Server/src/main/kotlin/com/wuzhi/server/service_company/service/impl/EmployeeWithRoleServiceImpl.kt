package com.wuzhi.server.service_company.service.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.wuzhi.server.service_company.mapper.EmployeeWithRoleMapper
import com.wuzhi.server.service_company.pojo.po.auth.EmployeeWithRole
import com.wuzhi.server.service_company.service.EmployeeWithRoleService
import org.springframework.stereotype.Service

@Service
class EmployeeWithRoleServiceImpl: EmployeeWithRoleService, ServiceImpl<EmployeeWithRoleMapper, EmployeeWithRole>() {
}
