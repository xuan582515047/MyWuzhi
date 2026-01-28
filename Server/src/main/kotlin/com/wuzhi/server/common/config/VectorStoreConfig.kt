package com.wuzhi.server.common.config

import com.wuzhi.server.common.constant.VectorStoreFields
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.embedding.TokenCountBatchingStrategy
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.redis.RedisVectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import redis.clients.jedis.JedisPooled

@Configuration
class VectorStoreConfig{

    @Value("\${spring.data.redis.host}")
    private val redisHost: String = "localhost"

    @Value("\${spring.data.redis.port}")
    private val redisPort: Int = 6379

    @Bean
    fun jedisPooled(): JedisPooled {
        return JedisPooled(redisHost, redisPort)
    }

    @Bean("databaseVectorStore")
    fun vectorStore(jedisPooled: JedisPooled, embeddingModel: EmbeddingModel): VectorStore {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
            .indexName("database-index") // 索引名（可以理解为表名），默认："spring-ai-index"
            .prefix("database:") // key前缀，key = key前缀 + ID，默认："embedding:"
            .metadataFields(
                // 可选，设置元数据字段（不设置的话，是没办法用元数据过滤器的）
                RedisVectorStore.MetadataField.tag(VectorStoreFields.Companion.FILE_NAME),  // 文件名
                RedisVectorStore.MetadataField.tag(VectorStoreFields.Companion.USER_ID),  // 用户id
                RedisVectorStore.MetadataField.tag(VectorStoreFields.Companion.COMPANY_ID),  // 公司id
                RedisVectorStore.MetadataField.tag(VectorStoreFields.Companion.DATABASE_ID),  // 数据库id
                RedisVectorStore.MetadataField.tag(VectorStoreFields.Companion.CREATE_TIME), // 创建时间
            )
            .initializeSchema(true) // 初始化向量库，默认为 false
            .batchingStrategy(TokenCountBatchingStrategy()) // 批处理策略，默认：TokenCountBatchingStrategy
            .build()
    }

}