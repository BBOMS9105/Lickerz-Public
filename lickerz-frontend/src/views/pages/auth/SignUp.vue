<script setup>
import FloatingConfigurator from '@/components/FloatingConfigurator.vue';
import { ref, computed } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();

const username = ref('');
const email = ref('');
const password = ref('');
const passwordCheck = ref('');
const message = ref('');
const isError = ref(false);

// 유효성 검사 함수들
const isUsernameValid = computed(() => {
  const usernameRegex = /^[a-zA-Z0-9_]{3,20}$/;
  return usernameRegex.test(username.value);
});

const isEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email.value);
});

const isPasswordValid = computed(() => {
  // 최소 8자, 최소 하나의 문자, 하나의 숫자, 하나의 특수 문자
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
  return passwordRegex.test(password.value);
});

const isPasswordMatch = computed(() => {
  return password.value === passwordCheck.value;
});

const termsAgreed = ref(false);

const isFormValid = computed(() => {
  return isUsernameValid.value && isEmailValid.value && isPasswordValid.value && isPasswordMatch.value;
});

const signUp = async () => {
  if (!isFormValid.value) {
    message.value = '모든 필드를 올바르게 입력해주세요.';
    isError.value = true;
    return;
  }

  try {
    const response = await axios.post(`${API_URL}/dummy/req/auth/users`, {
      username: username.value,
      email: email.value,
      password: password.value,
      role: 'user'
    });
    message.value = '회원가입 성공!';
    isError.value = false;
    alert('회원가입 성공!');
    router.push('/auth/login');
  } catch (error) {
    if (error.response) {
      message.value = 'Error: ' + (error.response.data || 'Unknown error');
    } else {
      message.value = '서버에 문제가 생겼습니다.';
    }
    isError.value = true;
  }
};
</script>

<template>
  <!-- 회원가입 컴포넌트 헤더 -->
  <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-[100vw] overflow-hidden">
      <div class="flex flex-col items-center justify-center">
          <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
              <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20" style="border-radius: 53px">
                  <div class="text-center mb-8">
                    <a href="/">
                      <img src="@/assets/lickerz_logo.png" alt="Lickerz Logo" class="mb-8 w-41 shrink-0 mx-auto">
                    </a>
                      <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4">Lickerz 회원가입</div>
                      <span class="text-muted-color font-medium">Sign up to Lickerz</span>
                  </div>
                  <!-- 입력 폼 -->
                  <div>
                      <label for="id1" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">아이디</label>
                      <InputText id="id1" type="text" placeholder="아이디" class="w-full md:w-[30rem] mb-2" v-model="username" :class="{ 'p-invalid': !isUsernameValid && username }" />
                      <small v-if="!isUsernameValid && username" class="p-error block mb-8">아이디는 3-20자의 영문, 숫자, 언더스코어만 사용 가능합니다.</small>

                      <label for="email1" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">이메일</label>
                      <InputText id="email1" type="text" placeholder="이메일" class="w-full md:w-[30rem] mb-2" v-model="email" :class="{ 'p-invalid': !isEmailValid && email }" />
                      <small v-if="!isEmailValid && email" class="p-error block mb-8">올바른 이메일 형식이 아닙니다.</small>

                      <label for="password1" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">비밀번호</label>
                      <Password id="password1" v-model="password" placeholder="비밀번호" :toggleMask="true" class="mb-2" :class="{ 'p-invalid': !isPasswordValid && password }" fluid :feedback="false"></Password>
                      <small v-if="!isPasswordValid && password" class="p-error block mb-4">비밀번호는 최소 8자, 문자, 숫자, 특수문자를 포함해야 합니다.</small>

                      <label for="password-check" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">비밀번호 확인</label>
                      <Password id="password-check" v-model="passwordCheck" placeholder="비밀번호 확인" :toggleMask="true" class="mb-2" :class="{ 'p-invalid': !isPasswordMatch && passwordCheck }" fluid :feedback="false"></Password>
                      <small v-if="!isPasswordMatch && passwordCheck" class="p-error block mb-4">비밀번호가 일치하지 않습니다.</small>

                      <br>
                      <!-- 이용약관 -->
                      <div class="mb-4">
                        <h3 class="text-surface-900 dark:text-surface-0 font-medium text-base mb-2">Lickerz이용약관</h3>
                        <div class="p-4 bg-surface-100 dark:bg-surface-700 rounded-lg h-24 overflow-y-auto text-sm mb-2">
                          <p class="w-[26rem]">
                            <br>
                            1. 서비스 이용 목적: Lickerz는 사용자 간의 소통과 정보 공유를 위한 플랫폼입니다.<br><br>
                            2. 개인정보 보호: 회원의 개인정보는 서비스 제공 목적으로만 사용되며, 관련 법령에 따라 엄격히 보호됩니다.<br><br>
                            3. 콘텐츠 책임: 사용자가 게시한 콘텐츠에 대한 책임은 해당 사용자에게 있습니다.<br><br>
                            4. 서비스 변경 및 중단: 회사는 필요에 따라 서비스를 변경하거나 중단할 수 있으며, 이 경우 사전에 공지합니다.<br><br>
                            5. 계정 관리: 회원은 자신의 계정 정보를 안전하게 관리해야 하며, 타인에게 양도할 수 없습니다.<br><br>
                            6. 약관 변경: 본 약관은 관련 법령 변경이나 서비스 개선을 위해 변경될 수 있으며, 변경 시 공지사항을 통해 안내됩니다.<br><br>

                            본 약관에 동의함으로써 회원은 위 내용을 이해하고 준수할 것을 동의합니다.

                          </p>
                      </div>

                      </div>
                          <label for="terms" class="block text-surface-900 dark:text-surface-0 font-medium text-base mb-2">
                              <input id="terms" type="checkbox" v-model="termsAgreed" />
                          약관 동의 (필수)
                      </label>

                      <Button label="회원가입" class="w-full" @click="signUp" :disabled="!isFormValid || !termsAgreed"></Button>
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
