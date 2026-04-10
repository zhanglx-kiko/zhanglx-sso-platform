<template>
  <div class="login-screen">
    <section class="login-card">
      <div class="login-card__brand">
        <span class="login-card__brand-mark">台</span>
        <div class="login-card__brand-copy">
          <strong>后台管理系统</strong>
          <span>输入账号和密码后进入当前工作区</span>
        </div>
      </div>

      <div class="login-card__header">
        <h2 class="login-card__title">账号登录</h2>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input v-model="form.username" size="large" placeholder="账号 / 邮箱">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            :type="loginPasswordVisible ? 'text' : 'password'"
            size="large"
            placeholder="输入密码"
            autocomplete="current-password"
            @blur="loginPasswordVisible = false"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
            <template #suffix>
              <button
                class="password-toggle"
                type="button"
                :aria-label="loginPasswordVisible ? '隐藏密码' : '显示密码'"
                @mousedown.prevent
                @click="loginPasswordVisible = !loginPasswordVisible"
              >
                <el-icon>
                  <Hide v-if="loginPasswordVisible" />
                  <View v-else />
                </el-icon>
              </button>
            </template>
          </el-input>
        </el-form-item>

        <div class="login-form__meta">
          <el-button link @click="forgotDialogVisible = true">忘记密码？</el-button>
          <span class="soft-note">验证码会发送到账号绑定手机号</span>
        </div>

        <div class="login-form__actions">
          <el-button type="primary" class="login-submit" :loading="loading" @click="handleLogin">
            登录
          </el-button>
        </div>
      </el-form>
    </section>

    <el-dialog v-model="forgotDialogVisible" title="找回密码" width="460px" @close="resetForgotForm">
      <el-alert
        class="forgot-alert"
        title="验证码有效期 3 分钟，1 分钟后可重新发送；重新发送后，上一条验证码会立即失效。"
        type="info"
        :closable="false"
        show-icon
      />

      <el-form ref="forgotFormRef" :model="forgotForm" :rules="forgotRules" label-position="top">
        <el-form-item label="账号" prop="username">
          <el-input v-model="forgotForm.username" placeholder="请输入账号" />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="forgotForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>

        <el-form-item label="短信验证码" prop="verificationCode">
          <el-input v-model="forgotForm.verificationCode" maxlength="6" placeholder="请输入 6 位验证码">
            <template #append>
              <el-button
                class="send-code-button"
                :disabled="forgotCodeSending || forgotCodeCountdown > 0"
                :loading="forgotCodeSending"
                @click="sendForgotCode"
              >
                {{ forgotCodeCountdown > 0 ? `${forgotCodeCountdown} 秒` : '发送验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <p v-if="forgotMaskedPhoneNumber" class="forgot-send-tip">
          验证码已发送至 {{ forgotMaskedPhoneNumber }}，请填写收到的 6 位验证码。
        </p>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="forgotForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="forgotDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="forgotSubmitting" @click="submitForgotForm">
          提交重置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Hide, Lock, User, View } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { forgotPasswordApi, loginApi, sendForgotPasswordVerificationCodeApi } from '../api/auth'
import { hasHandledGlobalError } from '../stores/globalError'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const forgotFormRef = ref<FormInstance>()
const loading = ref(false)
const forgotSubmitting = ref(false)
const forgotCodeSending = ref(false)
const forgotCodeCountdown = ref(0)
const forgotMaskedPhoneNumber = ref('')
const loginPasswordVisible = ref(false)
const forgotDialogVisible = ref(false)
let forgotCountdownTimer: number | null = null

const form = reactive({
  username: '',
  password: '',
  device: 'PC',
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const forgotForm = reactive({
  username: '',
  newPassword: '',
  confirmPassword: '',
  verificationCode: '',
})

const forgotRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '新密码长度需在 6 到 32 个字符之间', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/,
      message: '新密码必须同时包含字母和数字',
      trigger: 'blur',
    },
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    {
      pattern: /^\d{6}$/,
      message: '验证码必须为 6 位数字',
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (!value) {
          callback(new Error('请再次输入新密码'))
          return
        }

        if (value !== forgotForm.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
          return
        }

        callback()
      },
      trigger: 'blur',
    },
  ],
}

const redirectPath = computed(() => {
  return typeof route.query.redirect === 'string' && route.query.redirect
    ? route.query.redirect
    : '/system/auth/user'
})

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true

    try {
      const data = await loginApi(form)
      userStore.setLoginState(data)
      loginPasswordVisible.value = false
      ElMessage.success('登录成功')
      router.push(redirectPath.value)
    } catch (error) {
      if (!hasHandledGlobalError(error)) {
        ElMessage.error(error instanceof Error ? error.message : '登录失败')
      }
    } finally {
      loading.value = false
    }
  })
}

const clearForgotCountdown = () => {
  if (forgotCountdownTimer !== null) {
    window.clearInterval(forgotCountdownTimer)
    forgotCountdownTimer = null
  }
}

