import axios from 'axios'
import { useUserStore } from '@/store/userStore'

const axiosInstance = axios.create({
  baseURL: 'https://lickerz.duckdns.org/api'
})

// 요청 인터셉터
axiosInstance.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.accessToken) {
      config.headers['Authorization'] = `Bearer ${userStore.accessToken}`
      // console.log('Adding token to request:', config.url);
      // console.log('Token:', userStore.accessToken);
    } else {
      // console.log('No token available for request:', config.url);
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 응답 인터셉터
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    // console.log('Interceptor caught an error:', error.response?.status);
    if (error.response && error.response.status === 401) {
      // console.log('401 오류 감지, 토큰 갱신 시도');
      const userStore = useUserStore()
      try {
        const newToken = await userStore.refreshAccessToken()
        error.config.headers['Authorization'] = `Bearer ${newToken}`
        return axiosInstance(error.config)
      } catch (refreshError) {
        // console.error('토큰 갱신 실패:', refreshError)
        throw refreshError
      }
    }
    return Promise.reject(error)
  }
)

export default axiosInstance
