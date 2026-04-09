<template>
  <div class="login-screen">
    <section class="login-card">
      <div class="login-card__brand">
        <span class="login-card__brand-mark">台</span>
        <div class="login-card__brand-copy">
          <strong>后台管理系统</strong>
          <span>输入账号与密码后进入当前工作区</span>
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
          <span class="soft-note">联调阶段验证码仅校验非空，正式环境以后端逻辑为准</span>
        </div>

        <div class="login-form__actions">
          <el-button type="primary" class="login-submit" :loading="loading" @click="handleLogin">
            登录
          </el-button>
        </div>
      </el-form>
    </section>

    <el-dialog v-model="forgotDialogVisible" title="找回密码" width="460px" @close="resetForgotForm">
      <el-form ref="forgotFormRef" :model="forgotForm" :rules="forgotRules" label-position="top">
        <el-form-item label="账号" prop="username">
          <el-input v-model="forgotForm.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="forgotForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="验证码" prop="verificationCode">
          <el-input v-model="forgotForm.verificationCode" placeholder="联调阶段传任意非空值即可" />
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
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Hide, Lock, User, View } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { forgotPasswordApi, loginApi } from '../api/auth'
import { hasHandledGlobalError } from '../stores/globalError'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const forgotFormRef = ref<FormInstance>()
const loading = ref(false)
const forgotSubmitting = ref(false)
const loginPasswordVisible = ref(false)
const forgotDialogVisible = ref(false)

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
  verificationCode: '',
})

const forgotRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  verificationCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
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

const resetForgotForm = () => {
  forgotForm.username = ''
  forgotForm.newPassword = ''
  forgotForm.verificationCode = ''
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
    await forgotPasswordApi(forgotForm)
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
