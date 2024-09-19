<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { useUserStore } from '@/store/userStore';
import { useToast } from 'primevue/usetoast';
import { storeToRefs } from 'pinia';
import Breadcrumb from 'primevue/breadcrumb';
import { useConfirm } from "primevue/useconfirm";
import ConfirmDialog from 'primevue/confirmdialog';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const { userId, isLoggedIn } = storeToRefs(userStore);
const toast = useToast();
const instrument = ref(null);
const loading = ref(true);
const newReview = ref('');
const newRating = ref(0);
const reviews = ref([]);
const totalReviews = ref(0);
const currentPage = ref(0);
const editingReview = ref(null);
const editedRating = ref(0);
const editedComment = ref('');
const sortBy = ref('createdAt');
const sortDirection = ref('desc');
const byteCount = ref(0);
const editedByteCount = ref(0);
const confirm = useConfirm();

const naverShoppingUrl = computed(() => {
    if (instrument.value) {
        return `https://search.shopping.naver.com/search/all?query=${encodeURIComponent(instrument.value.model)}`;
    }
    return '';
});

const youtubeSearchUrl = computed(() => {
    if (instrument.value) {
        return `https://www.youtube.com/results?search_query=${encodeURIComponent(instrument.value.model)}`;
    }
    return '';
});

const digimartSearchUrl = computed(() => {
    if (instrument.value) {
        return `https://www.digimart.net/search?keywordAnd=${encodeURIComponent(instrument.value.model)}`;
    }
    return '';
});

// 리뷰 작성 권한 확인
const canAddReview = computed(() => {
    return isLoggedIn.value && (userStore.userRole === 'user' || userStore.userRole === 'admin');
});

// 리뷰 수정 권한 확인
const canEditReview = (review) => {
    return userId.value === review.userId || userStore.userRole === 'admin';
};

// 악기 데이터 로드
onMounted(async () => {
    try {
        const response = await axios.get(`{$API_URL}/dummy/req/auth/public/instruments/${route.params.id}`);
        instrument.value = response.data;
        await fetchReviews();
    } catch (error) {
        // console.error('Error fetching instrument data:', error);
    } finally {
        loading.value = false;
    }
});

// 리뷰 제출
const submitReview = async () => {
    if (byteCount.value < 24) {
        alert('리뷰는 최소 24바이트 이상 작성해야 합니다.');
        return;
    }
    if (byteCount.value > 2000) {
        alert('리뷰는 2000바이트를 초과할 수 없습니다.');
        return;
    }

    // console.log('Current userStore state:', userStore.$state);
    // console.log('Submitting review with userId:', userId.value);

    if (!userId.value) {
        // console.error('userId is not set in userStore');
        toast.add({ severity: 'error', summary: '오류', detail: '사용자 ID가 없습니다. 다시 로그인해 주세요.', life: 3000 });
        return;
    }

    try {
        const reviewData = {
            userId: userId.value,
            rating: newRating.value,
            comment: newReview.value
        };
        // console.log('Sending review data:', reviewData);

        const response = await axios.post(`{$API_URL}/dummy/req/auth/instruments/${route.params.id}/reviews`, reviewData, {
            headers: {
                'Authorization': `Bearer ${userStore.accessToken}`
            }
        });

        // console.log('Review submission response:', response.data);
        toast.add({ severity: 'success', summary: '성공', detail: '리뷰가 성공적으로 저장되었습니다.', life: 3000 });
        newReview.value = '';
        newRating.value = 0;
        byteCount.value = 0;
        await fetchReviews();
    } catch (error) {
        // console.error('Review submission error:', error.response ? error.response.data : error.message);
        const errorMessage = error.response && error.response.data ? error.response.data : '리뷰 저장 중 오류가 발생했습니다.';
        toast.add({ severity: 'error', summary: '오류', detail: errorMessage, life: 3000 });
    }
};

const fetchReviews = async (page = 0) => {
    try {
        const response = await axios.get(`{$API_URL}/dummy/req/auth/instruments/${route.params.id}/reviews`, {
            params: {
                page: page,
                size: 10,
                sort: `${sortBy.value},${sortDirection.value}`
            }
        });
        // console.log('Fetched reviews:', response.data);
        reviews.value = response.data.content;
        totalReviews.value = response.data.totalElements;
        currentPage.value = page;
    } catch (error) {
        // console.error('Error fetching reviews:', error);
        toast.add({ severity: 'error', summary: '오류', detail: '리뷰를 불러오는 데 실패했습니다.', life: 3000 });
    }
};

