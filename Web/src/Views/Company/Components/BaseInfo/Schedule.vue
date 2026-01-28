<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Clock } from '@element-plus/icons-vue'
import { ScheduleApi } from '@/Api/company/ScheduleApi'
import type {
  ScheduleDailyGroup,
  AddScheduleRequest,
  UpdateScheduleRequest,
  ScheduleDailyItem
} from '@/Entity/company/ScheduleEntity'

// Props
const props = defineProps<{ companyId: string }>()

// 响应式数据
const scheduleData = ref<ScheduleDailyGroup[]>([])
const dialogVisible = ref(false)
const isEditing = ref(false)
const draggingItem = ref<{item: ScheduleDailyItem, sourceDate: string} | null>(null)
const weekDates = ref<string[]>([])

const form = reactive({
  id: '',
  title: '',
  description: '',
  time: '',
  date: ''
})

// 获取今天的日期字符串（YYYY-MM-DD）
const getTodayString = () => {
  const today = new Date()
  return today.toISOString().split('T')[0]
}

// ISO时间转换工具函数（用于提交到后端）
const toISOTime = (date: string, time: string): string => {
  return `${date}T${time}:00`
}

// 显示时转换工具函数
const getWeekday = (date: string): string => {
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return weekdays[new Date(date).getDay()]!
}

const formatTime = (isoTime: string): string => {
  const date = new Date(isoTime)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 生成本周日期列表
const generateWeekDates = (date: Date): string[] => {
  const dates: string[] = []
  const dayOfWeek = date.getDay()
  const mondayOffset = dayOfWeek === 0 ? -6 : -(dayOfWeek - 1)
  const monday = new Date(date)
  monday.setDate(date.getDate() + mondayOffset)
  
  for (let i = 0; i < 7; i++) {
    const currentDate = new Date(monday)
    currentDate.setDate(monday.getDate() + i)
    dates.push(currentDate.toISOString().split('T')[0]!)
  }
  
  return dates
}

// 初始化数据 - 从API加载本周日程
const initData = async () => {
  const today = new Date()
  weekDates.value = generateWeekDates(today)
  
  // 先创建7个空日程列
  const emptyScheduleData: ScheduleDailyGroup[] = weekDates.value.map(date => ({
    date,
    scheduleList: []
  }))
  
  // 从后端加载数据
  const backendData = await ScheduleApi.getScheduleList({
    companyId: props.companyId,
    startDate: `${weekDates.value[0]}T00:00:00`,
    endDate: `${weekDates.value[6]}T23:59:59`
  })
  
  // 合并后端数据到空列中
  if (backendData && backendData.length > 0) {
    backendData.forEach(backendDay => {
      const targetDay = emptyScheduleData.find(day => day.date === backendDay.date)
      if (targetDay) {
        targetDay.scheduleList = backendDay.scheduleList || []
      }
    })
  }
  
  scheduleData.value = emptyScheduleData
}

// 拖拽事件处理
const onDragStart = (event: DragEvent, item: ScheduleDailyItem, sourceDate: string) => {
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
    draggingItem.value = { item, sourceDate }
  }
}

const onDragOver = (event: DragEvent) => {
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
}

const onDrop = async (event: DragEvent, targetDate: string) => {
  event.preventDefault()
  if (!draggingItem.value) return

  const { item, sourceDate } = draggingItem.value
  
  // 调用API更新
  const updateData: UpdateScheduleRequest = {
    id: item.id,
    title: item.title,
    description: item.description,
    time: toISOTime(targetDate, formatTime(item.time))
  }
  
  await ScheduleApi.updateSchedule(updateData)

  // 从源日期移除
  const sourceDay = scheduleData.value.find(d => d.date === sourceDate)
  if (sourceDay) {
    const itemIndex = sourceDay.scheduleList.findIndex((i: ScheduleDailyItem) => i.id === item.id)
    if (itemIndex > -1) {
      sourceDay.scheduleList.splice(itemIndex, 1)
    }
  }

  // 添加到目标日期
  const targetDay = scheduleData.value.find(d => d.date === targetDate)
  if (targetDay) {
    targetDay.scheduleList.push(item)
  }

  draggingItem.value = null
}

const onDragEnd = () => {
  draggingItem.value = null
}

// 日程操作
const handleAddSchedule = (date?: string) => {
  isEditing.value = false
  Object.assign(form, {
    id: '',
    title: '',
    description: '',
    time: '',
    date: date || ''
  })
  dialogVisible.value = true
}

