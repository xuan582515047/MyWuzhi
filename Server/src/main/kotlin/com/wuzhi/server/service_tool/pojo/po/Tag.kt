package com.wuzhi.server.service_tool.pojo.po

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName

@TableName("data_tag")
class Tag(

    @TableId("id")
    val id: String,

    @TableField("name")
    val name: String,
) {

}