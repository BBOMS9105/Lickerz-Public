<script setup>
import { ref, computed } from 'vue';
import { useUserStore } from '@/store/userStore';
import { useRouter } from 'vue-router';
import AppMenuItem from './AppMenuItem.vue';

const userStore = useUserStore();
const router = useRouter(); //

const model = computed(() => [
    {
        label: 'Home',
        items: [{ label: 'Home', icon: 'pi pi-fw pi-home', to: '/' }]
    },
    {
        label: 'Auth',
        icon: 'pi pi-fw pi-briefcase',
        items: [
            {
                label: 'Auth',
                icon: 'pi pi-fw pi-user',
                items: [
                    {
                        label: userStore.isLoggedIn ? 'Logout' : 'Login',
                        icon: userStore.isLoggedIn ? 'pi pi-fw pi-sign-out' : 'pi pi-fw pi-sign-in',
                        command: () => handleAuthAction()
                    }
                ]
            },
            {
                label: 'Profile',
                icon: 'pi pi-fw pi-id-card',
                to: '/user/profile',
                visible: userStore.isLoggedIn
            },
            {
                label: 'Manage',
                icon: 'pi pi-fw pi-pencil',
                to: '/manage',
                visible: userStore.userRole === 'admin' // admin만 볼 수 있도록 설정
            }
        ]
    },
    {
        label: 'Instruments',
        items: [
            {
                label: 'Electric Guitar',
                icon: 'pi pi-fw pi-music',
                items: [
                    { label : 'Epiphone', icon: 'pi pi-fw pi-chevron-right', to: '/instruments/list/electric-guitar/Epiphone' },
                    { label: 'Fender Custom Shop', icon: 'pi pi-fw pi-chevron-right', to: '/instruments/list/electric-guitar/Fender%20Custom%20Shop' },
                    { label: 'Fender USA', icon: 'pi pi-fw pi-chevron-right', to: '/instruments/list/electric-guitar/Fender%20USA' },
                    { label: 'Fender Mexico', icon: 'pi pi-fw pi-chevron-right', to: '/instruments/list/electric-guitar/Fender%20Mexico' },
                    { label: 'Fender Japan', icon: 'pi pi-fw pi-chevron-right', to: '/instruments/list/electric-guitar/Fender%20Japan' },
                    { label: 'Gibson Custom Shop', icon: 'pi pi-fw pi-chevron-right', to: '/instruments/list/electric-guitar/Gibson%20Custom%20Shop' },
                    { label: 'Gibson', icon: 'pi pi-fw pi-chevron-right', to: '/instruments/list/electric-guitar/Gibson' },
                    // { label: 'Ibanez', icon: 'pi pi-fw pi-chevron-right', to: '/instruments/list/electric-guitar/Ibanez' },
                    // { label: 'PRS', icon: 'pi pi-fw pi-chevron-right', to: '/instruments/list/electric-guitar/PRS' }
                ]
            },
            // TODO: Acoustic Guitar 추가
            // Bass
            {
                label: 'Bass',
                icon: 'pi pi-fw pi-music',
                items: [
                    { label: 'Fender USA', icon: 'pi pi-fw pi-chevron-right', to: '/instruments/list/bass/Fender%20USA' },
                    // { label: 'Music Man', icon: 'pi pi-fw pi-chevron-right', to: '/instruments/list/bass/Music Man' },
                    // { label: 'Warwick', icon: 'pi pi-fw pi-chevron-right', to: '/instruments/list/bass/Warwick' },
                    // { label: 'Yamaha', icon: 'pi pi-fw pi-chevron-right', to: '/instruments/list/bass/Yamaha' }
                ]
            }
            // TODO: Effector 등등 추가
        ]
    }
]);

const handleAuthAction = () => {
    if (userStore.isLoggedIn) {
        // 로그아웃 처리
        userStore.logout();
        router.push('/'); // 홈페이지로 리다이렉트
    } else {
        // 로그인 페이지로 이동
        router.push('/auth/login');
    }
};

</script>

<template>
    <ul class="layout-menu">
        <template v-for="(item, i) in model" :key="item">
            <app-menu-item v-if="!item.separator" :item="item" :index="i"></app-menu-item>
            <li v-if="item.separator" class="menu-separator"></li>
        </template>
    </ul>
</template>

<style lang="scss" scoped></style>
