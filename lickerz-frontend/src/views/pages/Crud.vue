<script setup>
import { InstrumentService } from '@/service/InstrumentService';
import { FilterMatchMode } from '@primevue/core/api';
import { useToast } from 'primevue/usetoast';
import { onMounted, ref } from 'vue';
import Rating from 'primevue/rating';
import { useUserStore } from '@/store/userStore';
import axiosInstance from '@/utils/axiosInterceptor';

const userStore = useUserStore();

onMounted(() => {
    loadInstruments();
});

const toast = useToast();
const dt = ref();
const instruments = ref([]);
const instrumentDialog = ref(false);
const deleteInstrumentDialog = ref(false);
const deleteInstrumentsDialog = ref(false);
const instrument = ref({});
const selectedInstruments = ref();
const filters = ref({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS }
});
const submitted = ref(false);
const imageFile = ref(null);

// 악기 목록 로드
async function loadInstruments() {
    try {
        const response = await axiosInstance.get(`${API_URL}/dummy/req/auth/public/instruments`, {
            headers: {
                'Authorization': `Bearer ${userStore.accessToken}`
            }
        });
        // console.log('Full response:', response);

        if (Array.isArray(response.data)) {
            instruments.value = response.data.map(item => ({
                ...item,
                name: item.model,
                rating: item.averageRating
            }));
        } else {
            // console.error('Unexpected data format:', response.data);
            instruments.value = [];
        }
    } catch (error) {
        // console.error('Error loading instruments:', error);
        if (error.response) {
            // console.error('Response status:', error.response.status);
            // console.error('Response data:', error.response.data);
        }
        toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to load instruments', life: 3000 });
        instruments.value = [];
    }
}

// 이미지 업로드
function onImageUpload(event) {
    if (event.files && event.files[0]) {
        const file = event.files[0];
        imageFile.value = file;
        const reader = new FileReader();
        reader.onload = (e) => {
            instrument.value.imgUrl = e.target.result;
        };
        reader.readAsDataURL(file);
    }
}

// 새 악기 추가
function openNew() {
    instrument.value = {
        name: '',
        type: '',
        brand: '',
        imgUrl: '',
        rating: 0,
        reviewCount: 0
    };
    imageFile.value = null;
    submitted.value = false;
    instrumentDialog.value = true;
}

// 대화 상자 숨기기
function hideDialog() {
    instrumentDialog.value = false;
    submitted.value = false;
}

// 악기 저장
async function saveInstrument() {
    submitted.value = true;

    if (instrument.value.name?.trim()) {
        try {
            const formData = new FormData();
            formData.append('model', instrument.value.name);
            formData.append('type', instrument.value.type);
            formData.append('brand', instrument.value.brand);
            if (imageFile.value) {
                formData.append('file', imageFile.value);
            }

            // console.log('FormData contents:');
            for (let [key, value] of formData.entries()) {
                // console.log(key, value);
            }

            // console.log('Current access token:', userStore.accessToken);
            // console.log('Request headers:', axiosInstance.defaults.headers);

            if (instrument.value.id) {
                // Update existing instrument
                formData.append('id', instrument.value.id);
                await updateInstrument(formData);
            } else {
                // Create new instrument
                await InstrumentService.addInstrument(formData);
                toast.add({ severity: 'success', summary: 'Successful', detail: 'Instrument Created', life: 3000 });
            }
            await loadInstruments();
            instrumentDialog.value = false;
            instrument.value = {};
            imageFile.value = null;
        } catch (error) {
            // console.error('Error saving instrument:', error);
            toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to save instrument', life: 3000 });
        }
    }
}

// 악기 편집
function editInstrument(inst) {
    instrument.value = { ...inst };
    imageFile.value = null; // 이미지 파일 초기화
    instrumentDialog.value = true;
}

