//==========================> 数据配置 <==========================
/**
 * 系统中的静态数据配置
 * 包括菜单、选项、分类等写死的数据
 */

import { 
  Plus, Clock, Collection, OfficeBuilding, Box,
  CoffeeCup, Connection, House, Money, School, User
} from '@element-plus/icons-vue'
import type {HomeMenu} from "@/Views/New/index.vue";
//==========================> 主页菜单配置 <==========================
/**
 * 主页菜单配置
 */
export const HOME_MENU_LIST: HomeMenu[] = [
  {name: '新建对话', label: 'new', icon: Plus},
  {name: '历史对话', label: 'history', icon: Clock},
  {name: '我的企业', label: 'company', icon: OfficeBuilding},
  {name: '企管工具', label: 'tools', icon: Collection},
/*  {name: '数据中心', label: 'datas', icon: Collection},*/
]

//==========================> 表单验证配置 <==========================
/**
 * 密码验证规则
 */
export const PASSWORD_RULES = {
  minLength: 6,
  maxLength: 20,
  pattern: /(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/,
  message: {
    minLength: '密码长度至少6位',
    maxLength: '密码长度不能超过20位',
    pattern: '密码必须包含大小写字母和数字'
  }
}

/**
 * 手机号验证规则
 */
export const PHONE_RULES = {
  pattern: /^1[3-9]\d{9}$/,
  message: '请输入正确的手机号'
}

//==========================> 验证码倒计时配置 <==========================
/**
 * 验证码倒计时配置
 */
export const COUNTDOWN_CONFIG = {
  totalTime: 60, // 总秒数
  interval: 1000 // 间隔毫秒数
}
