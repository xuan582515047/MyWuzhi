package com.wuzhi.server.service_chat.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.wuzhi.server.service_chat.pojo.po.Conversation
import org.apache.ibatis.annotations.Mapper

@Mapper
interface ConversationMapper : BaseMapper<Conversation> {
}