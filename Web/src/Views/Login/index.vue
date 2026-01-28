<script setup lang="ts">
import {ref, reactive, watch} from "vue";
import {ElMessage} from "element-plus";
import {useRouter} from "vue-router";
import {useAuthStore} from "@/Stores/AuthStore.ts";
import { PHONE_RULES, PASSWORD_RULES } from "@/Config/DataConfig.ts";
import {AuthApi} from "@/Api/AuthApi.ts";
import {useUserStore} from "@/Stores/UserStore.ts";
import type {LoginRequest} from "@/Entity/AuthEntity.ts";


//==========================> 变量定义 <==========================
const loading = ref<boolean>(false);
const router = useRouter();
const authStore = useAuthStore()
const userStore = useUserStore()

// 登录表单数据
const loginForm = ref({
  phone: '',
  password: '',
  remember: false
});

//==========================> 方法定义 <==========================


/**
 * 手机号登录处理
 */
const handleLogin = async () => {
  // 格式校验
  if (!loginForm.value.phone || !loginForm.value.password) {
    ElMessage.warning("请输入手机号和密码");
    return;
  }
  if (!PHONE_RULES.pattern.test(loginForm.value.phone)) {
    ElMessage.warning(PHONE_RULES.message);
    return;
  }
  if (!PASSWORD_RULES.pattern.test(loginForm.value.password)) {
    ElMessage.warning(PASSWORD_RULES.message.pattern);
    return;
  }

  // 发送请求
  loading.value = true;
  const loginRequest: LoginRequest = {
    phone: loginForm.value.phone,
    password: loginForm.value.password
  };
  const userData = await AuthApi.login(loginRequest)
  if(userData != null){
    // 保存登录信息到 store
    authStore.saveLoginData(
        userData.accessToken,
        userData.refreshToken,
        loginForm.value.remember
    )
    userStore.saveUserData(
        userData.name,
        userData.avatar
    )

    loading.value = false;
    router.push('/new')
  }
};

</script>

<template>
  <div class="login-container">
    <div class="login-layout">
      <!-- 登录表单区域 -->
      <div class="login-card">
        <div class="login-form">
          <div class="form-header">
            <h3>手机号登录</h3>
            <p>使用手机号和密码登录系统</p>
          </div>

          <!-- 手机号密码登录表单 -->
          <div class="phone-login">
            <el-input
                v-model="loginForm.phone"
                placeholder="请输入手机号"
                size="large"
                maxlength="11"
                class="input-field"
                @keyup.enter="handleLogin"
            />
            <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
                class="input-field"
                @keyup.enter="handleLogin"
            />
            <div class="login-options">
              <el-checkbox v-model="loginForm.remember">记住登录</el-checkbox>
              <a href="#" class="forgot-link">忘记密码?</a>
            </div>
            <el-button
                class="login-btn"
                type="primary"
                size="large"
                :loading="loading"
                @click="handleLogin"
            >
              立即登录
            </el-button>
          </div>

          <!-- 注册链接 -->
          <div class="register-link">
            <span>还没有账号？</span>
            <router-link to="/register">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--gradient-bg);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 整体布局 */
.login-layout {
  display: flex;
  width: 100%;
  max-width: 500px;
  height: auto;
  background: var(--bg-secondary);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: var(--shadow-heavy);
  border: 1px solid var(--border-light);
  overflow: hidden;
  position: relative;
}

/* 暗色模式下的卡片边缘发光效果 */
.dark .login-layout {
  box-shadow: var(--shadow-heavy), var(--card-edge-glow);
}

/* 登录卡片区域 */
.login-card {
  width: 100%;
  padding: 40px;
  display: flex;
  flex-direction: column;
  background: var(--card-bg);
}

.login-form {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.form-header h3 {
  color: var(--text-primary);
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.form-header p {
  color: var(--text-secondary);
  font-size: 14px;
}

.phone-login {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.input-field {
  margin-bottom: 20px;
}

:deep(.el-input__wrapper) {
  background: var(--input-bg);
  border: 1px solid var(--border-primary);
  border-radius: 12px;
  box-shadow: var(--shadow-light);
  transition: all 0.3s ease;
}

:deep(.el-input__inner) {
  color: var(--text-primary);
}

:deep(.el-input__inner::placeholder) {
  color: var(--text-tertiary);
}

:deep(.el-input__wrapper:hover) {
  border-color: var(--border-hover-color);
  box-shadow: var(--shadow-medium);
}

:deep(.el-input__wrapper.is-focused) {
  border-color: var(--primary-color);
  box-shadow: var(--focus-ring);
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  font-size: 14px;
  color: var(--text-secondary);
}

.forgot-link {
  color: var(--text-tertiary);
  text-decoration: none;
  transition: all 0.3s ease;
}

.forgot-link:hover {
  color: var(--link-hover);
  text-decoration: underline;
}

.login-btn {
  width: 100%;
  height: 50px;
  background: var(--gradient-primary);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  margin-top: 20px;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-medium);
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: var(--text-tertiary);
  font-size: 14px;
}

.register-link a {
  color: var(--link-color);
  text-decoration: none;
  font-weight: 500;
  margin-left: 5px;
  transition: all 0.3s ease;
}

.register-link a:hover {
  color: var(--link-hover);
  text-decoration: underline;
}































































































/* 响应式设计 */
@media (max-width: 768px) {
  .login-layout {
    max-width: 400px;
  }

  .login-card {
    padding: 30px 25px;
  }
}
</style>