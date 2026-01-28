package com.wuzhi.server.service_company.service.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.wuzhi.server.service_company.mapper.RoleWithPermissionMapper
import com.wuzhi.server.service_company.pojo.po.auth.RoleWithPermission
import com.wuzhi.server.service_company.service.RoleWithPermissionService
import org.springframework.stereotype.Service

@Service
class RoleWithPermissionServiceImpl: RoleWithPermissionService, ServiceImpl<RoleWithPermissionMapper, RoleWithPermission>() {
}
