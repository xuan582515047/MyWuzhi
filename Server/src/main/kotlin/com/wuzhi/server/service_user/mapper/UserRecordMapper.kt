package com.wuzhi.server.service_user.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.wuzhi.server.service_user.pojo.po.UserRecord
import org.apache.ibatis.annotations.Mapper

@Mapper
interface UserRecordMapper: BaseMapper<UserRecord> {
}