// 악기 업데이트
async function updateInstrument(formData) {
    try {
        await InstrumentService.updateInstrument(formData);
        toast.add({ severity: 'success', summary: 'Successful', detail: 'Instrument Updated', life: 3000 });
        await loadInstruments();
        instrumentDialog.value = false;
    } catch (error) {
        // console.error('Error updating instrument:', error);
        toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to update instrument', life: 3000 });
    }
}

// 악기 삭제 확인
function confirmDeleteInstrument(inst) {
    instrument.value = inst;
    deleteInstrumentDialog.value = true;
}

// 악기 삭제
async function deleteInstrument() {
    try {
        await InstrumentService.deleteInstrument(instrument.value.id);
        await loadInstruments();
        deleteInstrumentDialog.value = false;
        instrument.value = {};
        toast.add({ severity: 'success', summary: 'Successful', detail: '악기가 삭제되었습니다.', life: 3000 });
    } catch (error) {
        // console.error('Error deleting instrument:', error);
        toast.add({ severity: 'error', summary: 'Error', detail: '악기 삭제에 실패했습니다.', life: 3000 });
    }
}

// CSV 내보내기
function exportCSV() {
    dt.value.exportCSV();
}

// 선택된 악기 삭제 확인
function confirmDeleteSelected() {
    deleteInstrumentsDialog.value = true;
}

// 선택된 악기 삭제
async function deleteSelectedInstruments() {
    try {
        for (let instrument of selectedInstruments.value) {
            await InstrumentService.deleteInstrument(instrument.id);
        }
        await loadInstruments();
        deleteInstrumentsDialog.value = false;
        selectedInstruments.value = null;
        toast.add({ severity: 'success', summary: 'Successful', detail: 'Instruments Deleted', life: 3000 });
    } catch (error) {
        // console.error('Error deleting selected instruments:', error);
        toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to delete selected instruments', life: 3000 });
    }
}

// 이미지 로드 실패 시 대체 이미지 설정
function handleImageError(event) {
    event.target.src = '/path/to/fallback/image.png'; // 대체 이미지 경로
}
</script>

