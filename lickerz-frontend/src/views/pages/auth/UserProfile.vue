<script setup>
import { ref, onMounted, computed } from 'vue';
import { useUserStore } from '@/store/userStore';
import { useRouter } from 'vue-router';
import axios from 'axios';
import Button from 'primevue/button';
import ProgressSpinner from 'primevue/progressspinner';
import Dialog from 'primevue/dialog';
import Password from 'primevue/password';
import { useConfirm } from "primevue/useconfirm";
import ConfirmDialog from 'primevue/confirmdialog';
import Rating from 'primevue/rating';
import Paginator from 'primevue/paginator';
import Textarea from 'primevue/textarea';
import { useToast } from "primevue/usetoast";
import axiosInstance from '@/utils/axiosInterceptor';
const userStore = useUserStore();
const router = useRouter();
const user = ref(null);
const showDeleteModal = ref(false);
const currentPassword = ref('');
const deleteMessage = ref('');
const isDeleteError = ref(false);
const isPasswordVerified = ref(false);

const confirm = useConfirm();

const userReviews = ref([]);
const totalReviews = ref(0);
const currentPage = ref(0);
const sortBy = ref('createdAt');
const sortDirection = ref('desc');

const toast = useToast();
const editingReview = ref(null);
const editedRating = ref(0);
const editedComment = ref('');
const editedByteCount = ref(0);

const getRoleName = (role) => {
    return role === 'admin' ? '관리자' : '사용자';
};

const formatDate = (dateString) => {
    const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' };
    return new Date(dateString).toLocaleString('ko-KR', options);
};

onMounted(async () => {
    try {
        const response = await axiosInstance.get(`${API_URL}/dummy/req/auth/users/profile`, {
            headers: {
                'Authorization': `Bearer ${userStore.accessToken}`
            }
        });
        user.value = response.data;
    } catch (error) {
        // console.error('Error fetching user profile:', error);
    }
});

const openDeleteModal = () => {
    showDeleteModal.value = true;
    currentPassword.value = '';
    deleteMessage.value = '';
    isDeleteError.value = false;
    isPasswordVerified.value = false;
};

const verifyPassword = async () => {
    try {
        await axiosInstance.post(`{$API_URL}/dummy/req/auth/users/verify-password`,
            { password: currentPassword.value },
            { headers: { 'Authorization': `Bearer ${userStore.accessToken}` } }
        );
        isPasswordVerified.value = true;
        deleteMessage.value = '비밀번호가 확인되었습니다.';
        isDeleteError.value = false;

        confirm.require({
            message: '정말로 회원 탈퇴하시겠습니까? 데이터는 복구되지 않으며 작성한 리뷰는 삭제됩니다.',
            header: '회원 탈퇴 확인',
            icon: 'pi pi-exclamation-triangle',
            acceptClass: 'p-button-danger',
            accept: deleteAccount,
            reject: () => {
                showDeleteModal.value = false;
            }
        });
    } catch (error) {
        deleteMessage.value = '비밀번호가 일치하지 않습니다.';
        isDeleteError.value = true;
    }
};

const deleteAccount = async () => {
    try {
        await axiosInstance.delete(`${API_URL}/dummy/req/auth/users/delete`, {
            headers: { 'Authorization': `Bearer ${userStore.accessToken}` }
        });
        userStore.logout();
        router.push('/');
        alert('회원 탈퇴가 완료되었습니다.');
    } catch (error) {
        console.error('회원 탈퇴 실패:', error);
        deleteMessage.value = '회원 탈퇴에 실패했습니다. 다시 시도해주세요.';
        isDeleteError.value = true;
    }
};

const fetchUserReviews = async (page = 0) => {
    try {
        const response = await axiosInstance.get(`{$API_URL}/dummy/req/auth/users/reviews`, {
            params: {
                page: page,
                size: 10,
                sort: `${sortBy.value},${sortDirection.value}`
            },
            headers: {
                'Authorization': `Bearer ${userStore.accessToken}`
            }
        });
        // console.log('Fetched user reviews:', response.data);
        userReviews.value = response.data.content;
        totalReviews.value = response.data.totalElements;
        currentPage.value = page;
    } catch (error) {
        console.error('Error fetching user reviews:', error);
    }
};

const onPageChange = (event) => {
    fetchUserReviews(event.page);
};

const sortReviews = (criteria) => {
    if (criteria === 'oldest') {
        sortBy.value = 'createdAt';
        sortDirection.value = 'asc';
    } else {
        sortBy.value = criteria;
        sortDirection.value = 'desc';
    }
    fetchUserReviews(0);
};

onMounted(() => {
    fetchUserReviews();
});

const startEditingReview = (review) => {
    editingReview.value = review.id;
    editedRating.value = review.rating;
    editedComment.value = review.comment;
    updateEditedByteCount();
};

