import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { jwtDecode } from 'jwt-decode'
import axios from 'axios'
import axiosInstance from '@/utils/axiosInterceptor'

// 사용자 정보 저장
export const useUserStore = defineStore('user', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || null)
  const username = ref('')
  const userRole = ref('')
  const userId = ref(null)

  const isLoggedIn = computed(() => !!accessToken.value)

  // 토큰 설정
  function setTokens(newAccessToken) {
    accessToken.value = newAccessToken
    localStorage.setItem('accessToken', newAccessToken)
    const decoded = jwtDecode(newAccessToken)
    username.value = decoded.sub
    userRole.value = decoded.role
    userId.value = decoded.userId // JWT에서 userId를 직접 추출
  }

  // 토큰 재발급
  async function refreshAccessToken() {
    // console.log('토큰 갱신 시도')
    try {
      const response = await axios.post(`{$API_URL}/dummy/req/auth/refresh`, {
        withCredentials: true
      })
      setTokens(response.data.token)
      return response.data.token
    } catch (error) {
      // console.error('토큰 갱신 실패:', error);
      throw error
    }
  }

  // 로그아웃
  async function logout() {
    try {
      await axiosInstance.post(`{$API_URL}/dummy/req/auth/logout`, {}, {
        withCredentials: true
      });
    } catch (error) {
      console.error('로그아웃 중 오류 발생:', error);
    } finally {
      accessToken.value = null;
      username.value = '';
      userRole.value = '';
      userId.value = null;
      localStorage.removeItem('accessToken');
    }
  }

  // 초기화 시 토큰이 있다면 사용자 정보 설정
  if (accessToken.value) {
    const decoded = jwtDecode(accessToken.value)
    username.value = decoded.sub
    userRole.value = decoded.role
    userId.value = decoded.userId
  }

  return { accessToken, username, userRole, userId, isLoggedIn, setTokens, refreshAccessToken, logout }
})
