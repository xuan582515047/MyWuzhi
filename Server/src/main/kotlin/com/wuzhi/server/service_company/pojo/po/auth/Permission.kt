package com.wuzhi.server.service_company.pojo.po.auth

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName

/*
默认的初始数据
1   character   角色管理    角色管理
11   character_add    新增角色    新增角色   1
12   character_edit    编辑角色    编辑角色   1
13   character_delete    删除角色    删除角色   1
2   department   部门管理    部门管理
21   department_add    新增部门    新增部门   2
22   department_edit    编辑部门    编辑部门   2
23   department_delete    删除部门    删除部门   2
3   employee   员工管理    员工管理
31   employee_add    新增员工    新增员工   3
32   employee_edit    编辑员工    编辑员工   3
33   employee_delete    删除员工    删除员工   3
 */
@TableName("data_permission")
data class Permission(
    @TableId("id")
    val id: String? = null,

    @TableField("code")
    val code: String,

    @TableField("name")
    val name: String,

    @TableField("description")
    val description: String? = null,

    @TableField("parent_id")
    val parentId: String? = null
)