const cancelEditingReview = () => {
    editingReview.value = null;
    editedRating.value = 0;
    editedComment.value = '';
};

const saveEditedReview = async () => {
    if (!userStore.userId || !editingReview.value) {
        toast.add({ severity: 'error', summary: '오류', detail: '로그인이 필요하거나 수정할 리뷰가 선택되지 않았습니다.', life: 3000 });
        return;
    }
    if (editedByteCount.value < 24) {
        alert('리뷰는 최소 24바이트 이상 작성해야 합니다.');
        return;
    }
    if (editedByteCount.value > 2000) {
        alert('리뷰는 2000바이트를 초과할 수 없습니다.');
        return;
    }
    try {
        const reviewToEdit = userReviews.value.find(review => review.id === editingReview.value);
        const response = await axios.put(`{$API_URL}/dummy/req/auth/instruments/${reviewToEdit.instrumentDto.id}/reviews/${editingReview.value}`, {
            rating: editedRating.value,
            comment: editedComment.value
        }, {
            headers: { 'Authorization': `Bearer ${userStore.accessToken}` }
        });
        await fetchUserReviews(currentPage.value);
        cancelEditingReview();
        toast.add({ severity: 'success', summary: '성공', detail: '리뷰가 수정되었습니다.', life: 3000 });
    } catch (error) {
        console.error('Error updating review:', error);
        toast.add({ severity: 'error', summary: '오류', detail: '리뷰 수정에 실패했습니다.', life: 3000 });
    }
};

const updateEditedByteCount = () => {
    const encoder = new TextEncoder();
    editedByteCount.value = encoder.encode(editedComment.value).length;
};

const deleteReview = (review) => {
    confirm.require({
        message: '정말로 이 리뷰를 삭제하시겠습니까?',
        header: '리뷰 삭제 확인',
        icon: 'pi pi-exclamation-triangle',
        acceptClass: 'p-button-danger',
        accept: async () => {
            if (!userStore.userId) {
                toast.add({ severity: 'error', summary: '오류', detail: '로그인이 필요합니다.', life: 3000 });
                return;
            }
            try {
                await axios.delete(`{$API_URL}/dummy/req/auth/instruments/${review.instrumentDto.id}/reviews/${review.id}`, {
                    headers: { 'Authorization': `Bearer ${userStore.accessToken}` }
                });
                await fetchUserReviews(currentPage.value);
                toast.add({ severity: 'success', summary: '성공', detail: '리뷰가 삭제되었습니다.', life: 3000 });
            } catch (error) {
                console.error('Error deleting review:', error);
                toast.add({ severity: 'error', summary: '오류', detail: '리뷰 삭제에 실패했습니다.', life: 3000 });
            }
        },
        reject: () => {
            toast.add({ severity: 'info', summary: '취소', detail: '리뷰 삭제가 취소되었습니다.', life: 3000 });
        }
    });
};
</script>

