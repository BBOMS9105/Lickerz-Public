import axiosInterceptor from '@/utils/axiosInterceptor';

const API_URL = `${API_URL}/dummy/req/auth`;

export const InstrumentService = {
    getInstruments() {
        return axiosInterceptor.get(`${API_URL}/instruments`);
    },
    addInstrument(formData) {
        return axiosInterceptor.post(`${API_URL}/instruments/add`, formData,
            {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            }
        );
    },
    updateInstrument(formData) {
        return axiosInterceptor.put(`${API_URL}/instruments/update`, formData,
            {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            }
        );
    },
    deleteInstrument(id) {
        return axiosInterceptor.delete(`${API_URL}/instruments/delete/${id}`);

    }
};
