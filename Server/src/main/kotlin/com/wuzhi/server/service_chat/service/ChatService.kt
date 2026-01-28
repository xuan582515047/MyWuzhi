package com.wuzhi.server.service_chat.service

import com.baomidou.mybatisplus.extension.service.IService
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_chat.pojo.dto.ConversationDTO
import com.wuzhi.server.service_chat.pojo.dto.MessageDTO
import com.wuzhi.server.service_chat.pojo.dto.MissionToolDTO
import com.wuzhi.server.service_chat.pojo.po.Chat
import com.wuzhi.server.service_company.pojo.po.manage.Department
import com.wuzhi.server.service_company.pojo.vo.mission.MissionVO

interface ChatService : IService<Chat> {

    /**
     * 获取模型列表
     */
    fun getModelList(): ResponseResult

    /**
     * 任务分配AI
     */
    fun callMission(missions: List<MissionVO>, targetDep: Department, targetEmployeeId: String): MissionToolDTO

    /**
     * 非流式返回
     */
    fun callMessage(dto: MessageDTO): ResponseResult

    /**
     * 获取会话列表
     */
    fun getConversationList(): ResponseResult

    /**
     * 获取消息列表
     */
    fun getMessageList(conversationId: String): ResponseResult

    /**
     * 删除会话
     */
    fun deleteConversation(conversationId: String): ResponseResult

    /**
     * 更新会话
     */
    fun updateConversation(conversationDTO: ConversationDTO): ResponseResult
}