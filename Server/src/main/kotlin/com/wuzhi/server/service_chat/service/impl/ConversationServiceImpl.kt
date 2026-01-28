package com.wuzhi.server.service_chat.service.impl

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.wuzhi.server.service_chat.mapper.ConversationMapper
import com.wuzhi.server.service_chat.pojo.po.Conversation
import com.wuzhi.server.service_chat.service.ConversationService
import org.springframework.stereotype.Service

@Service
class ConversationServiceImpl: ConversationService, ServiceImpl<ConversationMapper, Conversation>() {

}
