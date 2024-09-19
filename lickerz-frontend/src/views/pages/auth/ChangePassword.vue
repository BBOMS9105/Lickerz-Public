<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/userStore';
import axios from 'axios';
import Button from 'primevue/button';
import Password from 'primevue/password';

const router = useRouter();
const userStore = useUserStore();
const API_URL = process.env.VUE_APP_API_URL;
const currentPassword = ref('');
const newPassword = ref('');
const confirmPassword = ref('');
const message = ref('');
const isError = ref(false);
const isCurrentPasswordVerified = ref(false);

const isPasswordValid = computed(() => {
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
    return passwordRegex.test(newPassword.value);
});

const verifyCurrentPassword = async () => {
    try {
        await axios.post(`${API_URL}/dummy/req/auth/users/verify-password`,
            { password: currentPassword.value },
            { headers: { 'Authorization': `Bearer ${userStore.accessToken}` } }
        );
        isCurrentPasswordVerified.value = true;
        message.value = '현재 비밀번호가 확인되었습니다. 새 비밀번호를 입력해주세요.';
        isError.value = false;
    } catch (error) {
        message.value = '현재 비밀번호가 일치하지 않습니다.';
        isError.value = true;
    }
};

const changePassword = async () => {
    if (!isPasswordValid.value) {
        message.value = '비밀번호는 최소 8자, 문자, 숫자, 특수문자를 포함해야 합니다.';
        isError.value = true;
        return;
    }

    if (newPassword.value !== confirmPassword.value) {
        message.value = '새 비밀번호가 일치하지 않습니다.';
        isError.value = true;
        return;
    }

    try {
        await axios.post(`${API_URL}/dummy/req/auth/users/change-password`,
            { currentPassword: currentPassword.value, newPassword: newPassword.value },
            { headers: { 'Authorization': `Bearer ${userStore.accessToken}` } }
        );
        message.value = '비밀번호가 성공적으로 변경되었습니다.';
        isError.value = false;
        alert('비밀번호가 성공적으로 변경되었습니다.');
        router.push('/user/profile');
    } catch (error) {
        message.value = '비밀번호 변경에 실패했습니다.';
        isError.value = true;
    }
};
</script>

<template>
    <!-- 메인 -->
    <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-[100vw] overflow-hidden">
        <div class="flex flex-col items-center justify-center">
            <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
                <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20" style="border-radius: 53px">
                    <div class="text-center mb-8">
                        <a href="/">
                            <img src="@/assets/lickerz_logo.png" alt="Lickerz Logo" class="mb-8 w-41 shrink-0 mx-auto">
                        </a>
                        <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4">비밀번호 변경</div>
                        <span class="text-muted-color font-medium">Change your password</span>
                    </div>
                    <!-- 비밀번호 확인 -->
                    <div>
                        <div v-if="!isCurrentPasswordVerified">
                            <label for="currentPassword" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">현재 비밀번호</label>
                            <Password id="currentPassword" v-model="currentPassword" placeholder="현재 비밀번호" :toggleMask="true" class="w-full md:w-[30rem] mb-4" :feedback="false"></Password>
                            <Button label="비밀번호 확인" class="w-full" @click="verifyCurrentPassword"></Button>
                        </div>
                        <!-- 비밀번호 변경 -->
                        <div v-else>
                            <label for="newPassword" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">새 비밀번호</label>
                            <Password id="newPassword" v-model="newPassword" placeholder="새 비밀번호" :toggleMask="true" class="w-full md:w-[30rem] mb-2" :class="{ 'p-invalid': !isPasswordValid && newPassword }" :feedback="false"></Password>
                            <small v-if="!isPasswordValid && newPassword" class="p-error block mb-4">비밀번호는 최소 8자, 문자, 숫자, 특수문자를 포함해야 합니다.</small>

                            <label for="confirmPassword" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">새 비밀번호 확인</label>
                            <Password id="confirmPassword" v-model="confirmPassword" placeholder="새 비밀번호 확인" :toggleMask="true" class="w-full md:w-[30rem] mb-4" :feedback="false"></Password>

                            <Button label="비밀번호 변경" class="w-full" @click="changePassword"></Button>
                        </div>
                        <div v-if="message" :class="['mt-4 text-center', { 'text-red-500': isError, 'text-green-500': !isError }]">
                            {{ message }}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.text-red-500 {
    color: red !important;
}

.text-green-500 {
    color: green !important;
}

:deep(.p-password) {
    width: 100%;
}

:deep(.p-password-input) {
    width: 100%;
}
</style>