const handleEdit = (item: ScheduleDailyItem, date: string) => {
  isEditing.value = true
  Object.assign(form, { 
    id: item.id,
    title: item.title,
    description: item.description,
    time: formatTime(item.time),
    date: date 
  })
  dialogVisible.value = true
}

const handleDelete = async (item: ScheduleDailyItem, date: string) => {
  try {
    await ElMessageBox.confirm('确定要删除这个日程吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await ScheduleApi.deleteSchedule({ id: item.id })
    
    const day = scheduleData.value.find(d => d.date === date)
    if (day) {
      const index = day.scheduleList.findIndex(i => i.id === item.id)
      if (index > -1) {
        day.scheduleList.splice(index, 1)
      }
    }

  } catch {
    // 用户取消删除
  }
}

const handleConfirm = async () => {
  if (!form.title || !form.date || !form.time) {
    ElMessage.warning('请填写完整信息')
    return
  }

  const targetDay = scheduleData.value.find(d => d.date === form.date)
  if (!targetDay) {
    ElMessage.warning('选择的日期不在当前视图内')
    return
  }

  if (isEditing.value) {
    // 编辑
    const updateData: UpdateScheduleRequest = {
      id: form.id,
      title: form.title,
      description: form.description,
      time: toISOTime(form.date, form.time)
    }
    
    // 更新本地数据 - 找到原来的项并更新
    for (const day of scheduleData.value) {
      const item = day.scheduleList.find(i => i.id === form.id)
      if (item) {
        // 如果日期改变了，需要移动到其他天
        if (day.date !== form.date) {
          const index = day.scheduleList.indexOf(item)
          day.scheduleList.splice(index, 1)
          targetDay.scheduleList.push({
            id: form.id,
            title: form.title,
            description: form.description,
            time: toISOTime(form.date, form.time)
          })
        } else {
          // 日期没变，直接更新
          item.title = form.title
          item.description = form.description
          item.time = toISOTime(form.date, form.time)
        }
        break
      }
    }
    
    // 异步调用后端，不管结果
    ScheduleApi.updateSchedule(updateData).catch(() => {})

  } else {
    // 新增 - 直接往列表里加
    const newItem: ScheduleDailyItem = {
      id: Date.now().toString(), // 用时间戳生成临时ID
      title: form.title,
      description: form.description,
      time: toISOTime(form.date, form.time)
    }
    
    targetDay.scheduleList.push(newItem)
    
    // 异步调用后端，不管结果
    const addData: AddScheduleRequest = {
      companyId: props.companyId,
      title: form.title,
      description: form.description,
      time: toISOTime(form.date, form.time)
    }
    ScheduleApi.addSchedule(addData).catch(() => {})
  }
  
  // 关闭对话框
  dialogVisible.value = false
}

onMounted(() => {
  initData()
})
</script>

<template>
  <div class="schedule-container">
    <div class="header">
      <h2>日程表</h2>
    </div>

    <div class="schedule-content">
      <div
          v-for="day in scheduleData"
          :key="day.date"
          class="day-column"
          :class="{ 'today': day.date === getTodayString() }"
          @dragover="onDragOver"
          @drop="(e) => onDrop(e, day.date)"
      >
        <div class="day-header">
          <h3>
            {{ day.date }}
            <span v-if="day.date === getTodayString()" class="today-badge">今天</span>
          </h3>
          <span class="weekday">{{ getWeekday(day.date) }}</span>
        </div>

        <el-button type="primary" @click="handleAddSchedule(day.date)" class="add-btn-column">
          <el-icon><Plus /></el-icon>
          添加日程
        </el-button>

        <div class="schedule-items">
          <div
              v-for="item in day.scheduleList"
              :key="item.id"
              class="schedule-item"
              draggable="true"
              @dragstart="(e) => onDragStart(e, item, day.date)"
              @dragend="onDragEnd"
          >
            <div class="item-content">
              <div class="item-time">
                <el-icon class="time-icon"><Clock /></el-icon>
                {{ formatTime(item.time) }}
              </div>
              <div class="item-title">{{ item.title }}</div>
              <div class="item-desc">{{ item.description }}</div>
            </div>
            <div class="item-actions">
              <el-icon @click="handleEdit(item, day.date)" class="action-icon"><Edit /></el-icon>
              <el-icon @click="handleDelete(item, day.date)" class="action-icon"><Delete /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑日程对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="isEditing ? '编辑日程' : '添加日程'"
        :show-close="false"
        width="500px"
        class="schedule-dialog"
        :append-to-body="true"
        align-center
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input
              v-model="form.title"
              placeholder="请输入日程标题"
              class="dialog-input"
          />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
              v-model="form.date"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              class="dialog-date-picker"
              :disabled="!!form.date"
          />
        </el-form-item>
        <el-form-item label="时间">
          <el-time-picker
              v-model="form.time"
              placeholder="选择时间"
              format="HH:mm"
              value-format="HH:mm"
              class="dialog-time-picker"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
              v-model="form.description"
              type="textarea"
              :rows="3"
              placeholder="请输入日程描述"
              class="dialog-textarea"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="handleConfirm" class="confirm-btn">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.schedule-container {
  width: 100%;
  padding: 20px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  box-sizing: border-box;
  font-family: 'Helvetica Neue', Arial, sans-serif;
  border-radius: 12px;
  box-shadow: var(--shadow-light);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 16px;
  animation: fadeInDown 0.6s ease;
}

.header h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 0 2px 4px rgba(124, 58, 237, 0.2);
  letter-spacing: 0.5px;
}

