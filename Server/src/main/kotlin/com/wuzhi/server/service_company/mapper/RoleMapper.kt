package com.wuzhi.server.service_company.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.wuzhi.server.service_company.pojo.po.auth.Role
import org.apache.ibatis.annotations.Mapper

@Mapper
interface RoleMapper : BaseMapper<Role> {
}