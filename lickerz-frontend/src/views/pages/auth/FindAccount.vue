<script setup>
import { ref, computed, onUnmounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const email = ref('');
const username = ref('');
const verificationCode = ref('');
const newPassword = ref('');
const newPasswordCheck = ref('');
const message = ref('');
const isError = ref(false);
const isFindingId = ref(true);
const isVerificationSent = ref(false);
const isVerified = ref(false);
const remainingTime = ref(0);
let timerInterval;

const isEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email.value);
});

const isUsernameValid = computed(() => {
  const usernameRegex = /^[a-zA-Z0-9_]{4,20}$/;
  return usernameRegex.test(username.value);
});

const isPasswordValid = computed(() => {
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
  return passwordRegex.test(newPassword.value);
});

const isPasswordMatch = computed(() => {
  return newPassword.value === newPasswordCheck.value;
});

const formattedTime = computed(() => {
  const minutes = Math.floor(remainingTime.value / 60);
  const seconds = remainingTime.value % 60;
  return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
});

const startTimer = () => {
  remainingTime.value = 300; // 5분 = 300초
  clearInterval(timerInterval);
  timerInterval = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--;
    } else {
      clearInterval(timerInterval);
      isVerificationSent.value = false;
    }
  }, 1000);
};

onUnmounted(() => {
  clearInterval(timerInterval);
});

const findAccount = async () => {
  if (isFindingId.value) {
    if (!isEmailValid.value) {
      message.value = '유효한 이메일 주소를 입력해주세요.';
      isError.value = true;
      return;
    }

    try {
      const response = await axios.get(`${API_URL}/dummy/req/auth/public/users/find-id/${email.value}`);
      const maskedUsername = response.data.username.slice(0, -2) + '**';
      message.value = `찾은 아이디: ${maskedUsername}`;
      isError.value = false;
    } catch (error) {
      message.value = '해당 이메일로 등록된 계정을 찾을 수 없습니다.';
      isError.value = true;
    }
  } else {
    if (!isUsernameValid.value || !isEmailValid.value) {
      message.value = '유효한 아이디와 이메일 주소를 입력해주세요.';
      isError.value = true;
      return;
    }

    try {
      await axios.post(`${API_URL}/dummy/req/auth/public/users/send-verification-code`, { email: email.value });
      message.value = '인증 코드가 이메일로 전송되었습니다.';
      isError.value = false;
      isVerificationSent.value = true;
      startTimer();
    } catch (error) {
      message.value = '인증 코드 전송에 실패했습니다.';
      isError.value = true;
    }
  }
};

const verifyCode = async () => {
  try {
    await axios.post(`${API_URL}/dummy/req/auth/public/users/verify-code`, { email: email.value, code: verificationCode.value });
    message.value = '인증 성공';
    isError.value = false;
    isVerified.value = true;
    clearInterval(timerInterval);
  } catch (error) {
    message.value = '잘못된 인증 코드입니다.';
    isError.value = true;
  }
};

const resetPassword = async () => {
  if (!isPasswordValid.value) {
    message.value = '비밀번호는 최소 8자, 문자, 숫자, 특수문자를 포함해야 합니다.';
    isError.value = true;
    return;
  }
  if (!isPasswordMatch.value) {
    message.value = '비밀번호가 일치하지 않습니다.';
    isError.value = true;
    return;
  }
  try {
    await axios.post(`${API_URL}/dummy/req/auth/public/users/reset-password`, {
      email: email.value,
      code: verificationCode.value,
      newPassword: newPassword.value
    });
    alert('비밀번호가 성공적으로 변경되었습니다.');
    router.push('/auth/login');
  } catch (error) {
    message.value = '비밀번호 변경에 실패했습니다.';
    isError.value = true;
  }
};

const toggleFindMode = () => {
  isFindingId.value = !isFindingId.value;
  message.value = '';
  isError.value = false;
  isVerificationSent.value = false;
  isVerified.value = false;
  email.value = '';
  username.value = '';
  verificationCode.value = '';
  newPassword.value = '';
  newPasswordCheck.value = '';
  clearInterval(timerInterval);
  remainingTime.value = 0;
};
</script>

