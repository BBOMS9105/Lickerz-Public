<script setup>
import FloatingConfigurator from '@/components/FloatingConfigurator.vue';
import { ref, computed } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/userStore';

const router = useRouter();
const userStore = useUserStore();

const username = ref('');
const password = ref('');
const message = ref('');
const isError = ref(false);

// 유효성 검사 함수들
const isUsernameValid = computed(() => {
  const usernameRegex = /^[a-zA-Z0-9_]{4,20}$/;
  return usernameRegex.test(username.value);
});

const isPasswordValid = computed(() => {
  // 최소 8자, 최소 하나의 문자, 하나의 숫자, 하나의 특수 문자
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
  return passwordRegex.test(password.value);
});

const login = async () => {
  if (!username.value || !password.value) {
    message.value = '아이디와 비밀번호를 모두 입력해주세요.';
    isError.value = true;
    return;
  }

  try {
    const response = await axios.post(`${API_URL}/dummy/req/auth/auth/login`, {
      username: username.value,
      password: password.value
    }, {
      headers: {
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest'
      },
      withCredentials: true
    });

    if (response.data.token && response.data.refreshToken) {
      userStore.setTokens(response.data.token, response.data.refreshToken);
      router.push('/');
    } else {
      throw new Error('토큰이 없습니다.');
    }
  } catch (error) {
    message.value = '로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.';
    isError.value = true;
  }
};
</script>

<template>
  <!-- 로그인 컴포넌트 헤더 -->
  <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-[100vw] overflow-hidden">
      <div class="flex flex-col items-center justify-center">
          <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
              <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20" style="border-radius: 53px">
                  <div class="text-center mb-8">
                    <a href="/">
                      <img src="@/assets/lickerz_logo.png" alt="Lickerz Logo" class="mb-8 w-41 shrink-0 mx-auto">
                    </a>
                      <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4">Lickerz 로그인</div>
                      <span class="text-muted-color font-medium">Sign in to Lickerz</span>
                  </div>
                  <!-- 입력 폼 -->
                  <div>
                      <label for="username" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">아이디</label>
                      <InputText id="username" type="text" placeholder="아이디" class="w-full md:w-[30rem] mb-2" v-model="username" :class="{ 'p-invalid': !isUsernameValid && username }" />
                      <small v-if="!isUsernameValid && username" class="p-error block mb-8">아이디는 4-20자의 영문, 숫자, 언더스코어만 사용 가능합니다.</small>

                      <label for="password" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">비밀번호</label>
                      <Password id="password" v-model="password" placeholder="비밀번호" :toggleMask="true" class="mb-2" :class="{ 'p-invalid': !isPasswordValid && password }" fluid :feedback="false"></Password>
                      <small v-if="!isPasswordValid && password" class="p-error block mb-4">비밀번호는 최소 8자, 문자, 숫자, 특수문자를 포함해야 합니다.</small>

                      <div class="flex items-center justify-between mt-2 mb-8 gap-8">
                          <!-- <div class="flex items-center">
                              <Checkbox v-model="checked" id="rememberme1" binary class="mr-2"></Checkbox>
                              <label for="rememberme1">계정 정보 기억하기</label>
                          </div> -->
                          <div class="flex gap-4">
                              <!-- <span class="font-medium no-underline ml-2 text-right cursor-pointer text-primary">계정 찾기</span> -->
                              <router-link to="/auth/find-account" class="font-medium no-underline cursor-pointer text-primary">계정 찾기</router-link>
                              <router-link to="/auth/signup" class="font-medium no-underline cursor-pointer text-primary">회원가입</router-link>
                          </div>
                      </div>
                      <Button label="로그인" class="w-full" @click="login"></Button>
                      <p v-if="message" class="mt-4 text-center" :class="{ 'text-red-500': isError, 'text-green-500': !isError }">{{ message }}</p>
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

.pi-eye {
  transform: scale(1.6);
  margin-right: 1rem;
}

.pi-eye-slash {
  transform: scale(1.6);
  margin-right: 1rem;
}
</style>
