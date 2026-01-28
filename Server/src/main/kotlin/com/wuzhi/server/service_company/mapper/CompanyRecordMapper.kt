package com.wuzhi.server.service_company.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.wuzhi.server.service_company.pojo.po.manage.CompanyRecord
import org.apache.ibatis.annotations.Mapper

@Mapper
interface CompanyRecordMapper: BaseMapper<CompanyRecord> {
}