package com.wuzhi.server.service_tool.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.wuzhi.server.service_tool.pojo.po.ToolNode
import org.apache.ibatis.annotations.Mapper

@Mapper
interface ToolNodeMapper : BaseMapper<ToolNode> {
}