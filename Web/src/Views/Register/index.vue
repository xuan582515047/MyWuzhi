<script setup lang="ts">
import {ref, reactive} from "vue";
import {ElMessage} from "element-plus";
import {useRouter} from "vue-router";
import { PASSWORD_RULES, PHONE_RULES, COUNTDOWN_CONFIG } from "@/Config/DataConfig.ts";
import {AuthApi} from "@/Api/AuthApi.ts";
import type {RegisterRequest} from "@/Entity/AuthEntity.ts";

//==========================> 变量定义 <==========================
const router = useRouter();
const loading = ref<boolean>(false);
const countdown = ref<number>(0);

// 注册表单数据
const registerForm = reactive({
  phone: '',
  captcha: '',
  password: '',
  confirmPassword: '',
  agreement: false
});

//==========================> 方法定义 <==========================
/**
 * 手机验证码发送
 */
const sendVerificationCode = async () => {
  // 格式校验
  if (!registerForm.phone) {
    ElMessage.warning("请输入手机号");
    return;
  }
  if (!PHONE_RULES.pattern.test(registerForm.phone)) {
    ElMessage.warning(PHONE_RULES.message);
    return;
  }

  AuthApi.sendCaptcha({ captcha: registerForm.phone });
  ElMessage.success("验证码已发送");
};

/**
 * 密码强度验证
 */
const validatePassword = (password: string) => {
  if (password.length < PASSWORD_RULES.minLength) {
    return PASSWORD_RULES.message.minLength;
  }
  if (password.length > PASSWORD_RULES.maxLength) {
    return PASSWORD_RULES.message.maxLength;
  }
  if (!PASSWORD_RULES.pattern.test(password)) {
    return PASSWORD_RULES.message.pattern;
  }
  return null;
};

/**
 * 手机注册处理
 */
const handleRegister = async () => {
  // 格式校验
  if (!registerForm.phone || !registerForm.captcha) {
    ElMessage.warning("请输入手机号和验证码");
    return;
  }
  if (!registerForm.password) {
    ElMessage.warning("请输入密码");
    return;
  }
  const pwdError = validatePassword(registerForm.password);
  if (pwdError) {
    ElMessage.warning(pwdError);
    return;
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.warning("两次输入的密码不一致");
    return;
  }
  if (!registerForm.agreement) {
    ElMessage.warning("请阅读并同意用户协议");
    return;
  }

  // 发送请求
  loading.value = true;
  const registerRequest: RegisterRequest = {
    phone: registerForm.phone,
    password: registerForm.password,
    captcha: registerForm.captcha
  };
  await AuthApi.register(registerRequest);

  // 页面跳转
  ElMessage.success("注册成功！");
  loading.value = false;
  router.push('/login');

};
</script>

<template>
  <div class="register-container">
    <div class="register-layout">
      <!-- 注册表单区域 -->
      <div class="register-card">
        <div class="register-form">
          <div class="form-header">
            <h3>手机号注册</h3>
            <p>使用手机号快速注册账号</p>
          </div>

          <div class="account-register">
            <el-input
                v-model="registerForm.phone"
                placeholder="请输入手机号"
                size="large"
                maxlength="11"
                class="input-field"
            />
            <div class="verification-group">
              <el-input
                  v-model="registerForm.captcha"
                  placeholder="请输入验证码"
                  size="large"
                  class="verification-input"
              />
              <el-button
                  class="verification-btn"
                  :disabled="countdown > 0"
                  @click="sendVerificationCode"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </div>
            <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请设置密码（6-20位字符）"
                size="large"
                show-password
                class="input-field"
            />
            <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请确认密码"
                size="large"
                show-password
                class="input-field"
                @keyup.enter="handleRegister"
            />
            <div class="register-options">
              <el-checkbox v-model="registerForm.agreement">
                我已阅读并同意
                <a href="#" class="agreement-link">《用户协议》</a>
                和
                <a href="#" class="agreement-link">《隐私政策》</a>
              </el-checkbox>
            </div>
            <el-button
                class="register-btn"
                type="primary"
                size="large"
                :loading="loading"
                @click="handleRegister"
            >
              立即注册
            </el-button>
          </div>

          <!-- 登录链接 -->
          <div class="login-link">
            <span>已有账号？</span>
            <router-link to="/login">立即登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--gradient-bg);
  padding: 20px;
  position: relative;
  overflow: hidden;
  animation: fadeIn 0.6s ease-out;
}

/* 整体布局 */
.register-layout {
  display: flex;
  width: 100%;
  max-width: 500px;
  height: auto;
  background: var(--card-bg);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: var(--shadow-large);
  border: 1px solid var(--border-light);
  overflow: hidden;
}

/* 注册卡片区域 */
.register-card {
  width: 100%;
  padding: 40px;
  display: flex;
  flex-direction: column;
  background: var(--card-bg);
}

.register-form {
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

.account-register {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.input-field {
  margin-bottom: 20px;
}

:deep(.el-input__wrapper) {
  background: var(--input-bg);
  border: 1px solid var(--border-light);
  border-radius: 12px;
  box-shadow: var(--shadow-light);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: var(--border-hover-color);
  box-shadow: var(--shadow-medium);
}

:deep(.el-input__wrapper.is-focused) {
  border-color: var(--primary-color);
  box-shadow: var(--focus-ring);
}

.verification-group {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.verification-input {
  flex: 1;
}

.verification-btn {
  height: 50px;
  width: 140px;
  background: var(--primary-gradient);
  border: none;
  color: var(--btn-primary-text);
  border-radius: 12px;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.verification-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-hover);
}

.register-options {
  display: flex;
  align-items: center;
  margin-bottom: 25px;
  font-size: 14px;
}

.agreement-link {
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.3s ease;
}

.agreement-link:hover {
  color: var(--link-hover);
  text-decoration: underline;
}

.register-btn {
  width: 100%;
  height: 50px;
  background: var(--primary-gradient);
  border: none;
  border-radius: 12px;
  color: var(--btn-primary-text);
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
  margin-top: 20px;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-hover);
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: var(--text-secondary);
  font-size: 14px;
}

.login-link a {
  color: var(--link-color);
  text-decoration: none;
  font-weight: 500;
  margin-left: 5px;
  transition: all 0.3s ease;
}

.login-link a:hover {
  text-decoration: underline;
}

/* 添加动画定义 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 添加微妙的背景动画 */
.register-container::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(ellipse at center, var(--bg-glow) 0%, rgba(255, 255, 255, 0) 70%);
  pointer-events: none;
  transition: all 0.5s ease;
  z-index: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-layout {
    max-width: 400px;
  }

  .register-card {
    padding: 30px 25px;
  }
}
</style>