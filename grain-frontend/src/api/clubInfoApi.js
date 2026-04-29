import api from './axiosConfig';

export const clubInfoApi = {
    get: () => api.get('/club-info'),
    update: (data) => api.put('/admin/club-info', data),
};
