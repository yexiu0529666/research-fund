<template>
  <div class="app-wrapper">
    <!-- 侧边菜单 -->
    <div class="sidebar-container" :style="{ width: isCollapse ? '64px' : '210px' }">
      <div class="logo-container">
        <img src="@/assets/logo.svg" alt="Logo" class="logo-img" v-if="!isCollapse">
        <img src="@/assets/logo.svg" alt="Logo" class="logo-small" v-else>
      </div>
      
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :unique-opened="true"
          :collapse-transition="false"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          class="sidebar-menu"
        >
          <el-sub-menu index="1">
            <template #title>
              <el-icon><HomeFilled /></el-icon>
              <span>首页概览</span>
            </template>
            <el-menu-item index="1-1" @click="handleMenuClick('/dashboard')">
              工作台
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 项目管理：对所有用户可见 -->
          <el-sub-menu index="2">
            <template #title>
              <el-icon><Briefcase /></el-icon>
              <span>项目管理</span>
            </template>
            <el-menu-item index="2-1" @click="handleMenuClick('/project/list')">
              项目列表
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 管理员可见菜单 -->
          <template v-if="isAdmin">
            <el-sub-menu index="3">
              <template #title>
                <el-icon><List /></el-icon>
                <span>任务管理</span>
              </template>
              <el-menu-item index="3-1" @click="handleMenuClick('/task/list')">
                任务列表
              </el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu index="4">
              <template #title>
                <el-icon><Trophy /></el-icon>
                <span>奖励管理</span>
              </template>
              <el-menu-item index="4-1" @click="handleMenuClick('/achievement/list')">
                奖励列表
              </el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu index="7">
              <template #title>
                <el-icon><User /></el-icon>
                <span>系统管理</span>
              </template>
              <el-menu-item index="7-1" @click="handleMenuClick('/system/user')">
                用户管理
              </el-menu-item>
            </el-sub-menu>
          </template>
          
          <!-- 科研人员可见菜单 -->
          <template v-if="!isAdmin">
            <el-sub-menu index="5">
              <template #title>
                <el-icon><Money /></el-icon>
                <span>经费管理</span>
              </template>
              <el-menu-item index="5-1" @click="handleMenuClick('/expense/apply/list')">
                经费申请列表
              </el-menu-item>
              <el-menu-item index="5-3" @click="handleMenuClick('/expense/transfer/list')">
                经费结转列表
              </el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu index="6">
              <template #title>
                <el-icon><Collection /></el-icon>
                <span>成果管理</span>
              </template>
              <el-menu-item index="6-1" @click="handleMenuClick('/achievement/list')">
                成果列表
              </el-menu-item>
            </el-sub-menu>
          </template>
          
          <!-- 所有用户可见 - 只显示个人信息 -->
          <el-sub-menu index="8">
            <template #title>
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </template>
            <el-menu-item index="8-1" @click="handleMenuClick('/profile')">
              个人信息
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </div>
    
    <!-- 主内容区域 -->
    <div class="main-container" :style="{ marginLeft: isCollapse ? '64px' : '210px' }">
      <!-- 顶部导航 -->
      <div class="navbar">
        <div class="left-area">
          <el-icon class="fold-icon" @click="toggleSidebar">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <breadcrumb />
        </div>
        
        <div class="right-menu">
          <el-tooltip content="全屏" placement="bottom">
            <el-icon class="right-menu-icon" @click="toggleFullScreen">
              <FullScreen />
            </el-icon>
          </el-tooltip>
          
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="avatar-container">
              <span class="user-name">{{ userInfo.name }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 页面内容 -->
      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watchEffect } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  HomeFilled, 
  Money, 
  Briefcase, 
  List, 
  Trophy, 
  Collection, 
  User, 
  Expand, 
  Fold, 
  FullScreen, 
  ArrowDown 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import Breadcrumb from './components/Breadcrumb.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 计算用户是否为管理员
const isAdmin = computed(() => {
  return userStore.isAdmin
})

// 计算用户是否为科研人员
const isResearcher = computed(() => {
  return userStore.roles.includes('ROLE_RESEARCHER')
})

// 当前用户信息
const userInfo = computed(() => {
  return {
    name: userStore.name || '用户'
  }
})

// 当前激活的菜单
const activeMenu = computed(() => {
  const { path } = route
  return path
})

// 监控用户角色信息
watchEffect(() => {
  console.log('当前用户角色状态:', userStore.roles)
  console.log('是否为管理员:', userStore.isAdmin)
})

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 切换全屏
const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
    }
  }
}

// 处理菜单点击
const handleMenuClick = (path) => {
  console.log('点击菜单，路径:', path)
  
  if (path === '/expense/transfer/list') {
    console.log('点击了经费结转列表菜单项，将导航到:', path)
  }
  
  // 使用replace模式可以避免回退问题
  router.push({
    path: path,
    replace: true
  }).then(() => {
    console.log('路由跳转成功, 当前路径:', router.currentRoute.value.path)
  }).catch(err => {
    console.error('路由跳转失败:', err)
    ElMessage.error('页面跳转失败: ' + err.message)
  })
}

// 处理用户下拉菜单命令
const handleUserCommand = (command) => {
  if (command === 'password') {
    router.push('/change-password')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确认退出登录?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout().then(() => {
        ElMessage.success('退出登录成功')
        router.push('/login')
      })
    }).catch(() => {})
  }
}

// 页面加载时检查用户登录状态
onMounted(() => {
  if (!userStore.token) {
    router.push('/login')
  }
})
</script>

<style scoped>
.app-wrapper {
  position: relative;
  height: 100vh;
  width: 100%;
}

.sidebar-container {
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 1001;
  height: 100%;
  background-color: #304156;
  transition: width 0.28s;
  overflow: hidden;
}

.logo-container {
  height: 60px;
  padding: 0;
  text-align: center;
  overflow: hidden;
  background-color: #263445;
  display: flex;
  justify-content: center;
  align-items: center;
}

.logo-img {
  height: 60px;
  width: 100%;
  object-fit: cover;
}

.logo-small {
  height: 60px;
  width: 64px;
  object-fit: cover;
}

.sidebar-menu {
  border-right: none;
}

.main-container {
  min-height: 100%;
  transition: margin-left 0.28s;
  position: relative;
}

.navbar {
  height: 60px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 15px;
}

.left-area {
  display: flex;
  align-items: center;
}

.fold-icon {
  font-size: 20px;
  cursor: pointer;
  margin-right: 15px;
}

.right-menu {
  display: flex;
  align-items: center;
}

.right-menu-icon {
  cursor: pointer;
  font-size: 20px;
  padding: 0 12px;
}

.avatar-container {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-left: 10px;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.avatar-container:hover {
  background-color: #f5f7fa;
}

.user-name {
  margin: 0 5px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.app-main {
  padding: 20px;
  position: relative;
  overflow: auto;
  height: calc(100vh - 60px);
  background-color: #f0f2f5;
}

/* 页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}
</style> 