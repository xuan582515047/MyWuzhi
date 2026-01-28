package com.wuzhi.server.service_user.service

import com.baomidou.mybatisplus.extension.service.IService
import com.wuzhi.server.service_user.pojo.po.UserRecord

interface UserRecordService: IService<UserRecord> {

    fun loginRecord(userId: String)

    fun refreshTokenRecord(userId: String)
}