<template>
    <ConfirmDialog></ConfirmDialog>
    <!-- 내 정보 -->
    <div class="grid">
        <div class="col-12 lg:col-8 lg:col-offset-2">
            <div class="card">
                <div class="flex align-items-center justify-content-between mb-4">
                    <h1 class="text-3xl font-bold m-0 flex align-items-center">
                        <i class="text-4xl"></i>사용자 프로필
                    </h1>
                    <br>
                </div>
                <div v-if="user" class="p-fluid">
                    <div class="grid">
                        <div class="col-12 md:col-6 mb-4">
                            <div class="p-field">
                                <label class="block text-lg font-medium mb-2">
                                    <i class="pi pi-user mr-2"></i>사용자 이름
                                </label>
                                <p class="text-xl p-3 bg-surface-100 border-round">{{ user.username }}</p>
                            </div>
                        </div>
                        <div class="col-12 md:col-6 mb-4">
                            <div class="p-field">
                                <label class="block text-lg font-medium mb-2">
                                    <i class="pi pi-envelope mr-2"></i>이메일
                                </label>
                                <p class="text-xl p-3 bg-surface-100 border-round">{{ user.email }}</p>
                            </div>
                        </div>
                        <div class="col-12 md:col-6 mb-4">
                            <div class="p-field">
                                <label class="block text-lg font-medium mb-2">
                                    <i class="pi pi-calendar mr-2"></i>가입일
                                </label>
                                <p class="text-xl p-3 bg-surface-100 border-round">{{ formatDate(user.createdAt) }}</p>
                            </div>
                        </div>
                    </div>
                    <div class="mt-6">
                        <h2 class="text-2xl font-bold mb-3">
                            <i class="pi pi-shield mr-2"></i>보안 설정
                        </h2>
                        <div class="flex justify-content-between">
                            <Button icon="pi pi-lock" label="비밀번호 변경" class="p-button-secondary" @click="$router.push('/auth/change-password')" />
                            <Button icon="pi pi-user-minus" label="회원 탈퇴" class="p-button-danger" @click="openDeleteModal" />
                        </div>
                    </div>
                </div>
                <div v-else class="flex align-items-center justify-content-center" style="height: 300px;">
                    <ProgressSpinner />
                </div>
            </div>
        </div>
    </div>

    <Dialog v-model:visible="showDeleteModal" modal header="회원 탈퇴" :style="{ width: '400px' }">
        <div class="p-fluid">
            <div class="field">
                <label for="currentPassword" class="font-bold">현재 비밀번호</label>
                <Password id="currentPassword" v-model="currentPassword" :feedback="false" toggleMask inputClass="w-full" />
            </div>
            <small :class="{ 'p-error': isDeleteError, 'p-success': !isDeleteError }">{{ deleteMessage }}</small>
        </div>
        <template #footer>
            <Button label="취소" icon="pi pi-times" @click="showDeleteModal = false" class="p-button-text" />
            <Button label="확인" icon="pi pi-check" @click="verifyPassword" autofocus />
        </template>
    </Dialog>
    <!-- 내가 작성한 리뷰 -->
    <div class="card mt-4">
        <div class="flex justify-between items-center mb-4">
            <h2 class="text-2xl font-bold">내가 작성한 리뷰</h2>
            <div>
                <Button label="최신순" class="p-button-text" @click="sortReviews('createdAt')" :class="{ 'p-button-outlined p-button-secondary': sortBy === 'createdAt' && sortDirection === 'desc' }" />
                <Button label="평점순" class="p-button-text ml-2" @click="sortReviews('rating')" :class="{ 'p-button-outlined p-button-secondary': sortBy === 'rating' }" />
                <Button label="오래된순" class="p-button-text ml-2" @click="sortReviews('oldest')" :class="{ 'p-button-outlined p-button-secondary': sortBy === 'createdAt' && sortDirection === 'asc' }" />
            </div>
        </div>
        <div v-if="userReviews.length > 0">
            <div v-for="review in userReviews" :key="review.id" class="mb-4 p-4 border rounded flex">
                <div class="instrument-image-container mr-4">
                    <img :src="review.instrumentDto.imageUrl" :alt="review.instrumentDto.model" class="instrument-image">
                </div>
                <div class="flex-grow">
                    <div v-if="editingReview !== review.id">
                        <div class="flex justify-between items-center mb-2">
                            <div>
                                <span class="font-bold">{{ review.instrumentDto.model }}</span>
                                <Rating v-model="review.rating" :cancel="false" :readonly="true" />
                            </div>
                            <div class="text-sm text-gray-500">
                                {{ formatDate(review.createdAt) }}
                                <span v-if="review.updatedAt"><br>(수정됨: {{ formatDate(review.updatedAt) }})</span>
                            </div>
                        </div>
                        <p>{{ review.comment }}</p>
                        <div class="flex justify-end mt-2">
                            <Button icon="pi pi-pencil" class="p-button-text p-button-lg" @click="startEditingReview(review)" />
                            <Button icon="pi pi-trash" class="p-button-text p-button-danger p-button-lg" @click="deleteReview(review)" />
                        </div>
                    </div>
                    <div v-else>
                        <Rating v-model="editedRating" :cancel="false" />
                        <div class="relative mt-2">
                            <Textarea v-model="editedComment" rows="3" class="w-full" @input="updateEditedByteCount" />
                            <span class="absolute bottom-2 right-2 text-sm text-gray-500">{{ editedByteCount }}/2000 Byte</span>
                        </div>
                        <div class="flex justify-end mt-2">
                            <Button label="취소" class="p-button-text" @click="cancelEditingReview" />
                            <Button label="저장" class="ml-2" @click="saveEditedReview" />
                        </div>
                    </div>
                </div>
            </div>
            <Paginator v-if="totalReviews > 10" :rows="10" :totalRecords="totalReviews" @page="onPageChange" :first="currentPage * 10" />
        </div>
        <div v-else class="text-center py-8 text-gray-500 text-xl">
            작성한 리뷰가 없습니다.
        </div>
    </div>
</template>

<style scoped>
.p-button {
    width: auto;
}

.bg-surface-100 {
    background-color: var(--surface-100);
}

.border-round {
    border-radius: var(--border-radius);
}

:deep(.p-dialog-content) {
    padding-bottom: 0;
}

:deep(.p-dialog-footer) {
    padding-top: 1.5rem;
}

:deep(.p-password) {
    width: 100%;
}

:deep(.p-password input) {
    width: 100%;
}

.instrument-image-container {
    width: 100px;
    height: 100px;
    overflow: hidden;
}

.instrument-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.p-button-lg {
  font-size: 1.25rem;
  padding: 0rem 1.5rem;
}

.p-button-lg .p-button-icon {
  font-size: 1.5rem;
}
</style>
