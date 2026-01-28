package com.wuzhi.server.service_company.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.wuzhi.server.service_company.pojo.po.auth.EmployeeWithRole
import org.apache.ibatis.annotations.Mapper

@Mapper
interface EmployeeWithRoleMapper: BaseMapper<EmployeeWithRole> {
}