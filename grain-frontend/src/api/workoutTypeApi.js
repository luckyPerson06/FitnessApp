import api from './axiosConfig';

export const workoutTypeApi = {
    getAll: () => api.get('/workout-types'),
    getById: (id) => api.get(`/workout-types/${id}`),
    getByName: (name) => api.get(`/workout-types/name/${name}`),
    getActive: () => api.get('/workout-types/active'),
    getByCategory: (category) => api.get(`/workout-types/category/${category}`),
    getByTrainer: (trainerId) => api.get(`/workout-types/trainer/${trainerId}`),
    create: (data) => api.post('/workout-types', data),
    update: (id, data) => api.put(`/workout-types/${id}`, data),
    deactivate: (id) => api.patch(`/workout-types/${id}/deactivate`),
    delete: (id) => api.delete(`/workout-types/${id}`),
    getTrainers: (id) => api.get(`/workout-types/${id}/trainers`),
};

