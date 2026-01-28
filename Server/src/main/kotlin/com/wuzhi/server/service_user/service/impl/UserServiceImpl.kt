package com.wuzhi.server.service_user.service.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.wuzhi.server.service_user.mapper.UserMapper
import com.wuzhi.server.service_user.pojo.po.User
import com.wuzhi.server.service_user.service.UserService
import org.springframework.stereotype.Service

@Service
class UserServiceImpl: UserService, ServiceImpl<UserMapper, User>() {

}
