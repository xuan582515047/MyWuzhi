package com.wuzhi.server.service_tool.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.wuzhi.server.service_tool.pojo.po.DatabaseNode
import org.apache.ibatis.annotations.Mapper

@Mapper
interface DatabaseNodeMapper : BaseMapper<DatabaseNode> {
}