const onPageChange = (event) => {
    fetchReviews(event.page);
};

// 좋아요 토글
const toggleLike = async (review) => {
    if (!userId.value) {
        toast.add({ severity: 'error', summary: '오류', detail: '로그인이 필요합니다.', life: 3000 });
        return;
    }
    try {
        const response = await axios({
            method: 'post',
            url: `{$API_URL}/dummy/req/auth/instruments/${route.params.id}/reviews/${review.id}/like`,
            headers: { 'Authorization': `Bearer ${userStore.accessToken}` }
        });
        const updatedReview = response.data;
        const index = reviews.value.findIndex(r => r.id === updatedReview.id);
        if (index !== -1) {
            reviews.value[index] = updatedReview;
        }
        toast.add({ severity: 'success', summary: '성공', detail: updatedReview.likedByCurrentUser ? '좋아요를 눌렀습니다.' : '좋아요가 취소되었습니다.', life: 3000 });
    } catch (error) {
        // console.error('Error toggling like:', error);
        const errorMessage = error.response && error.response.data ? error.response.data : '좋아요 처리에 실패했습니다.';
        toast.add({ severity: 'error', summary: '오류', detail: errorMessage, life: 3000 });
    }
};

const startEditingReview = (review) => {
    // console.log('Editing review:', review.id);
    editingReview.value = review.id;
    editedRating.value = review.rating;
    editedComment.value = review.comment;
    updateEditedByteCount();
};

const cancelEditingReview = () => {
    // console.log('Canceling edit');
    editingReview.value = null;
    editedRating.value = 0;
    editedComment.value = '';
};

const saveEditedReview = async () => {
    if (!userId.value || !editingReview.value) {
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
        const response = await axios.put(`{$API_URL}/dummy/req/auth/instruments/${route.params.id}/reviews/${editingReview.value}`, {
            rating: editedRating.value,
            comment: editedComment.value
        }, {
            headers: { 'Authorization': `Bearer ${userStore.accessToken}` }
        });
        await fetchReviews(currentPage.value);
        cancelEditingReview();
        toast.add({ severity: 'success', summary: '성공', detail: '리뷰가 수정되었습니다.', life: 3000 });
    } catch (error) {
        // console.error('Error updating review:', error);
        toast.add({ severity: 'error', summary: '오류', detail: '리뷰 수정에 실패했습니다.', life: 3000 });
    }
};

