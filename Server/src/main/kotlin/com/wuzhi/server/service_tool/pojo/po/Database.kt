package com.wuzhi.server.service_tool.pojo.po

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler

@TableName("data_database", autoResultMap = true)
data class Database(

    @TableId("id")
    var id: String? = null,

    @TableField("name")
    var name: String,

    @TableField("description")
    var description: String? = null,

    @TableField("type")
    var type: String,

    @TableField("url")
    var url: String? = null,

    @TableField("text")
    var text: String? = null,
){
    companion object{
        const val TYPE_URL = "url"
        const val TYPE_LOCAL = "local"
        const val TYPE_TEXT = "text"
    }
}