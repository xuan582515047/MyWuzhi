package com.wuzhi.server.service_tool.service.impl

import com.baomidou.mybatisplus.extension.kotlin.KtQueryWrapper
import com.baomidou.mybatisplus.extension.kotlin.KtUpdateWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.wuzhi.server.common.constant.ErrorMessages
import com.wuzhi.server.common.constant.OkMessages
import com.wuzhi.server.common.constant.VectorStoreFields
import com.wuzhi.server.common.context.UserContext
import com.wuzhi.server.common.pojo.ResponseResult
import com.wuzhi.server.common.util.FileUtil
import com.wuzhi.server.service_tool.mapper.DatabaseMapper
import com.wuzhi.server.service_tool.mapper.DatabaseNodeMapper
import com.wuzhi.server.service_tool.pojo.dto.database.DatabaseAddDTO
import com.wuzhi.server.service_tool.pojo.dto.database.DatabaseEditDTO
import com.wuzhi.server.service_tool.pojo.dto.database.DatabaseNodeAddDTO
import com.wuzhi.server.service_tool.pojo.dto.database.DatabaseNodeEditDTO
import com.wuzhi.server.service_tool.pojo.dto.database.DatabaseNodeChangeDTO
import com.wuzhi.server.service_tool.pojo.dto.NodeStatusDTO
import com.wuzhi.server.service_tool.pojo.po.Database
import com.wuzhi.server.service_tool.pojo.po.DatabaseNode
import com.wuzhi.server.service_tool.pojo.vo.DatabaseDetailVO
import com.wuzhi.server.service_tool.pojo.vo.DatabaseTreeVO
import com.wuzhi.server.service_tool.service.DatabaseService
import com.wuzhi.server.service_tool.util.DocSplitter
import org.springframework.ai.document.Document
import org.springframework.ai.reader.ExtractedTextFormatter
import org.springframework.ai.reader.pdf.PagePdfDocumentReader
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig
import org.springframework.ai.reader.tika.TikaDocumentReader
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID


