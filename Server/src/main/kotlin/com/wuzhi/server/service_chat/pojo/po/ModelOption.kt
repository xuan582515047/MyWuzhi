package com.wuzhi.server.service_chat.pojo.po

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime
/* 初始数据
    code	name	create_time
    deepseek	DeepSeek	当前时间
    qwen3Max	Qwen3-Max	当前时间

 */
@TableName("data_model_option")
class ModelOption(

    @TableId("code")
    val code: String,

    @TableField("name")
    val name: String,

    @TableField("create_time")
    val createTime: LocalDateTime,
) {
}