<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();

const recentlyReviewedInstruments = ref([]);
const topRatedInstruments = ref([]);
const mostReviewedInstruments = ref([]);

onMounted(async () => {
    try {
        const [recentlyReviewed, topRated, mostReviewed] = await Promise.all([
            axios.get(`${API_URL}/dummy/req/auth/public/dashboard/instruments/recently-reviewed`),
            axios.get(`${API_URL}/dummy/req/auth/public/dashboard/instruments/top-rated`),
            axios.get(`${API_URL}/dummy/req/auth/public/dashboard/instruments/most-reviewed`)
        ]);

        recentlyReviewedInstruments.value = recentlyReviewed.data;
        topRatedInstruments.value = topRated.data;
        mostReviewedInstruments.value = mostReviewed.data;
    } catch (error) {
        // console.error('Error fetching instrument data:', error);
    }
});
</script>

<template>
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="card flex flex-col h-[calc(100vh-340px)]">
            <h2 class="text-2xl font-bold mb-4">Recently Reviewed Gears</h2>
            <div class="space-y-4 overflow-y-auto flex-grow">
                <div v-for="instrument in recentlyReviewedInstruments" :key="instrument.id" class="flex items-center space-x-4">
                    <router-link :to="`/instruments/detail/${instrument.id}`" class="w-[80px] h-[80px] flex-shrink-0">
                        <img :src="instrument.imgUrl" :alt="instrument.model" class="w-full h-full object-contain bg-gray-100 rounded cursor-pointer">
                    </router-link>
                    <div class="flex-grow">
                        <router-link :to="`/instruments/detail/${instrument.id}`" class="block">
                            <h3 class="font-semibold text-lg mb-1 line-clamp-2 hover:text-primary cursor-pointer">{{ instrument.model }}</h3>
                        </router-link>
                        <div class="flex items-center">
                            <Rating :modelValue="instrument.averageRating" readonly :cancel="false" />
                            <span class="ml-2">{{ instrument.averageRating.toFixed(1) }}</span>
                            <span class="ml-2 text-gray-500">({{ instrument.reviewCount }} 리뷰)</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="card flex flex-col h-[calc(100vh-340px)]">
            <h2 class="text-2xl font-bold mb-4">Top Rated Gears</h2>
            <div class="space-y-4 overflow-y-auto flex-grow">
                <div v-for="instrument in topRatedInstruments" :key="instrument.id" class="flex items-center space-x-4">
                    <router-link :to="`/instruments/detail/${instrument.id}`" class="w-[80px] h-[80px] flex-shrink-0">
                        <img :src="instrument.imgUrl" :alt="instrument.model" class="w-full h-full object-contain bg-gray-100 rounded cursor-pointer">
                    </router-link>
                    <div class="flex-grow">
                        <router-link :to="`/instruments/detail/${instrument.id}`" class="block">
                            <h3 class="font-semibold text-lg mb-1 line-clamp-2 hover:text-primary cursor-pointer">{{ instrument.model }}</h3>
                        </router-link>
                        <div class="flex items-center">
                            <Rating :modelValue="instrument.averageRating" readonly :cancel="false" />
                            <span class="ml-2">{{ instrument.averageRating.toFixed(1) }}</span>
                            <span class="ml-2 text-gray-500">({{ instrument.reviewCount }} 리뷰)</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="card flex flex-col h-[calc(100vh-340px)]">
            <h2 class="text-2xl font-bold mb-4">Most Reviewed Gears</h2>
            <div class="space-y-4 overflow-y-auto flex-grow">
                <div v-for="instrument in mostReviewedInstruments" :key="instrument.id" class="flex items-center space-x-4">
                    <router-link :to="`/instruments/detail/${instrument.id}`" class="w-[80px] h-[80px] flex-shrink-0">
                        <img :src="instrument.imgUrl" :alt="instrument.model" class="w-full h-full object-contain bg-gray-100 rounded cursor-pointer">
                    </router-link>
                    <div class="flex-grow">
                        <router-link :to="`/instruments/detail/${instrument.id}`" class="block">
                            <h3 class="font-semibold text-lg mb-1 line-clamp-2 hover:text-primary cursor-pointer">{{ instrument.model }}</h3>
                        </router-link>
                        <div class="flex items-center">
                            <Rating :modelValue="instrument.averageRating" readonly :cancel="false" />
                            <span class="ml-2">{{ instrument.averageRating.toFixed(1) }}</span>
                            <span class="ml-2 text-gray-500">({{ instrument.reviewCount }} 리뷰)</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}
</style>
