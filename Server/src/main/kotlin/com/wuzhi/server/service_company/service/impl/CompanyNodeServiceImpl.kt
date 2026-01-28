package com.wuzhi.server.service_company.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.wuzhi.server.service_company.mapper.CompanyNodeMapper
import com.wuzhi.server.service_company.pojo.po.manage.CompanyNode
import com.wuzhi.server.service_company.pojo.vo.company.CompanyNodeVO
import com.wuzhi.server.service_company.service.CompanyNodeService
import org.springframework.stereotype.Service

@Service
class CompanyNodeServiceImpl: CompanyNodeService, ServiceImpl<CompanyNodeMapper, CompanyNode>() {
    override fun getTree(companyId: String, departmentId: String): CompanyNodeVO {
        // 查询出当前节点
        val nodeWrapper = KtQueryWrapper(CompanyNode::class.java)
            .eq(CompanyNode::companyId, companyId)
            .eq(CompanyNode::targetId, departmentId)
        val currentNode = getOne(nodeWrapper)

        // 查询出所有得到下级节点
        val nodeListWrapper = KtQueryWrapper(CompanyNode::class.java)
            .eq(CompanyNode::companyId, companyId)
            .gt(CompanyNode::level, currentNode.level)
        val nodeList = list(nodeListWrapper)

        // 查询当前节点的子节点
        val children = getChildrenTree(currentNode.id!!, nodeList)
        val result = CompanyNodeVO(
            id = currentNode.id,
            name = currentNode.name,
            type = currentNode.type,
            children = children
        )

        // 递归构建子树
        return result
    }

    private fun getChildrenTree(parentId: String, nodeList: List<CompanyNode>?): List<CompanyNodeVO> {
        if (nodeList == null) {
            return emptyList()
        }
        // 按parentId分组
        val groupedNodes = nodeList.groupBy { it.parentId }

        // 递归构建子树
        fun buildTree(parentId: String?): List<CompanyNodeVO> {
            val children = groupedNodes[parentId]
                ?: return emptyList()

            return children.map { node ->
                CompanyNodeVO(
                    id = node.id!!,
                    name = node.name,
                    type = node.type,
                    children = buildTree(node.id) // 递归构建子树
                )
            }
        }

        return buildTree(parentId)

    }
}