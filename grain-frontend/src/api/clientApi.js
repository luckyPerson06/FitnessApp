import api from './axiosConfig';

export const clientApi = {
    getAll: () => api.get('/clients'),
    getById: (id) => api.get(`/clients/${id}`),
    create: (data) => api.post('/clients', data),
    update: (id, data) => api.patch(`/clients/${id}`, data),
    delete: (id) => api.delete(`/clients/${id}`),
    getByLastName: (lastName) => api.get(`/clients/lastname/${lastName}`),
    getByStatus: (status) => api.get(`/clients/status/${status}`),
};
