<script setup>
import { useLayout } from '@/layout/composables/layout';
import { useUserStore } from '@/store/userStore';
import { useRouter } from 'vue-router';

const { onMenuToggle, toggleDarkMode, isDarkTheme } = useLayout();
const userStore = useUserStore();
const router = useRouter();

const logout = () => {
    userStore.logout();
    router.push('/');
};

const handleProfileClick = () => {
    if (userStore.isLoggedIn) {
        router.push('/user/profile');
    } else {
        router.push('/auth/login');
    }
};

const handleLoginClick = () => {
    router.push('/auth/login');
};
</script>

<template>
    <div class="layout-topbar">
        <div class="layout-topbar-logo-container">
            <button class="layout-menu-button layout-topbar-action" @click="onMenuToggle">
                <i class="pi pi-bars"></i>
            </button>
            <router-link to="/" class="layout-topbar-logo">
                <img src="@/assets/lickerz_logo.png" class="w-45 h-14 p-1" alt="Logo" />
            </router-link>
        </div>

        <div class="layout-topbar-actions">
            <div class="layout-config-menu">
            </div>
            <button
                class="layout-topbar-menu-button layout-topbar-action"
                v-styleclass="{ selector: '@next', enterFromClass: 'hidden', enterActiveClass: 'animate-scalein', leaveToClass: 'hidden', leaveActiveClass: 'animate-fadeout', hideOnOutsideClick: true }"
            >
                <i class="pi pi-ellipsis-v"></i>
            </button>

            <div class="layout-topbar-menu hidden lg:flex">
                <div class="layout-topbar-menu-content">
                    <button type="button" class="layout-topbar-action" @click="handleProfileClick">
                        <i class="pi pi-user"></i>
                        <span>{{ userStore.isLoggedIn ? userStore.username : 'Profile' }}</span>
                    </button>
                    <button v-if="!userStore.isLoggedIn" type="button" class="layout-topbar-action" @click="handleLoginClick">
                        <i class="pi pi-sign-in"></i>
                        <span>로그인</span>
                    </button>
                </div>
            </div>
        </div>

        <div class="layout-topbar-right">
            <span v-if="userStore.isLoggedIn" class="user-info" @click="handleProfileClick">{{ userStore.username }}님 환영합니다.</span>
            <button v-if="userStore.isLoggedIn" class="p-button p-button-text p-button-plain" @click="logout">
                <i class="pi pi-power-off"></i>
                <span>로그아웃</span>
            </button>
            <button v-else class="p-button p-button-text p-button-plain" @click="handleLoginClick">
                <span class="text-l font-normal">로그인</span>
            </button>
        </div>
    </div>
</template>

<style lang="scss" scoped>
.layout-topbar-right {
    display: flex;
    align-items: center;
    
    .user-info {
        margin-right: 1rem;
        cursor: pointer;
        &:hover {
            text-decoration: underline;
        }
    }
}

.layout-topbar-menu-content {
    display: flex;
    gap: 1rem;
}
</style>
