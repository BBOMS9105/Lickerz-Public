import AppLayout from '@/layout/AppLayout.vue';
import { createRouter, createWebHistory } from 'vue-router';
import { jwtDecode } from 'jwt-decode';
import { useToast } from 'primevue/usetoast';
import axios from 'axios';

const routes = [
    {
        path: '/',
        component: AppLayout,
        children: [
            {
                path: '/',
                name: 'dashboard',
                component: () => import('@/views/Dashboard.vue'),
                meta: { title: 'Home :: Lickerz' }
            },
            {
                path: '/manage',
                name: 'manage',
                component: () => import('@/views/pages/Crud.vue'),
                meta: { title: 'Management :: Lickerz', requiresAdmin: true }
            },
            {
                path: '/instruments/list/:type/:brand',
                name: 'InstrumentList',
                component: () => import('@/views/pages/InstrumentList.vue'),
                meta: { dynamicTitle: true }
            },
            {
                path: '/instruments/detail/:id',
                name: 'InstrumentDetail',
                component: () => import('@/views/pages/InstrumentDetail.vue'),
                meta: { dynamicTitle: true }
            },
            {
                path: '/user/profile',
                name: 'profile',
                component: () => import('@/views/pages/auth/UserProfile.vue'),
                meta: { title: 'Profile :: Lickerz', requiresAuth: true }
            },
        ]
    },
    {
        path: '/auth/login',
        name: 'login',
        component: () => import('@/views/pages/auth/Login.vue'),
        meta: { title: 'Login :: Lickerz' }
    },
    {
        path: '/auth/signup',
        name: 'signup',
        component: () => import('@/views/pages/auth/SignUp.vue'),
        meta: { title: 'Sign Up :: Lickerz' }
    },
    {
        path: '/auth/find-account',
        name: 'find-account',
        component: () => import('@/views/pages/auth/FindAccount.vue'),
        meta: { title: 'Find Account :: Lickerz' }
    },
    {
        path: '/auth/change-password',
        name: 'change-password',
        component: () => import('@/views/pages/auth/ChangePassword.vue'),
        meta: { title: 'Change Password :: Lickerz' }
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

// 페이지 제목 설정
router.beforeEach(async (to, from, next) => {
    const toast = useToast();

    // 동적 페이지 제목 설정
    if (to.meta.dynamicTitle) {
        if (to.name === 'InstrumentList' && to.params.brand) {
            document.title = `${to.params.brand} :: Lickerz`;
        } else if (to.name === 'InstrumentDetail' && to.params.id) {
            try {
                const response = await axios.get(`{$API_URL}/dummy/req/auth/public/instruments/${to.params.id}`);
                const instrumentName = response.data.model;
                document.title = `${instrumentName} :: Lickerz`;
            } catch (error) {
                console.error('Error fetching instrument details:', error);
                document.title = 'Lickerz - Instrument Detail';
            }
        }
    } else if (to.meta.title) {
        document.title = to.meta.title;
    } else {
        document.title = 'Lickerz';
    }

    // 관리자 권한 확인
    if (to.matched.some(record => record.meta.requiresAdmin)) {
        const token = localStorage.getItem('accessToken');
        if (token) {
            try {
                const decodedToken = jwtDecode(token);
                if (decodedToken.role === 'admin') {
                    next();
                } else {
                    alert('접근 거부: 관리자 권한이 필요합니다.');
                    next('/');
                }
            } catch (error) {
                console.error('Token decoding failed:', error);
                alert('오류: 인증에 실패했습니다. 다시 로그인해주세요.');
                next('/auth/login');
            }
        } else {
            alert('로그인 필요: 이 페이지에 접근하려면 로그인이 필요합니다.');
            next('/auth/login');
        }
    } else {
        next();
    }
});

export default router;