<template>
    <div>
        <div class="card">
            <Toolbar class="mb-6">
                <template #start>
                    <Button label="New" icon="pi pi-plus" severity="secondary" class="mr-2" @click="openNew" />
                    <Button label="Delete" icon="pi pi-trash" severity="secondary" @click="confirmDeleteSelected" :disabled="!selectedInstruments || !selectedInstruments.length" />
                </template>

                <template #end>
                    <Button label="Export" icon="pi pi-upload" severity="secondary" @click="exportCSV($event)" />
                </template>
            </Toolbar>

            <DataTable
                ref="dt"
                v-model:selection="selectedInstruments"
                :value="instruments"
                dataKey="id"
                :paginator="true"
                :rows="10"
                :filters="filters"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                :rowsPerPageOptions="[5, 10, 25]"
                currentPageReportTemplate="Showing {first} to {last} of {totalRecords} instruments"
                :loading="instruments.length === 0"
            >
                <template #header>
                    <div class="flex flex-wrap gap-2 items-center justify-between">
                        <h4 class="m-0" style="font-size: 20px; font-weight: bold;">악기 관리</h4>
                        <IconField>
                            <InputIcon>
                                <i class="pi pi-search" />
                            </InputIcon>
                            <InputText v-model="filters['global'].value" placeholder="Search..." />
                        </IconField>
                    </div>
                </template>

                <Column selectionMode="multiple" style="width: 3rem" :exportable="false"></Column>
                <Column field="name" header="이름" sortable style="min-width: 16rem"></Column>
                <Column header="이미지" style="width: 100px">
                    <template #body="slotProps">
                        <img :src="slotProps.data.imgUrl" :alt="slotProps.data.name"
                             style="width: 100px; height: auto;"
                             @error="handleImageError" />
                    </template>
                </Column>
                <Column field="type" header="분류" sortable style="min-width: 10rem"></Column>
                <Column field="brand" header="브랜드" sortable style="min-width: 10rem"></Column>
                <Column field="rating" header="평점" sortable style="min-width: 8rem">
                    <template #body="slotProps">
                        <div class="flex items-center">
                            <span class="rating-number">{{ slotProps.data.rating.toFixed(1) }}</span>
                            <Rating v-model="slotProps.data.rating" :cancel="false" :readonly="true" />
                        </div>
                    </template>
                </Column>
                <Column field="reviewCount" header="리뷰 수" sortable style="min-width: 8rem"></Column>
                <Column :exportable="false" style="min-width: 12rem">
                    <template #body="slotProps">
                        <Button icon="pi pi-pencil" outlined rounded class="mr-2" @click="editInstrument(slotProps.data)" />
                        <Button icon="pi pi-trash" outlined rounded severity="danger" @click="confirmDeleteInstrument(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>

            <div v-if="instruments.length === 0" class="text-center p-4">
                No instruments found.
            </div>
        </div>

        <Dialog v-model:visible="instrumentDialog" :style="{ width: '450px' }" header="Instrument Details" :modal="true">
            <div class="flex flex-col gap-6">
                <div>
                    <label for="name" class="block font-bold mb-3">Name</label>
                    <InputText id="name" v-model.trim="instrument.name" required="true" autofocus :invalid="submitted && !instrument.name" fluid />
                    <small v-if="submitted && !instrument.name" class="text-red-500">Name is required.</small>
                </div>
                <div>
                    <label for="type" class="block font-bold mb-3">Type</label>
                    <InputText id="type" v-model="instrument.type" required="true" fluid />
                </div>
                <div>
                    <label for="brand" class="block font-bold mb-3">Brand</label>
                    <InputText id="brand" v-model="instrument.brand" required="true" fluid />
                </div>
                <div>
                    <label for="image" class="block font-bold mb-3">Image</label>
                    <FileUpload
                        mode="basic"
                        name="file"
                        accept="image/*"
                        :maxFileSize="1000000"
                        @select="onImageUpload"
                        :auto="false"
                        chooseLabel="Choose Image"
                    />
                </div>
                <div v-if="instrument.imgUrl">
                    <img :src="instrument.imgUrl" alt="Instrument Image" style="max-width: 100%; height: auto;" />
                </div>
            </div>

            <template #footer>
                <Button label="Cancel" icon="pi pi-times" text @click="hideDialog" />
                <Button label="Save" icon="pi pi-check" @click="saveInstrument" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteInstrumentDialog" :style="{ width: '450px' }" header="Confirm" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="instrument"
                    >정말 <b>{{ instrument.name }}을/를 삭제하시겠습니까?</b
                    >?</span
                >
            </div>
            <template #footer>
                <Button label="No" icon="pi pi-times" text @click="deleteInstrumentDialog = false" />
                <Button label="Yes" icon="pi pi-check" @click="deleteInstrument" />
            </template>
        </Dialog>

        <Dialog v-model:visible="deleteInstrumentsDialog" :style="{ width: '450px' }" header="Confirm" :modal="true">
            <div class="flex items-center gap-4">
                <i class="pi pi-exclamation-triangle !text-3xl" />
                <span v-if="instrument">Are you sure you want to delete the selected instruments?</span>
            </div>
            <template #footer>
                <Button label="No" icon="pi pi-times" text @click="deleteInstrumentsDialog = false" />
                <Button label="Yes" icon="pi pi-check" text @click="deleteSelectedInstruments" />
            </template>
        </Dialog>
    </div>
</template>

<style scoped>
.p-datatable .p-datatable-tbody > tr > td {
    vertical-align: middle;
}

.rating-number {
    width: 40px;
    text-align: right;
    margin-right: 6px;
}

:deep(.p-rating) {
    font-size: 1.2rem;
}

:deep(.p-rating .p-rating-item.p-rating-item-active .p-rating-icon) {
    color: #FFA41C;
}
</style>
