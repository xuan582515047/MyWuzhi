package com.wuzhi.server.service_tool.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.wuzhi.server.service_tool.pojo.po.Tag
import org.apache.ibatis.annotations.Mapper

@Mapper
interface TagMapper : BaseMapper<Tag> {
}