.add-btn {
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 600;
  background: var(--gradient-primary);
  border: none;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-light);
  color: var(--text-on-primary);
}

.add-btn:hover {
  background: var(--gradient-primary-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-medium);
}

.add-btn-column {
  width: 100%;
  margin-bottom: 16px;
  border-radius: 8px;
  padding: 8px 12px;
  font-weight: 500;
  font-size: 14px;
  background: var(--gradient-primary);
  border: none;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-light);
  color: var(--text-on-primary);
}

.add-btn-column:hover {
  background: var(--gradient-primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-medium);
}

.schedule-content {
  margin-top: 20px;
  display: flex;
  gap: 20px;
  overflow-x: auto;
  padding: 0 16px 16px;
  min-height: 70vh;
  background-color: var(--bg-primary);
  border-radius: 12px;
}

.day-column {
  margin-top: 10px;
  flex: 1;
  min-width: 280px;
  background-color: var(--card-bg);
  border: 1px solid var(--border-primary);
  border-radius: 16px;
  padding: 20px;
  box-shadow: var(--shadow-card);
  transition: all 0.3s ease;
  animation: slideInUp 0.5s ease;
}

.day-column:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
  border-color: var(--accent-primary);
}

.day-column:nth-child(1) { animation-delay: 0.1s; }
.day-column:nth-child(2) { animation-delay: 0.2s; }
.day-column:nth-child(3) { animation-delay: 0.3s; }
.day-column:nth-child(4) { animation-delay: 0.4s; }
.day-column:nth-child(5) { animation-delay: 0.5s; }
.day-column:nth-child(6) { animation-delay: 0.6s; }
.day-column:nth-child(7) { animation-delay: 0.7s; }

.day-header {
  text-align: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--accent-primary);
  background-color: var(--hover-bg);
  border-radius: 12px;
  padding: 12px;
}

.day-header h3 {
  margin: 0 0 4px 0;
  color: var(--accent-primary);
  font-size: 16px;
  font-weight: 600;
}

.weekday {
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
}

.today-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  margin-left: 8px;
  background: var(--gradient-primary);
  color: var(--text-on-primary);
  vertical-align: middle;
}

.schedule-items {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.schedule-item {
  background-color: var(--card-bg);
  border: 1px solid var(--border-primary);
  border-radius: 12px;
  padding: 16px;
  cursor: move;
  transition: all 0.3s ease;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
  overflow: hidden;
}

.schedule-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: var(--gradient-primary);
  border-radius: 2px 0 0 2px;
}

.schedule-item:hover {
  box-shadow: var(--shadow-medium);
  transform: translateY(-2px);
  border-color: var(--accent-primary);
  background-color: var(--card-hover);
}

.schedule-item.dragging {
  opacity: 0.6;
  transform: scale(0.98);
}

.item-content {
  flex: 1;
  margin-right: 12px;
}

.item-time {
  color: var(--accent-primary);
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.time-icon {
  font-size: 14px;
}

.item-title {
  font-weight: 600;
  margin-bottom: 6px;
  color: var(--text-primary);
  font-size: 15px;
  line-height: 1.4;
}

.item-desc {
  color: var(--text-tertiary);
  font-size: 13px;
  line-height: 1.4;
  opacity: 0.9;
}

.item-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: all 0.3s ease;
  transform: translateX(10px);
}

.schedule-item:hover .item-actions {
  opacity: 1;
  transform: translateX(0);
}

.action-icon {
  cursor: pointer;
  color: var(--accent-primary);
  transition: all 0.3s ease;
  padding: 4px;
  border-radius: 4px;
  background-color: var(--hover-icon-bg);
}

.action-icon:hover {
  color: var(--accent-secondary);
  background-color: var(--hover-button-bg);
  transform: scale(1.1);
}