const deleteReview = (reviewId) => {
    confirm.require({
        message: '정말로 이 리뷰를 삭제하시겠습니까?',
        header: '리뷰 삭제 확인',
        icon: 'pi pi-exclamation-triangle',
        acceptClass: 'p-button-danger',
        accept: async () => {
            if (!userId.value) {
                toast.add({ severity: 'error', summary: '오류', detail: '로그인이 필요합니다.', life: 3000 });
                return;
            }
            try {
                await axios.delete(`{$API_URL}/dummy/req/auth/instruments/${route.params.id}/reviews/${reviewId}`, {
                    headers: { 'Authorization': `Bearer ${userStore.accessToken}` }
                });
                await fetchReviews(currentPage.value);
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

const formatDate = (date) => {
    return new Date(date).toLocaleString();
};

const sortReviews = (criteria) => {
    if (criteria === 'oldest') {
        sortBy.value = 'createdAt';
        sortDirection.value = 'asc';
    } else {
        sortBy.value = criteria;
        sortDirection.value = 'desc';
    }
    fetchReviews(0);
};

const updateByteCount = () => {
    const encoder = new TextEncoder();
    byteCount.value = encoder.encode(newReview.value).length;
};

const updateEditedByteCount = () => {
    const encoder = new TextEncoder();
    editedByteCount.value = encoder.encode(editedComment.value).length;
};

// 수평 메뉴
const breadcrumbHome = { icon: 'pi pi-home', to: '/' };
const breadcrumbItems = computed(() => {
    if (!instrument.value) return [];
    return [
        {
            label: instrument.value.type,
            command: () => router.push(`/instruments/${instrument.value.type}`)
        },
        {
            label: instrument.value.brand,
            command: () => router.push(`/instruments/${instrument.value.type}/${instrument.value.brand}`)
        },
        { label: instrument.value.model }
    ];
});
</script>

<template>
    <ConfirmDialog></ConfirmDialog>
    <div v-if="loading">Loading...</div>
    <div v-else-if="instrument" class="card">
        <Breadcrumb :home="breadcrumbHome" :model="breadcrumbItems" />
        <div class="flex">
            <div class="instrument-image-container">
                <img :src="instrument.imgUrl" :alt="instrument.model" class="instrument-image" />
            </div>
            <div class="instrument-details">
                <h1 class="text-4xl font-bold mb-4 instrument-name">{{ instrument.model }}</h1>
                <div class="flex items-center mt-4">
                    <Rating v-model="instrument.averageRating" :cancel="false" :readonly="true" />
                    <span class="ml-2 text-xl">{{ instrument.averageRating.toFixed(1) }}</span>
                    <span class="ml-2 text-lg">({{ instrument.reviewCount }} reviews)</span>
                </div>
                <div class="mt-4 flex flex-wrap">
                    <a :href="naverShoppingUrl" target="_blank" rel="noopener noreferrer" class="p-button p-button-success mr-2 mb-2">
                        <i class="pi pi-shopping-cart mr-2"></i>
                        네이버 쇼핑 검색
                    </a>
                    <a :href="youtubeSearchUrl" target="_blank" rel="noopener noreferrer" class="p-button p-button-danger mr-2 mb-2">
                        <i class="pi pi-youtube mr-2"></i>
                        유튜브 검색
                    </a>
                    <a :href="digimartSearchUrl" target="_blank" rel="noopener noreferrer" class="p-button p-button-warning mb-2">
                        <i class="pi pi-search mr-2"></i>
                        Digimart 검색
                    </a>
                </div>
            </div>
        </div>
    </div>
    <div v-else>Instrument not found</div>
    <!-- 리뷰 작성 폼 -->
    <div v-if="instrument" class="card mt-4">
        <h2 class="text-2xl font-bold mb-4">리뷰 작성</h2>
        <div v-if="canAddReview" class="flex flex-col gap-4">
            <div class="flex items-center">
                <Rating v-model="newRating" :cancel="false" :stars="5" class="larger-rating" />
                <span class="ml-2 text-lg">{{ newRating }}/5</span>
            </div>
            <div class="relative">
                <Textarea v-model="newReview" rows="5" class="w-full" :placeholder="`리뷰를 작성해주세요.\n최소 24바이트 이상,\n최대 2000바이트까지 입력 가능합니다.`" @input="updateByteCount" />
                <span class="absolute bottom-2 right-2 text-sm" :class="{ 'text-red-500': byteCount < 24, 'text-gray-500': byteCount >= 24 }">
                    {{ byteCount }}/2000 Byte
                </span>
            </div>
            <div v-if="byteCount < 24" class="text-red-500 text-sm">
                리뷰는 최소 24바이트 이상 작성해야 합니다.
            </div>
            <Button label="리뷰 작성" @click="submitReview" />
        </div>
        <div v-else class="text-red-500">
            리뷰를 작성하려면 로그인이 필요합니다.&nbsp;
            <a href="/auth/login" style="color:rgb(99, 99, 99); text-decoration: underline;">로그인</a>
        </div>

    </div>
    <!-- 리뷰 -->
    <div v-if="instrument" class="card mt-4">
        <div class="flex justify-between items-center mb-4">
            <h2 class="text-2xl font-bold">리뷰</h2>
            <div>
                <Button label="최신순" class="p-button-text" @click="sortReviews('createdAt')" :class="{ 'p-button-outlined p-button-secondary': sortBy === 'createdAt' && sortDirection === 'desc' }" />
                <Button label="좋아요순" class="p-button-text ml-2" @click="sortReviews('likes')" :class="{ 'p-button-outlined p-button-secondary': sortBy === 'likes' }" />
                <Button label="별점순" class="p-button-text ml-2" @click="sortReviews('rating')" :class="{ 'p-button-outlined p-button-secondary': sortBy === 'rating' }" />
                <Button label="오래된순" class="p-button-text ml-2" @click="sortReviews('oldest')" :class="{ 'p-button-outlined p-button-secondary': sortBy === 'createdAt' && sortDirection === 'asc' }" />
            </div>
        </div>
        <div v-if="reviews.length > 0">
            <div v-for="review in reviews" :key="review.id" class="mb-4 p-4 border rounded">
                <div v-if="editingReview !== review.id">
                    <div class="flex justify-between items-center mb-2">
                        <div>
                            <span class="font-bold">{{ review.username }}</span>
                            <Rating v-model="review.rating" :cancel="false" :readonly="true" />
                        </div>
                        <div>
                            {{ formatDate(review.createdAt) }}
                            <span v-if="review.updatedAt">(수정됨: {{ formatDate(review.updatedAt) }})</span>
                        </div>
                    </div>
                    <p>{{ review.comment }}</p>
                    <div class="flex justify-between items-center mt-2">
                        <Button
                            :icon="review.likedByCurrentUser ? 'pi pi-heart-fill' : 'pi pi-heart'"
                            :class="{'p-button-outlined': !review.likedByCurrentUser}"
                            :label="review.likes.toString()"
                            @click="toggleLike(review)"
                        />
                        <div v-if="canEditReview(review)">
                            <Button icon="pi pi-pencil" class="p-button-text" @click="startEditingReview(review)" />
                            <Button icon="pi pi-trash" class="p-button-text p-button-danger" @click="deleteReview(review.id)" />
                        </div>
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
            <Paginator v-if="totalReviews > 10" :rows="10" :totalRecords="totalReviews" @page="onPageChange" :first="currentPage * 10" />
        </div>
        <div v-else class="text-center py-8 text-gray-500 text-xl">
            아직 등록된 리뷰가 없습니다.
        </div>
    </div>
</template>

<style scoped>
.instrument-image-container {
    width: 280px;
    height: 280px;
    overflow: hidden;
    margin-right: 2rem;
}

.instrument-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.instrument-details {
    flex: 1;
    padding-top: 1.2rem;
}

.flex {
    display: flex;
}

.items-center {
    align-items: center;
}

.mb-2 {
    margin-bottom: 0.5rem;
}

.mb-4 {
    margin-bottom: 1rem;
}

.mt-2 {
    margin-top: 0.5rem;
}

.mt-4 {
    margin-top: 1rem;
}

.ml-2 {
    margin-left: 0.5rem;
}

.text-4xl {
    font-size: 2.25rem;
    line-height: 2.5rem;
}

.text-2xl {
    font-size: 1.5rem;
    line-height: 2rem;
}

.text-xl {
    font-size: 1.25rem;
    line-height: 1.75rem;
}

.font-bold {
    font-weight: 700;
}

.p-button {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 0.5rem 1rem;
    font-size: 1rem;
    border-radius: 4px;
    cursor: pointer;
    text-decoration: none;
    transition: background-color 0.2s;
    border: 1px solid transparent;
}

.p-button-success {
    background-color: #22c55e;
    color: white;
}

.p-button-danger {
    background-color: #ef4444;
    color: white;
}

.p-button-warning {
    background-color: #f59e0b;
    color: white;
}

.p-button:hover {
    opacity: 0.9;
}

.mr-2 {
    margin-right: 0.5rem;
}

.mb-2 {
    margin-bottom: 0.5rem;
}

.flex-wrap {
    flex-wrap: wrap;
}

.relative {
    position: relative;
}

.absolute {
    position: absolute;
}

.bottom-2 {
    bottom: 0.5rem;
}

.right-2 {
    right: 0.5rem;
}

.text-sm {
    font-size: 0.875rem;
}

.text-gray-500 {
    color: #6b7280;
}

.w-full {
    width: 100%;
}

.text-center {
    text-align: center;
}

.py-8 {
    padding-top: 2rem;
    padding-bottom: 2rem;
}

.p-button-outlined.p-button-secondary {
    background-color: #f3f4f6;
    border-color: #9ca3af;
    color: #4b5563;
}

.larger-rating :deep(.p-rating-icon) {
    font-size: 2rem;
}

.larger-rating :deep(.p-rating-icon.pi-star-fill) {
    color: #FFA41C;
}
</style>
