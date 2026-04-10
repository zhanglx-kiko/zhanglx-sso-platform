<template>
  <div class="login-screen">
    <section class="login-card">
      <div class="login-card__brand">
        <span class="login-card__brand-mark">台</span>
        <div class="login-card__brand-copy">
          <strong>后台管理系统</strong>
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
          <el-input v-model="form.username" size="large" placeholder="账号">
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
          <el-button link @click="openForgotDialog">忘记密码？</el-button>
        </div>

        <div class="login-form__actions">
          <el-button type="primary" class="login-submit" :loading="loading" @click="handleLogin">
            登录
          </el-button>
        </div>
      </el-form>
    </section>

    <el-dialog
      v-model="forgotVerifyDialogVisible"
      title="找回密码"
      width="460px"
      @closed="handleForgotDialogClosed"
    >
      <div class="forgot-panel">
        <p class="forgot-step-tip">验证码会发送到账号绑定手机号</p>

        <el-form
          ref="forgotVerifyFormRef"
          :model="forgotVerifyForm"
          :rules="forgotVerifyRules"
          label-position="top"
        >
          <el-form-item label="账号" prop="username">
            <el-input v-model="forgotVerifyForm.username" placeholder="请输入账号" />
          </el-form-item>

          <el-form-item label="短信验证码" prop="verificationCode">
            <el-input v-model="forgotVerifyForm.verificationCode" maxlength="6" placeholder="请输入 6 位验证码">
              <template #append>
                <el-button
                  class="send-code-button"
                  :disabled="forgotCodeSending || forgotCodeCountdown > 0"
                  :loading="forgotCodeSending"
                  @click="sendForgotCode"
                >
                  {{ forgotSendButtonText }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
        </el-form>

        <p v-if="forgotMaskedPhoneNumber" class="forgot-send-tip">
          验证码已发送至 {{ forgotMaskedPhoneNumber }}
        </p>
      </div>

      <template #footer>
        <el-button @click="closeForgotFlow">取消</el-button>
        <el-button type="primary" :loading="forgotVerifying" @click="verifyForgotCodeAndContinue">
          下一步
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="forgotResetDialogVisible"
      title="设置新密码"
      width="460px"
      @closed="handleForgotDialogClosed"
    >
      <div class="forgot-panel">
        <p class="forgot-step-tip">验证码校验成功，请设置新密码</p>
        <p class="forgot-send-tip">当前账号：{{ forgotFlowUsername }}</p>

        <el-form
          ref="forgotResetFormRef"
          :model="forgotResetForm"
          :rules="forgotResetRules"
          label-position="top"
        >
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="forgotResetForm.newPassword"
              type="password"
              show-password
              placeholder="请输入新密码"
            />
          </el-form-item>

          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="forgotResetForm.confirmPassword"
              type="password"
              show-password
              placeholder="请再次输入新密码"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="closeForgotFlow">取消</el-button>
        <el-button type="primary" :loading="forgotSubmitting" @click="submitForgotForm">
          确认重置
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
import {
  forgotPasswordApi,
  loginApi,
  sendForgotPasswordVerificationCodeApi,
  verifyForgotPasswordVerificationCodeApi,
} from '../api/auth'
import { hasHandledGlobalError } from '../stores/globalError'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const forgotVerifyFormRef = ref<FormInstance>()
const forgotResetFormRef = ref<FormInstance>()
const loading = ref(false)
const forgotSubmitting = ref(false)
const forgotVerifying = ref(false)
const forgotCodeSending = ref(false)
const forgotCodeCountdown = ref(0)
const forgotResendIntervalSeconds = ref(60)
const forgotHasSentCode = ref(false)
const forgotMaskedPhoneNumber = ref('')
const forgotFlowUsername = ref('')
const forgotFlowVerificationCode = ref('')
const loginPasswordVisible = ref(false)
const forgotVerifyDialogVisible = ref(false)
const forgotResetDialogVisible = ref(false)
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

const forgotVerifyForm = reactive({
  username: '',
  verificationCode: '',
})

const forgotResetForm = reactive({
  newPassword: '',
  confirmPassword: '',
})

const forgotVerifyRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    {
      pattern: /^\d{6}$/,
      message: '验证码必须为 6 位数字',
      trigger: 'blur',
    },
  ],
}

const forgotResetRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '新密码长度需在 6 到 32 个字符之间', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/,
      message: '新密码必须同时包含字母和数字',
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

        if (value !== forgotResetForm.newPassword) {
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

const forgotSendButtonText = computed(() => {
  if (forgotCodeCountdown.value > 0) {
    return `${forgotCodeCountdown.value} 秒后重发`
  }
  return forgotHasSentCode.value ? '重新发送验证码' : '发送验证码'
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

const openForgotDialog = () => {
  resetForgotForm()
  forgotVerifyDialogVisible.value = true
}

const clearForgotCountdown = () => {
  if (forgotCountdownTimer !== null) {
    window.clearInterval(forgotCountdownTimer)
    forgotCountdownTimer = null
  }
}

const startForgotCountdown = (seconds: number) => {
  clearForgotCountdown()
  forgotResendIntervalSeconds.value = Math.max(1, seconds)
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

  if (!forgotVerifyForm.username.trim()) {
    ElMessage.warning('请输入账号后再发送验证码')
    return
  }

  try {
    await forgotVerifyFormRef.value?.validateField('username')
  } catch {
    return
  }

  forgotCodeSending.value = true

  try {
    const data = await sendForgotPasswordVerificationCodeApi({
      username: forgotVerifyForm.username.trim(),
    })
    forgotVerifyForm.verificationCode = ''
    forgotHasSentCode.value = true
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

const verifyForgotCodeAndContinue = async () => {
  if (!forgotVerifyFormRef.value || forgotVerifying.value) return

  try {
    await forgotVerifyFormRef.value.validate()
  } catch {
    return
  }

  forgotVerifying.value = true

  try {
    await verifyForgotPasswordVerificationCodeApi({
      username: forgotVerifyForm.username.trim(),
      verificationCode: forgotVerifyForm.verificationCode.trim(),
    })
    forgotFlowUsername.value = forgotVerifyForm.username.trim()
    forgotFlowVerificationCode.value = forgotVerifyForm.verificationCode.trim()
    forgotVerifyDialogVisible.value = false
    forgotResetDialogVisible.value = true
    forgotResetFormRef.value?.clearValidate()
    ElMessage.success('验证码校验通过')
  } catch (error) {
    if (!hasHandledGlobalError(error)) {
      ElMessage.error(error instanceof Error ? error.message : '验证码校验失败')
    }
  } finally {
    forgotVerifying.value = false
  }
}

const closeForgotFlow = () => {
  forgotVerifyDialogVisible.value = false
  forgotResetDialogVisible.value = false
  resetForgotForm()
}

const handleForgotDialogClosed = () => {
  if (!forgotVerifyDialogVisible.value && !forgotResetDialogVisible.value) {
    resetForgotForm()
  }
}

const resetForgotForm = () => {
  clearForgotCountdown()
  forgotVerifyForm.username = ''
  forgotVerifyForm.verificationCode = ''
  forgotResetForm.newPassword = ''
  forgotResetForm.confirmPassword = ''
  forgotCodeCountdown.value = 0
  forgotResendIntervalSeconds.value = 60
  forgotHasSentCode.value = false
  forgotMaskedPhoneNumber.value = ''
  forgotFlowUsername.value = ''
  forgotFlowVerificationCode.value = ''
  forgotVerifyFormRef.value?.clearValidate()
  forgotResetFormRef.value?.clearValidate()
}

const submitForgotForm = async () => {
  if (!forgotResetFormRef.value || forgotSubmitting.value) return

  try {
    await forgotResetFormRef.value.validate()
  } catch {
    return
  }

  forgotSubmitting.value = true

  try {
    await forgotPasswordApi({
      username: forgotFlowUsername.value,
      newPassword: forgotResetForm.newPassword,
      verificationCode: forgotFlowVerificationCode.value,
    })
    ElMessage.success('密码重置成功，请使用新密码登录')
    closeForgotFlow()
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
  justify-content: flex-start;
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

.forgot-panel {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.forgot-step-tip {
  margin: 0 0 12px;
  color: var(--app-muted);
  font-size: 13px;
  line-height: 1.6;
}

.forgot-send-tip {
  margin: 0 0 12px;
  color: var(--app-muted);
  font-size: 12px;
  line-height: 1.5;
}

.send-code-button {
  min-width: 132px;
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
    align-items: flex-start;
  }

  .login-submit {
    width: 100%;
  }
}
</style>