<template>
  <!-- 메인 -->
  <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-[100vw] overflow-hidden">
    <div class="flex flex-col items-center justify-center">
      <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
        <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20" style="border-radius: 53px">
          <div class="text-center mb-8">
            <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4">계정 찾기</div>
            <span class="text-muted-color font-medium">{{ isFindingId ? 'Find ID' : 'Reset Password' }}</span>
          </div>
          <!-- 아이디 찾기 -->
          <div>
            <label :for="isFindingId ? 'email' : 'username'" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">
              {{ isFindingId ? '이메일' : '아이디' }}
            </label>
            <InputText :id="isFindingId ? 'email' : 'username'" type="text" :placeholder="isFindingId ? '이메일' : '아이디'"
                       class="w-full md:w-[30rem] mb-2" :value="isFindingId ? email : username"
                       @input="isFindingId ? email = $event.target.value : username = $event.target.value"
                       :class="{ 'p-invalid': isFindingId ? !isEmailValid && email : !isUsernameValid && username }" />
            <small v-if="isFindingId && !isEmailValid && email" class="p-error block mb-4">유효한 이메일 주소를 입력해주세요.</small>
            <small v-if="!isFindingId && !isUsernameValid && username" class="p-error block mb-4">아이디는 4-20자의 영문, 숫자, 언더스코어만 사용 가능합니다.</small>

            <div v-if="!isFindingId">
              <label for="email" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2 mt-4">이메일</label>
              <InputText id="email" type="text" placeholder="이메일" class="w-full md:w-[30rem] mb-2" v-model="email" :class="{ 'p-invalid': !isEmailValid && email }" />
              <small v-if="!isEmailValid && email" class="p-error block mb-4">유효한 이메일 주소를 입력해주세요.</small>
            </div>
            <!-- 인증코드 입력 -->
            <div v-if="!isFindingId && isVerificationSent && !isVerified">
              <label for="verificationCode" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2 mt-4">인증 코드</label>
              <InputText id="verificationCode" type="text" placeholder="인증 코드" class="w-full md:w-[30rem] mb-2" v-model="verificationCode" />
              <div class="text-center text-primary-500 font-bold mb-2">남은 시간: {{ formattedTime }}</div>
              <div class="flex justify-center">
                <Button label="인증 코드 확인" class="w-[420px] mt-4" @click="verifyCode"></Button>
              </div>
            </div>
            <!-- 비밀번호 재설정 -->
            <div v-if="!isFindingId && isVerified">
              <label for="newPassword" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2 mt-4">새 비밀번호</label>
              <Password id="newPassword" v-model="newPassword" placeholder="새 비밀번호" :toggleMask="true" class="w-full md:w-[30rem] mb-2" :class="{ 'p-invalid': !isPasswordValid && newPassword }" :feedback="false"></Password>
              <small v-if="!isPasswordValid && newPassword" class="p-error block mb-4">비밀번호는 최소 8자, 문자, 숫자, 특수문자를 포함해야 합니다.</small>

              <label for="newPasswordCheck" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2 mt-4">새 비밀번호 확인</label>
              <Password id="newPasswordCheck" v-model="newPasswordCheck" placeholder="새 비밀번호 확인" :toggleMask="true" class="w-full md:w-[30rem] mb-2" :class="{ 'p-invalid': !isPasswordMatch && newPasswordCheck }" :feedback="false"></Password>
              <small v-if="!isPasswordMatch && newPasswordCheck" class="p-error block mb-4">비밀번호가 일치하지 않습니다.</small>

              <div class="flex justify-center">
                <Button label="비밀번호 변경" class="w-[420px] mt-4" @click="resetPassword"></Button>
              </div>
            </div>

            <div v-if="!isVerificationSent || isFindingId" class="flex justify-center">
              <Button :label="isFindingId ? '아이디 찾기' : '인증 코드 요청'" class="w-[420px] mt-4" @click="findAccount"></Button>
            </div>
            <p v-if="message" class="mt-4 text-center" :class="{ 'text-red-500': isError, 'text-green-500': !isError }">{{ message }}</p>

            <div class="text-center mt-4">
              <a href="#" @click.prevent="toggleFindMode" class="text-primary-500 hover:underline">
                {{ isFindingId ? '비밀번호를 잊으셨나요?' : '아이디를 잊으셨나요?' }}
              </a>
            </div>
            <div class="text-center mt-2">
              <router-link to="/auth/login" class="text-primary-500 hover:underline">로그인 페이지로 돌아가기</router-link>
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

/* Password 컴포넌트의 너비를 조정하는 스타일 */
:deep(.p-password) {
  width: 100%;
}

:deep(.p-password-input) {
  width: 100%;
}
</style>