const startForgotCountdown = (seconds: number) => {
  clearForgotCountdown()
  forgotCodeCountdown.value = Math.max(0, seconds)
  if (forgotCodeCountdown.value <= 0) return

  forgotCountdownTimer = window.setInterval(() => {
    if (forgotCodeCountdown.value <= 1) {
      forgotCodeCountdown.value = 0
      clearForgotCountdown()
      return
    }
    forgotCodeCountdown.value -= 1
  }, 1000)
}

const sendForgotCode = async () => {
  if (forgotCodeSending.value || forgotCodeCountdown.value > 0) return

  if (!forgotForm.username.trim()) {
    ElMessage.warning('请输入账号后再发送验证码')
    return
  }

  try {
    await forgotFormRef.value?.validateField('username')
  } catch {
    return
  }

  forgotCodeSending.value = true

  try {
    const data = await sendForgotPasswordVerificationCodeApi({
      username: forgotForm.username.trim(),
    })
    forgotForm.verificationCode = ''
    forgotMaskedPhoneNumber.value = data.maskedPhoneNumber
    startForgotCountdown(data.resendIntervalSeconds)
    ElMessage.success(`验证码已发送至 ${data.maskedPhoneNumber}`)
  } catch (error) {
    if (!hasHandledGlobalError(error)) {
      ElMessage.error(error instanceof Error ? error.message : '验证码发送失败')
    }
  } finally {
    forgotCodeSending.value = false
  }
}

const resetForgotForm = () => {
  clearForgotCountdown()
  forgotForm.username = ''
  forgotForm.newPassword = ''
  forgotForm.confirmPassword = ''
  forgotForm.verificationCode = ''
  forgotCodeCountdown.value = 0
  forgotMaskedPhoneNumber.value = ''
  forgotFormRef.value?.clearValidate()
}

const submitForgotForm = async () => {
  if (!forgotFormRef.value || forgotSubmitting.value) return

  try {
    await forgotFormRef.value.validate()
  } catch {
    return
  }

  forgotSubmitting.value = true

  try {
    await forgotPasswordApi({
      username: forgotForm.username,
      newPassword: forgotForm.newPassword,
      verificationCode: forgotForm.verificationCode,
    })
    forgotDialogVisible.value = false
    ElMessage.success('密码重置成功，请使用新密码登录')
    resetForgotForm()
  } catch (error) {
    if (!hasHandledGlobalError(error)) {
      ElMessage.error(error instanceof Error ? error.message : '重置失败')
    }
  } finally {
    forgotSubmitting.value = false
  }
}

onBeforeUnmount(() => {
  clearForgotCountdown()
})
</script>

<style scoped>
.login-screen {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
}

.login-card {
  width: min(420px, 100%);
  padding: 24px 24px 22px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.84) 0%, rgba(248, 250, 253, 0.8) 100%);
  box-shadow:
    0 18px 42px rgba(60, 64, 67, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.36);
  backdrop-filter: blur(14px);
}

.login-card__brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.login-card__brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 14px;
  border: 1px solid rgba(26, 115, 232, 0.14);
  background: rgba(26, 115, 232, 0.08);
  color: var(--app-accent-strong);
  font-size: 18px;
  font-weight: 600;
}

.login-card__brand-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.login-card__brand-copy strong {
  color: var(--app-title);
  font-size: 19px;
  font-weight: 600;
}

.login-card__brand-copy span {
  color: var(--app-muted);
  font-size: 12px;
}

.login-card__header {
  margin-top: 18px;
}

.login-card__title {
  margin: 0;
  color: var(--app-title);
  font-size: 26px;
  font-weight: 600;
  line-height: 1.12;
  letter-spacing: -0.04em;
}

.login-form {
  margin-top: 20px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.login-form__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 4px;
  margin-bottom: 16px;
}

.login-form__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.login-submit {
  min-width: 96px;
  min-height: 38px;
  font-size: 13px;
}

.soft-note {
  color: var(--app-muted);
  font-size: 12px;
}

.forgot-alert {
  margin-bottom: 16px;
}

.forgot-send-tip {
  margin: -8px 0 16px;
  color: var(--app-muted);
  font-size: 12px;
  line-height: 1.5;
}

.send-code-button {
  min-width: 104px;
}

.password-toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--app-muted);
  cursor: pointer;
  transition:
    color 0.2s ease,
    background-color 0.2s ease;
}

.password-toggle:hover {
  background: rgba(26, 115, 232, 0.08);
  color: var(--app-accent-strong);
}

@media (max-width: 768px) {
  .login-screen {
    padding: 12px;
  }

  .login-card {
    padding: 20px 18px;
    border-radius: 18px;
  }

  .login-card__title {
    font-size: 24px;
  }

  .login-form__meta,
  .login-form__actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .login-submit {
    width: 100%;
  }
}
</style>
