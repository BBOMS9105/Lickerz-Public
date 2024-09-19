<script setup>
import { ref, onMounted, watch, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import Breadcrumb from 'primevue/breadcrumb';

const route = useRoute();
const router = useRouter();
const instruments = ref([]);
const loading = ref(true);

// 악기 데이터 로드
const fetchInstruments = async () => {
    loading.value = true;
    try {
        const response = await axios.get(`https://lickerz.duckdns.org/api/public/instruments/list/${route.params.type}/${route.params.brand}`, {
            withCredentials: false
        });
        instruments.value = response.data;
    } catch (error) {
        // console.error('Error fetching instruments:', error);
    } finally {
        loading.value = false;
    }
};

const goToDetail = (instrumentId) => {
    router.push(`/instruments/detail/${instrumentId}`);
};

const breadcrumbHome = { icon: 'pi pi-home', to: '/' };
const breadcrumbItems = computed(() => [
    { label: route.params.type, to: `/instruments/${route.params.type}` },
    { label: route.params.brand, to: `/instruments/${route.params.type}/${route.params.brand}` }
]);

onMounted(fetchInstruments);

watch(() => route.params, fetchInstruments);
</script>

<template>
    <div class="grid">
        <div class="col-12">
            <div class="card">
                <Breadcrumb :home="breadcrumbHome" :model="breadcrumbItems" />
                <h1 class="text-4xl font-bold mb-4">{{ route.params.brand }}</h1>
                <div v-if="loading">Loading...</div>
                <div v-else class="instruments-container">
                    <div v-for="instrument in instruments" :key="instrument.id" class="instrument-card">
                        <div @click="goToDetail(instrument.id)" class="cursor-pointer">
                            <div class="image-container">
                                <img :src="instrument.imgUrl" :alt="instrument.model" class="instrument-image" />
                            </div>
                            <div class="p-3">
                                <div class="text-lg font-bold mb-2 instrument-name">{{ instrument.model }}</div>
                                <div class="flex items-center">
                                    <Rating v-model="instrument.averageRating" :cancel="false" :readonly="true" />
                                    <span class="ml-2 text-sm">{{ instrument.averageRating.toFixed(1) }}</span>
                                    <span class="ml-2 text-sm">({{ instrument.reviewCount }})</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.instruments-container {
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-start;
    gap: 20px;
    overflow-x: auto;
    padding: 20px 0;
}

.instrument-card {
    flex: 0 0 auto;
    width: 220px;
    border: 1px solid var(--surface-border);
    border-radius: 6px;
    overflow: hidden;
    background-color: var(--surface-card);
    transition: transform 0.3s ease-in-out;
}

.instrument-card:hover {
    transform: scale(1.03);
}

.image-container {
    width: 200px;
    height: 200px;
    overflow: hidden;
    display: flex;
    justify-content: center;
    align-items: center;
    margin: 10px auto 0;
}

.instrument-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.instrument-name {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
    height: 2.5em; /* Adjust this value based on your font size and line height */
    line-height: 1.2em;
}
</style>
