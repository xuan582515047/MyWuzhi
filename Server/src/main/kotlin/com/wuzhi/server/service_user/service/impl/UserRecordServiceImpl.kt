package com.wuzhi.server.service_user.service.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.wuzhi.server.service_user.mapper.UserRecordMapper
import com.wuzhi.server.service_user.pojo.po.UserRecord
import com.wuzhi.server.service_user.service.UserRecordService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserRecordServiceImpl: UserRecordService, ServiceImpl<UserRecordMapper, UserRecord>(){
    override fun loginRecord(userId: String) {
        val record = UserRecord(
            userId = userId,
            recordType = UserRecord.TYPE_LOGIN,
            createTime = LocalDateTime.now()
        )
        save(record)
    }

    override fun refreshTokenRecord(userId: String) {
        val record = UserRecord(
            userId = userId,
            recordType = UserRecord.TYPE_REFRESH_TOKEN,
            createTime = LocalDateTime.now()
        )
        save(record)
    }
}