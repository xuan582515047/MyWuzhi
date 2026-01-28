package com.wuzhi.server.service_chat.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.fasterxml.jackson.databind.ObjectMapper
import com.wuzhi.server.common.constant.OkMessages
import com.wuzhi.server.common.constant.VectorStoreFields
import com.wuzhi.server.common.context.UserContext
import com.wuzhi.server.common.pojo.OptionResult
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.common.util.FileUtil
import com.wuzhi.server.common.util.MediaConvertUtil
import com.wuzhi.server.service_chat.mapper.ChatMapper
import com.wuzhi.server.service_chat.mapper.ModelOptionMapper
import com.wuzhi.server.service_chat.pojo.dto.ConversationDTO
import com.wuzhi.server.service_chat.pojo.dto.MessageDTO
import com.wuzhi.server.service_chat.pojo.dto.MissionToolDTO
import com.wuzhi.server.service_chat.pojo.po.Chat
import com.wuzhi.server.service_chat.pojo.po.Conversation
import com.wuzhi.server.service_chat.pojo.vo.ConversationVO
import com.wuzhi.server.service_chat.pojo.vo.NormalChatVO
import com.wuzhi.server.service_chat.prompt.PromptConstant
import com.wuzhi.server.service_chat.service.ChatService
import com.wuzhi.server.service_chat.service.ConversationService
import com.wuzhi.server.service_chat.tool.MissionAddTool
import com.wuzhi.server.service_company.pojo.po.manage.Department
import com.wuzhi.server.service_company.pojo.po.manage.Employee
import com.wuzhi.server.service_company.pojo.po.manage.Mission
import com.wuzhi.server.service_company.pojo.vo.mission.MissionVO
import com.wuzhi.server.service_company.service.CompanyService
import com.wuzhi.server.service_company.service.EmployeeService
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.content.Media
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@Service
class ChatServiceImpl(
    private val applicationContext: ApplicationContext,
    private val conversationService: ConversationService,
    @Lazy private val companyService: CompanyService,
    @Lazy private val employeeService: EmployeeService,
    private val modelOptionMapper: ModelOptionMapper,
    @Qualifier("databaseVectorStore") private val vectorStore: VectorStore
) : ChatService, ServiceImpl<ChatMapper, Chat>() {

    @Value("\${spring.ai.vectorstore.redis.prefix}")
    val DOC_PREFIX: String = "doc:"

    companion object{
        const val TOP_K = 20;
        const val THRESHOLD = 0.6;
    }

    override fun getModelList(): ResponseResult {
        val modelList = modelOptionMapper.selectList(null)
            .map { OptionResult(
                code = it.code,
                name = it.name
            ) }
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, modelList)
    }

    override fun callMission(missions: List<MissionVO>, targetDep: Department, targetEmployeeId: String): MissionToolDTO {
        val chatClient = getChatClient("deepseek")
        val company = companyService.getById(targetDep.companyId)
        val employee = employeeService.getById(targetEmployeeId)
        /*TODO 有问题，暂时是直接返回
        val result = chatClient.prompt()
            .system(PromptConstant.getMissionPrompt(
                employeeInfo = employee.toString(),
                companyInfo = company.toString(),
                missionInfo = targetDep.toString(),
                departmentInfo = missions.toString()
            ))
            .tools(missionAddTool)
            .call()
            .content()
        return objectMapper.readValue(result, MissionToolDTO::class.java)
        */
        return MissionToolDTO(
            name = "test",
            description = "test",
            startTime = "2025-01-01T00:00:00",
            endTime = "2025-01-01T00:00:00",
            status = Mission.STATUS_NOT_START
        )
    }

    @Transactional
    override fun callMessage(dto: MessageDTO): ResponseResult {

        //TODO 单模态则解析文件，转为文本，多模态则解析文件，转为Media

        // 非流式返回
        val content: String? = createSender(dto)
            .call() // 直接返回字符串结果
            .content()
        return ResponseResult.success(OkMessages.MESSAGE_SEND_SUCCESS, content)
    }

    override fun getConversationList(): ResponseResult {
        val wrapper = KtQueryWrapper(Conversation::class.java)
        wrapper.eq(Conversation::userId, UserContext.getUserId())
        wrapper.eq(Conversation::deleted, false)
        wrapper.orderBy(true, true, Conversation::createTime)
        val conversationList = conversationService.list(wrapper).map(ConversationVO::fromPO)
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, conversationList)
    }

    override fun getMessageList(conversationId: String): ResponseResult {
        val wrapper = KtQueryWrapper(Chat::class.java)
        wrapper.eq(Chat::conversationId, conversationId)
        wrapper.eq(Chat::deleted, false)
        wrapper.orderBy(true, true, Chat::createTime)
        val messageList = list(wrapper).map(NormalChatVO::fromPO)
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, messageList)
    }

    @Transactional
    override fun deleteConversation(conversationId: String): ResponseResult {
        // 逻辑删除会话
        val conversation = conversationService.getById(conversationId)
        conversation.deleted = true
        conversationService.updateById(conversation)
        // 逻辑删除对话内容
        val wrapper = KtUpdateWrapper(Chat::class.java)
        wrapper.eq(Chat::conversationId, conversationId)
        wrapper.set(Chat::deleted, true)
        this.update(wrapper)
        return ResponseResult.success(OkMessages.DELETE_SUCCESS)

    }

    override fun updateConversation(conversationDTO: ConversationDTO): ResponseResult {
        val conversation = conversationService.getById(conversationDTO.id)
        conversation.title = conversationDTO.title
        conversation.updateTime = LocalDateTime.now()
        conversationService.updateById(conversation)
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    private fun getChatClient(modelName: String): ChatClient{
        return applicationContext.getBean(modelName+"Client", ChatClient::class.java)
    }

    private fun createSender(dto: MessageDTO): ChatClient.ChatClientRequestSpec{
        // 根据model获取对应的chatClient
        val chatClient = getChatClient(dto.model)

        //TODO 处理文件，这里仅上传，本地没有保存
        val mediaList = ArrayList<Media>()
        if (dto.files != null && dto.files.isNotEmpty()) {
             mediaList.addAll(MediaConvertUtil.fromMultipartFiles(dto.files))
        }

        // 创建sender
        val sender = chatClient.prompt()
            .system(PromptConstant.BASE_CHARACTOR_PROMPT)
            .user({ p -> p.text(dto.message).media(*mediaList.toTypedArray()) })
            .advisors({ a -> a.param(ChatMemory.CONVERSATION_ID, dto.conversationId) })

        // 查询知识库，配置advisor
        if (dto.databaseIds != null && dto.databaseIds.isNotEmpty()){
            val filter = FilterExpressionBuilder()
                .`in`(VectorStoreFields.DATABASE_ID, dto.databaseIds)
                .build()
            val searchRequest = SearchRequest.builder()
                .query(dto.message) // 基准文本
                .similarityThreshold(THRESHOLD) // 最低相似度
                .topK(TOP_K) // 选取条数（根据相似度）
                .filterExpression(filter) // 元数据过滤器（可以用String，也可以用下面的）
                .build()
            val qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .build()
            sender.advisors(qaAdvisor)
        }

        return sender
    }

}
