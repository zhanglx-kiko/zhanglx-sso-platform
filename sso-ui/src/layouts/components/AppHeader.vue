<template>
  <header class="app-header">
    <div class="app-header__main">
      <div class="app-header__lead">
        <button class="app-header__menu-trigger" type="button" @click="emit('toggle-menu')">
          <el-icon :size="20"><Operation /></el-icon>
        </button>

        <div class="app-header__titles">
          <h1 class="app-header__title">{{ pageTitle }}</h1>
        </div>
      </div>

      <div class="app-header__actions">
        <div class="app-header__date">
          <span class="app-header__date-label">今日</span>
          <strong>{{ currentDateLabel }}</strong>
        </div>

        <el-tooltip :content="fullscreenActionLabel" placement="bottom">
          <button class="app-header__icon-button" type="button" @click="toggleFullscreen">
            <el-icon :size="18">
              <ScaleToOriginal v-if="isFullscreen" />
              <FullScreen v-else />
            </el-icon>
          </button>
        </el-tooltip>

        <el-dropdown trigger="click" @command="handleCommand">
          <button class="app-header__user" type="button">
            <el-avatar :size="36" :src="userInfo?.avatar" class="app-header__avatar">
              <el-icon :size="16"><UserFilled /></el-icon>
            </el-avatar>
            <div class="app-header__user-copy">
              <strong :title="resolvedUserName">{{ displayUserName }}</strong>
            </div>
            <el-icon class="app-header__dropdown-icon"><ArrowDown /></el-icon>
          </button>

          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item command="password">
                <el-icon><Lock /></el-icon>
                修改密码
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="app-header__sub">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path || item.title">
          <span v-if="!item.path || item.path === route.path">{{ item.title }}</span>
          <router-link v-else :to="item.path">{{ item.title }}</router-link>
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
  </header>

  <el-dialog
    v-model="passwordDialogVisible"
    title="修改密码"
    width="460px"
    class="password-dialog"
    @close="resetPasswordForm"
  >
    <div class="password-dialog__intro">
      <strong>当前账号：{{ resolvedUserName }}</strong>
      <span>输入旧密码并确认新密码后即可完成修改。</span>
    </div>

    <el-form
      ref="passwordFormRef"
      :model="passwordForm"
      :rules="passwordRules"
      label-position="top"
      class="password-dialog__form"
      @keyup.enter="submitPasswordForm"
    >
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input
          v-model="passwordForm.oldPassword"
          :type="passwordVisibility.oldPassword ? 'text' : 'password'"
          placeholder="请输入旧密码"
          autocomplete="current-password"
          @blur="hidePasswordField('oldPassword')"
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
          <template #suffix>
            <button
              class="password-toggle"
              type="button"
              :aria-label="passwordVisibility.oldPassword ? '隐藏旧密码' : '显示旧密码'"
              @mousedown.prevent
              @click="togglePasswordField('oldPassword')"
            >
              <el-icon>
                <Hide v-if="passwordVisibility.oldPassword" />
                <View v-else />
              </el-icon>
            </button>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="passwordForm.newPassword"
          :type="passwordVisibility.newPassword ? 'text' : 'password'"
          placeholder="请输入新密码"
          autocomplete="new-password"
          @blur="hidePasswordField('newPassword')"
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
          <template #suffix>
            <button
              class="password-toggle"
              type="button"
              :aria-label="passwordVisibility.newPassword ? '隐藏新密码' : '显示新密码'"
              @mousedown.prevent
              @click="togglePasswordField('newPassword')"
            >
              <el-icon>
                <Hide v-if="passwordVisibility.newPassword" />
                <View v-else />
              </el-icon>
            </button>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input
          v-model="passwordForm.confirmPassword"
          :type="passwordVisibility.confirmPassword ? 'text' : 'password'"
          placeholder="请再次输入新密码"
          autocomplete="new-password"
          @blur="hidePasswordField('confirmPassword')"
        >
          <template #prefix>
            <el-icon><Lock /></el-icon>
          </template>
          <template #suffix>
            <button
              class="password-toggle"
              type="button"
              :aria-label="passwordVisibility.confirmPassword ? '隐藏确认密码' : '显示确认密码'"
              @mousedown.prevent
              @click="togglePasswordField('confirmPassword')"
            >
              <el-icon>
                <Hide v-if="passwordVisibility.confirmPassword" />
                <View v-else />
              </el-icon>
            </button>
          </template>
        </el-input>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="passwordDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="passwordSubmitting" @click="submitPasswordForm">
        确认修改
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useMenuStore } from '@/stores/menu'
import { usePermissionStore } from '@/stores/permission'
import { resetRouter } from '@/router'
import { cancelAllRequests, setLoggingOut } from '@/utils/request'
import { logoutApi, updatePasswordApi } from '@/api/auth'
import { logoutAndRedirect, setLogoutInProgress } from '@/utils/auth'
import {
  ArrowDown,
  FullScreen,
  Hide,
  Lock,
  Operation,
  ScaleToOriginal,
  SwitchButton,
  User,
  UserFilled,
  View,
} from '@element-plus/icons-vue'

