package com.wuzhi.server.common.config

import com.wuzhi.server.service_chat.DatabaseChatMemory
import com.wuzhi.server.service_chat.prompt.PromptConstant
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy


@Configuration
class AiConfig {
    //TODO 这里的apiKey全是写死的，上线需要换成环境变量

    @Bean
    @Qualifier("deepseekClient")
    fun deepseekClient(baseApi: OpenAiApi, baseModel: OpenAiChatModel, @Lazy chatMemory: DatabaseChatMemory): ChatClient {
        val deepseekApi: OpenAiApi? = baseApi.mutate()
            .baseUrl("https://api.deepseek.com")
            //.apiKey(System.getenv("DEEPSEEK_API_KEY"))
            .apiKey("sk-b3f6b75bf8e24c6ebddc713e079570e3")
            .build()
        val deepseekModel: OpenAiChatModel = baseModel.mutate()
            .openAiApi(deepseekApi)
            .defaultOptions(
                OpenAiChatOptions.builder()
                    .model("deepseek-reasoner")
                    .build()
            )
            .build()
        return getChatClient(deepseekModel, chatMemory)
    }

    @Bean
    @Qualifier("qwen3MaxClient")
    fun qwen3MaxClient(baseApi: OpenAiApi, baseModel: OpenAiChatModel, @Lazy chatMemory: DatabaseChatMemory): ChatClient {
        val qwenApi: OpenAiApi? = baseApi.mutate()
            .baseUrl("https://dashscope.aliyuncs.com/compatible-mode")
            //.apiKey(System.getenv("ALIYUN_API_KEY"))
            .apiKey("sk-0352f52bdad14cb9a5e5cd921b41b968")
            .build()
        val qwenModel: OpenAiChatModel = baseModel.mutate()
            .openAiApi(qwenApi)
            .defaultOptions(
                OpenAiChatOptions.builder()
                    .model("qwen3-max")
                    .build()
            )
            .build()
        return getChatClient(qwenModel, chatMemory)
    }

    @Bean
    @Qualifier("qwen3OmniFlashClient")
    fun qwen3OmniFlashClient(baseApi: OpenAiApi, baseModel: OpenAiChatModel, @Lazy chatMemory: DatabaseChatMemory): ChatClient {
        val qwenApi: OpenAiApi? = baseApi.mutate()
            .baseUrl("https://dashscope.aliyuncs.com/compatible-mode")
            //.apiKey(System.getenv("ALIYUN_API_KEY"))
            .apiKey("sk-0352f52bdad14cb9a5e5cd921b41b968")
            .build()
        val qwenModel: OpenAiChatModel = baseModel.mutate()
            .openAiApi(qwenApi)
            .defaultOptions(
                OpenAiChatOptions.builder()
                    .model("qwen3-omni-flash")
                    .build()
            )
            .build()
        return getChatClient(qwenModel, chatMemory)
    }

    private fun getChatClient(model: ChatModel, chatMemory: ChatMemory): ChatClient{
        return ChatClient.builder(model)
            .defaultSystem(PromptConstant.BASE_CHARACTOR_PROMPT)
            .defaultAdvisors(
                SimpleLoggerAdvisor(),  // 日志记录 Advisor
                MessageChatMemoryAdvisor.builder(chatMemory).build() // 消息保存 Advisor
            )
            .build()
    }
}