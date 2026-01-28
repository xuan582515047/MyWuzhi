package com.wuzhi.server.service_chat.controller

import com.wuzhi.server.common.constant.BaseUrlConstant
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.service_chat.pojo.dto.ConversationDTO
import com.wuzhi.server.service_chat.pojo.dto.MessageDTO
import com.wuzhi.server.service_chat.service.ChatService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chat")
class ChatController(
    private val chatService: ChatService
) {

    @GetMapping("/models")
    fun getModelList(): ResponseResult {
        return chatService.getModelList()
    }

    /**
     * 发送消息
     */
    @PostMapping("/message/call", consumes = ["multipart/form-data"])
    fun callMessage(@Valid messageDTO: MessageDTO): ResponseResult {
        return chatService.callMessage(messageDTO)
    }

    /**
     * 获取对话列表
     */
    @GetMapping("/conversations")
    fun getConversationList(): ResponseResult {
        return chatService.getConversationList()
    }

    /**
     * 删除对话
     */
    @DeleteMapping("/conversations/{conversationId}")
    fun deleteConversation(@PathVariable("conversationId") conversationId: String): ResponseResult {
        return chatService.deleteConversation(conversationId)
    }

    /**
     * 更新对话
     */
    @PutMapping("/conversations")
    fun updateConversation(@RequestBody @Valid conversationDTO: ConversationDTO): ResponseResult {
        return chatService.updateConversation(conversationDTO)
    }

    /**
     * 获取消息列表
     */
    @GetMapping("/messages/{conversationId}")
    fun getMessageList(@PathVariable("conversationId") conversationId: String): ResponseResult {
        return chatService.getMessageList(conversationId)
    }
}