const emit = defineEmits<{
  (event: 'toggle-menu'): void
}>()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const menuStore = useMenuStore()
const permissionStore = usePermissionStore()

const isLoggingOut = ref(false)
const passwordDialogVisible = ref(false)
const passwordSubmitting = ref(false)
const passwordFormRef = ref<FormInstance>()
const isFullscreen = ref(Boolean(document.fullscreenElement))

const userInfo = computed(() => userStore.userInfo)
const resolvedUserName = computed(
  () => userInfo.value?.nickname || userInfo.value?.username || '管理员',
)
const pageTitle = computed(() => String(route.meta.title || '控制台'))
const currentDateLabel = computed(() =>
  new Intl.DateTimeFormat('zh-CN', {
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  }).format(new Date()),
)
const displayUserName = computed(() => truncateUserName(resolvedUserName.value))
const fullscreenActionLabel = computed(() => (isFullscreen.value ? '缩小' : '全屏'))

type PasswordFieldKey = 'oldPassword' | 'newPassword' | 'confirmPassword'

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const passwordVisibility = reactive<Record<PasswordFieldKey, boolean>>({
  oldPassword: false,
  newPassword: false,
  confirmPassword: false,
})

const validateConfirmPassword = (
  _rule: unknown,
  value: string,
  callback: (error?: Error) => void,
) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
    return
  }

  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
    return
  }

  callback()
}

const passwordRules: FormRules<typeof passwordForm> = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (!value) {
          callback(new Error('请输入新密码'))
          return
        }

        if (value === passwordForm.oldPassword) {
          callback(new Error('新密码不能与旧密码相同'))
          return
        }

        callback()
      },
      trigger: 'blur',
    },
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
}

const breadcrumbs = computed(() => {
  const matchedItems = route.matched
    .filter((item) => item.meta?.title)
    .map((item, index, list) => ({
      title: String(item.meta.title),
      path: index === list.length - 1 ? '' : item.path,
    }))

  if (matchedItems.length > 1) {
    return matchedItems
  }

  return menuStore.breadcrumbs.map((item, index, list) => ({
    title: item.title,
    path: index === list.length - 1 ? '' : item.path,
  }))
})

const syncFullscreenState = () => {
  isFullscreen.value = Boolean(document.fullscreenElement)
}

const toggleFullscreen = async () => {
  try {
    if (!document.fullscreenElement) {
      await document.documentElement.requestFullscreen()
    } else {
      await document.exitFullscreen()
    }
  } catch {
    ElMessage.warning('当前环境暂不支持全屏切换')
  } finally {
    syncFullscreenState()
  }
}

const truncateUserName = (value: string, maxLength = 12): string => {
  const chars = Array.from(value)
  return chars.length > maxLength ? `${chars.slice(0, maxLength).join('')}...` : value
}

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordVisibility.oldPassword = false
  passwordVisibility.newPassword = false
  passwordVisibility.confirmPassword = false
  passwordFormRef.value?.clearValidate()
}

const togglePasswordField = (field: PasswordFieldKey) => {
  passwordVisibility[field] = !passwordVisibility[field]
}

const hidePasswordField = (field: PasswordFieldKey) => {
  passwordVisibility[field] = false
}

const openPasswordDialog = () => {
  resetPasswordForm()
  passwordDialogVisible.value = true
}

const submitPasswordForm = async () => {
  if (!passwordFormRef.value || passwordSubmitting.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    passwordSubmitting.value = true

    try {
      await updatePasswordApi({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
      })

      passwordDialogVisible.value = false
      resetPasswordForm()
      ElMessage.success('密码已修改，请重新登录')
      await logoutAndRedirect({
        redirect: false,
        skipMessage: true,
      })
      return
    } finally {
      passwordSubmitting.value = false
    }
  })
}