/* 对话框样式 */
:deep(.schedule-dialog) {
  border-radius: 20px;
  overflow: hidden;
}

:deep(.schedule-dialog .el-dialog__header) {
  background: var(--gradient-primary);
  padding: 20px 24px;
  margin: 0;
  border-bottom: 1px solid var(--border-primary);
}

:deep(.schedule-dialog .el-dialog__title) {
  color: var(--text-on-primary);
  font-weight: 600;
  font-size: 18px;
  letter-spacing: 0.5px;
}

:deep(.schedule-dialog .el-dialog__body) {
  padding: 24px;
  background-color: var(--dialog-body-bg);
}

:deep(.dialog-input .el-input__inner),
:deep(.dialog-date-picker .el-input__inner),
:deep(.dialog-time-picker .el-input__inner) {
  border-radius: 8px;
  padding: 10px 12px;
  color: var(--text-primary);
  transition: all 0.3s ease;
  background-color: var(--input-bg);
  border-color: var(--border-primary);
}

:deep(.dialog-textarea .el-textarea__inner) {
  border: 1px solid var(--border-primary);
  border-radius: 8px;
  padding: 12px;
  background-color: var(--input-bg);
  color: var(--text-primary);
  transition: all 0.3s ease;
  resize: vertical;
  min-height: 80px;
}

:deep(.dialog-textarea .el-textarea__inner:focus) {
  border-color: var(--accent-primary);
  box-shadow: var(--focus-ring);
  transform: translateY(-1px);
}

:deep(.schedule-dialog .el-form-item__label) {
  color: var(--text-primary) !important;
  font-weight: 600;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid var(--dialog-footer-border);
}

.cancel-btn {
  border-radius: 8px;
  padding: 10px 20px;
  border: 1px solid var(--border-primary);
  color: var(--text-primary);
  background-color: var(--card-bg);
  transition: all 0.3s ease;
  font-weight: 500;
}

.cancel-btn:hover {
  background-color: var(--card-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-light);
  border-color: var(--accent-primary);
}

.confirm-btn {
  border-radius: 8px;
  padding: 10px 20px;
  background: var(--gradient-primary);
  border: none;
  font-weight: 500;
  transition: all 0.3s ease;
  color: var(--text-on-primary);
}

.confirm-btn:hover {
  background: var(--gradient-primary-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-medium);
}

/* 滚动条优化 */
.schedule-content::-webkit-scrollbar {
  height: 8px;
}

.schedule-content::-webkit-scrollbar-track {
  background-color: var(--scrollbar-track);
  border-radius: 4px;
}

.schedule-content::-webkit-scrollbar-thumb {
  background: var(--gradient-primary);
  border-radius: 4px;
}

.schedule-content::-webkit-scrollbar-thumb:hover {
  background: var(--gradient-primary-hover);
}

/* 动画效果 */
@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .schedule-container {
    padding: 16px;
  }

  .schedule-content {
    gap: 16px;
    padding: 0 12px 12px;
  }

  .day-column {
    min-width: 260px;
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .schedule-container {
    padding: 12px;
  }

  .header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
    text-align: center;
  }

  .header h2 {
    font-size: 24px;
  }

  .schedule-content {
    flex-direction: column;
    gap: 16px;
    overflow-x: visible;
  }

  .day-column {
    min-width: auto;
    animation: none;
  }

  .item-actions {
    opacity: 1;
    transform: translateX(0);
  }

  :deep(.schedule-dialog) {
    width: 95% !important;
    margin: 20px auto;
  }

  :deep(.schedule-dialog .el-dialog__body) {
    padding: 16px;
  }

  .dialog-footer {
    flex-direction: column;
    gap: 8px;
  }

  .cancel-btn,
  .confirm-btn {
    width: 100%;
  }
}

/* 空状态样式 */
.schedule-items:empty::after {
  content: '暂无日程安排';
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100px;
  color: var(--text-tertiary);
  font-style: italic;
  background-color: var(--hover-bg);
  border-radius: 8px;
  border: 2px dashed var(--border-primary);
}

/* 拖拽占位符效果 */
.day-column.drag-over {
  background-color: var(--hover-bg);
  border: 2px dashed var(--accent-primary);
  transform: scale(1.02);
}

/* 今日高亮效果 */
.day-column.today .day-header {
  background-color: var(--hover-bg);
  border-bottom-color: var(--accent-secondary);
}

.day-column.today .day-header h3 {
  color: var(--accent-secondary);
  text-shadow: 0 2px 4px rgba(124, 58, 237, 0.2);
}
</style>