@Service
class DatabaseServiceImpl(
    @Qualifier("databaseVectorStore") private val vectorStore: VectorStore,
    private val databaseNodeMapper: DatabaseNodeMapper
): DatabaseService, ServiceImpl<DatabaseMapper, Database>() {

    @Transactional
    override fun updateDatabaseNodeStatus(dto: List<NodeStatusDTO>): ResponseResult {
        dto.forEach {
            val databaseNode = databaseNodeMapper.selectById(it.id)
            databaseNode.status = it.status
            databaseNodeMapper.updateById(databaseNode)
        }
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    @Transactional
    override fun addDatabase(dto: DatabaseAddDTO): ResponseResult {
        // 构建数据库对象
        val database = Database(
            id = UUID.randomUUID().toString().replace("-", ""),
            name = dto.name,
            description = dto.description,
            type = dto.type,
            text = dto.text,
        )

        // 根据类型不同，处理不同逻辑
        if (dto.type == Database.TYPE_LOCAL){
            // 文件非空检测
            if ((dto.file == null)){
                return ResponseResult.fail(ErrorMessages.LOCAL_BUT_NO_FILE)
            }

            // 将文件保存到本地
            try {
                val localFileUrl = FileUtil.saveFile(dto.file)
                database.url = localFileUrl

                // 获取本地文件路径并处理文件，存入向量库
                val localFilePath = FileUtil.getLocalPathFromUrl(localFileUrl).toString()
                val documentList = handleResource(localFilePath, dto, database.id!!)
                vectorStore.add(documentList)
            } catch (e: Exception) {
                return ResponseResult.fail(e.message.toString())
            }

        } else if (dto.type == Database.TYPE_URL){
            //TODO URL类型数据库
            return ResponseResult.fail("还没做")
        } else if (dto.type == Database.TYPE_TEXT){
            // 文本检测
            if (dto.text == null || dto.text.isEmpty()){
                return ResponseResult.fail(ErrorMessages.TEXT_BUT_NO_TEXT)
            }

            // 处理文本，存入向量库
            val documentList = handleText(dto.text, dto, database.id!!)
            vectorStore.add(documentList)
        } else {
            return ResponseResult.fail(ErrorMessages.UNKNOWN_DATABASE_TYPE)
        }

        // 保存数据库对象
        save(database)

        // 校验父节点id，可以为null，但不能为空字符串
        var parentId = null as String?
        if (dto.parentId != ""){
            parentId = dto.parentId
        }

        // 保存节点对象（叶子节点）
        databaseNodeMapper.insert(DatabaseNode(
            name = dto.name,
            type = DatabaseNode.TYPE_LEAF,
            companyId = dto.companyId,
            createTime = LocalDateTime.now(),
            updateTime = LocalDateTime.now(),
            operatorUserId = UserContext.getUserId(),
            parentId = parentId,
            databaseId = database.id,
            userId = UserContext.getUserId(),
            status = DatabaseNode.STATUS_ENABLE
        ))

        return ResponseResult.success(OkMessages.ADD_SUCCESS)
    }

    override fun addDatabaseNode(dto: DatabaseNodeAddDTO): ResponseResult {
        // 校验父节点id，可以为null，但不能为空字符串
        var parentId = null as String?
        if (dto.parentId != null && dto.parentId != ""){
            parentId = dto.parentId
        }

        // 保存节点对象（中间节点）
        databaseNodeMapper.insert(DatabaseNode(
            name = dto.name,
            type = DatabaseNode.TYPE_MIDDLE,
            companyId = dto.companyId,
            createTime = LocalDateTime.now(),
            updateTime = LocalDateTime.now(),
            operatorUserId = UserContext.getUserId(),
            parentId = parentId,
            userId = UserContext.getUserId(),
            status = DatabaseNode.STATUS_ENABLE
        ))
        return ResponseResult.success(OkMessages.ADD_SUCCESS)
    }

    @Transactional
    override fun deleteDatabase(id: String): ResponseResult {
        // 删除向量库数据
        val filter = FilterExpressionBuilder().eq(VectorStoreFields.DATABASE_ID, id).build()
        vectorStore.delete(filter)

        // 删除节点对象
        val wrapper = KtQueryWrapper(DatabaseNode::class.java)
            .eq(DatabaseNode::databaseId, id)
        databaseNodeMapper.delete(wrapper)

        // 删除数据库对象
        removeById(id)

        return ResponseResult.success(OkMessages.DELETE_SUCCESS)
    }

    override fun deleteDatabaseNode(id: String): ResponseResult {
        // 直接删除节点
        databaseNodeMapper.deleteById(id)
        return ResponseResult.success(OkMessages.DELETE_SUCCESS)
    }

    @Transactional
    override fun editDatabase(dto: DatabaseEditDTO): ResponseResult {
        // 更新数据库对象
        val dataUpdateWrapper = KtUpdateWrapper(Database::class.java)
            .eq(Database::id, dto.id)
            .set(dto.name != null, Database::name, dto.name)
            .set(dto.description != null, Database::description, dto.description)
            .set(dto.text != null, Database::text, dto.text)
        update(dataUpdateWrapper)

        // 更新节点对象
        val nodeUpdateWrapper = KtUpdateWrapper(DatabaseNode::class.java)
            .eq(DatabaseNode::id, dto.id)
            .set(dto.name != null, DatabaseNode::name, dto.name)
            .set(DatabaseNode::updateTime, LocalDateTime.now())
            .set(DatabaseNode::operatorUserId, UserContext.getUserId())
        databaseNodeMapper.update(nodeUpdateWrapper)

        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    override fun editDatabaseNode(dto: DatabaseNodeEditDTO): ResponseResult {
        // 更新节点对象
        val updateWrapper = KtUpdateWrapper(DatabaseNode::class.java)
            .eq(DatabaseNode::id, dto.id)
            .set(dto.name != null, DatabaseNode::name, dto.name)
            .set(DatabaseNode::updateTime, LocalDateTime.now())
            .set(DatabaseNode::operatorUserId, UserContext.getUserId())
        databaseNodeMapper.update(updateWrapper)

        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    override fun getDatabaseDetail(id: String): ResponseResult {
        // 查询节点对象
        val node = databaseNodeMapper.selectById(id)
            ?: return ResponseResult.fail(ErrorMessages.DATABASE_NODE_NOT_EXIST)

        // 查询数据库对象
        val database = getById(node.databaseId)
            ?: return ResponseResult.fail(ErrorMessages.DATABASE_NOT_EXIST)

        val vo = DatabaseDetailVO(
            id = database.id,
            name = database.name,
            description = database.description,
            type = database.type,
            url = database.url,
            text = database.text
        )
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, vo)
    }

    override fun getDatabaseTree(companyId: String?): ResponseResult {
        // 查询出所有的节点
        val wrapper = KtQueryWrapper(DatabaseNode::class.java)
            .eq(DatabaseNode::userId, UserContext.getUserId())
            .eq(companyId != null, DatabaseNode::companyId, companyId)
        val nodeList = databaseNodeMapper.selectList(wrapper)

        // 如果没有节点，直接返回空树
        if (nodeList == null || nodeList.isEmpty()){
            return ResponseResult.success(OkMessages.QUERY_SUCCESS, null)
        }

        // 生成树
        val tree = getNodeChildren(null, nodeList)
        return ResponseResult.success(OkMessages.QUERY_SUCCESS, tree)
    }

    private fun handleText(text: String, dto: DatabaseAddDTO, databaseId: String): List<Document>{
        // 设置元数据
        val document = Document(text)
        document.metadata[VectorStoreFields.COMPANY_ID] = dto.companyId ?: ""
        document.metadata[VectorStoreFields.USER_ID] = UserContext.getUserId()
        document.metadata[VectorStoreFields.DATABASE_ID] = databaseId
        document.metadata[VectorStoreFields.CREATE_TIME] = System.currentTimeMillis().toString()

        // 文档分割返回
        val docSplitter = DocSplitter()
        return docSplitter.apply(listOf(document))
    }

    private fun handleResource(filePath: String, dto: DatabaseAddDTO, databaseId: String): List<Document>{
        // 获取文件名和文件内容
        val file = java.io.File(filePath)
        val resource = org.springframework.core.io.FileSystemResource(file)
        val fileName = file.name
        val suffix = file.name.substringAfterLast(".", "")

        // 设置元数据
        val metaData = HashMap<String, String>()
        metaData[VectorStoreFields.COMPANY_ID] = dto.companyId ?: ""
        metaData[VectorStoreFields.USER_ID] = UserContext.getUserId()
        metaData[VectorStoreFields.FILE_NAME] = fileName
        metaData[VectorStoreFields.DATABASE_ID] = databaseId
        metaData[VectorStoreFields.CREATE_TIME] = System.currentTimeMillis().toString()

        // 分类处理
        val documentList = ArrayList<Document>()
        if (suffix == "pdf"){
            val pdfReader = PagePdfDocumentReader(
                resource,
                PdfDocumentReaderConfig.builder()
                    .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                    .withPagesPerDocument(1)
                    .build()
            )
            val documents = pdfReader.read()
            documentList.addAll(documents)
        }else{
            val docReader = TikaDocumentReader(resource)
            val documents = docReader.get()
            documentList.addAll(documents)
        }

        // 添加元数据
        documentList.forEach {
            it.metadata.putAll(metaData)
        }

        // 文档分割返回
        val docSplitter = DocSplitter()
        return docSplitter.apply(documentList)

    }

    private fun getNodeChildren(parentId: String?, nodeList: List<DatabaseNode>): List<DatabaseTreeVO>{
        return nodeList.filter { it.parentId == parentId }.map {
            val id = it.id!!
            if (it.type == DatabaseNode.TYPE_MIDDLE){
                // 中间节点，递归查询子节点
                DatabaseTreeVO(
                    id = id,
                    name = it.name,
                    status = it.status,
                    type = DatabaseNode.TYPE_MIDDLE,
                    children = getNodeChildren(id, nodeList)
                )
            }else{
                // 叶子节点，直接返回
                DatabaseTreeVO(
                    id = id,
                    name = it.name,
                    status = it.status,
                    type = DatabaseNode.TYPE_LEAF,
                    children = null
                )
            }
        }
    }

    /**
     * 修改数据库节点的父级（中间节点和叶子节点）
     */
    @Transactional
    override fun changeDatabaseParent(dto: DatabaseNodeChangeDTO): ResponseResult {
        // 查询当前节点
        val currentNode = databaseNodeMapper.selectById(dto.nodeId)
            ?: return ResponseResult.error(ErrorMessages.DATABASE_NODE_NOT_EXIST)
        // 更新节点
        // 将空字符串转换为 null，确保移动到根节点时能正确处理
        val parentId = if (dto.parentId.isNullOrEmpty()) null else dto.parentId
        val updateWrapper = KtUpdateWrapper(DatabaseNode::class.java)
            .eq(DatabaseNode::id, dto.nodeId)
            .set(DatabaseNode::parentId, parentId)
            .set(DatabaseNode::updateTime, LocalDateTime.now())
            .set(DatabaseNode::operatorUserId, UserContext.getUserId())
        databaseNodeMapper.update(updateWrapper)
        
        return ResponseResult.success(OkMessages.UPDATE_SUCCESS)
    }

    /**
     * 检查 targetParentId 是否是 nodeId 的后代节点（防止循环引用）
     */
    private fun isNodeDescendant(nodeId: String, targetParentId: String): Boolean {
        // 递归查询 targetParentId 的所有祖先节点
        var currentId: String? = targetParentId
        while (currentId != null) {
            if (currentId == nodeId) {
                return true // 找到了，说明 targetParentId 是 nodeId 的后代
            }
            val node = databaseNodeMapper.selectById(currentId)
            currentId = node?.parentId
        }
        return false
    }

}