const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人中心功能即将开放')
      break
    case 'password':
      openPasswordDialog()
      break
    case 'logout':
      if (isLoggingOut.value) return

      try {
        await ElMessageBox.confirm('确认退出当前账号吗？', '退出登录', {
          confirmButtonText: '退出',
          cancelButtonText: '取消',
          type: 'warning',
        })

        isLoggingOut.value = true
        setLogoutInProgress(true)
        setLoggingOut(true)
        cancelAllRequests()
        try {
          await logoutApi()
        } catch {
          // ignore API errors on logout
        }
        ElMessage.success('已安全退出登录')
        await logoutAndRedirect({
          redirect: false,
          skipMessage: true,
        })
        return
      } catch {
        // cancelled by user
      } finally {
        isLoggingOut.value = false
        setLogoutInProgress(false)
        setLoggingOut(false)
      }
      break
  }
}

onMounted(() => {
  syncFullscreenState()
  document.addEventListener('fullscreenchange', syncFullscreenState)
})

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', syncFullscreenState)
})
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 12px;
  z-index: 20;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 11px 14px 10px;
  border: 1px solid var(--app-border);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: var(--app-shadow-soft);
  backdrop-filter: blur(6px);
}

.app-header__main,
.app-header__sub {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.app-header__lead,
.app-header__actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.app-header__lead {
  flex: 1;
  min-width: 0;
}

.app-header__main {
  min-height: 48px;
}

.app-header__titles {
  display: flex;
  min-width: 0;
  align-items: center;
  min-height: 36px;
}

.app-header__actions {
  min-width: 0;
  flex-shrink: 0;
  justify-content: flex-end;
  padding-top: 4px;
  transform: translateY(5px);
}

.app-header__title {
  margin: 0;
  color: var(--app-title);
  font-size: clamp(20px, 2vw, 24px);
  font-weight: 600;
  line-height: 1.14;
  letter-spacing: -0.03em;
}

.app-header__date {
  display: flex;
  min-height: 34px;
  min-width: 112px;
  flex-direction: column;
  justify-content: center;
  padding: 0 9px;
  border: 1px solid var(--app-border);
  border-radius: 12px;
  background: var(--app-surface-muted);
  color: var(--app-muted);
  text-align: left;
}

.app-header__date-label {
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.app-header__date strong {
  margin-top: 1px;
  color: var(--app-title);
  font-size: 12px;
  font-weight: 600;
}

.app-header__icon-button,
.app-header__menu-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 1px solid var(--app-border);
  border-radius: 50%;
  background: var(--app-surface);
  color: var(--app-title);
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease;
}

.app-header__icon-button:hover,
.app-header__menu-trigger:hover {
  border-color: #c6d4e5;
  background: #f1f5fb;
}

.app-header__menu-trigger {
  display: none;
}

.app-header__user {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 4px;
  border: 1px solid var(--app-border);
  border-radius: 999px;
  background: var(--app-surface);
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease;
}

.app-header__user:hover {
  border-color: #c6d4e5;
  background: #f8fafd;
}

.app-header__avatar {
  border: 1px solid #d2e3fc;
  background: #e8f0fe;
  color: var(--app-accent-strong);
}

.app-header__user-copy {
  min-width: 0;
  padding-right: 2px;
  text-align: left;
}

.app-header__user-copy strong {
  display: block;
  min-width: 0;
  max-width: 168px;
  overflow: hidden;
  color: var(--app-title);
  font-size: 13px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-header__dropdown-icon {
  color: var(--app-muted);
}

.app-header__sub {
  min-height: 22px;
  padding-top: 1px;
}

.password-dialog__intro {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 16px;
  padding: 12px 14px;
  border: 1px solid rgba(26, 115, 232, 0.12);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(232, 240, 254, 0.72) 0%, rgba(248, 250, 253, 0.92) 100%);
}

.password-dialog__intro strong {
  color: var(--app-title);
  font-size: 13px;
  font-weight: 600;
}

.password-dialog__intro span {
  color: var(--app-muted);
  font-size: 12px;
  line-height: 1.6;
}

.password-dialog__form :deep(.el-form-item) {
  margin-bottom: 14px;
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

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 1024px) {
  .app-header {
    top: 10px;
  }

  .app-header__menu-trigger {
    display: inline-flex;
  }

  .app-header__date {
    display: none;
  }
}

@media (max-width: 768px) {
  .app-header {
    gap: 8px;
    padding: 10px 12px;
    border-radius: 16px;
  }

  .app-header__main,
  .app-header__sub {
    flex-direction: column;
    align-items: stretch;
  }

  .app-header__actions {
    padding-top: 0;
    transform: none;
    justify-content: space-between;
  }

  .app-header__user {
    flex: 1;
    justify-content: space-between;
  }
}
</style>

