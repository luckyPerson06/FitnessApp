import api from './axiosConfig';

export const trainerApi = {
    getAll: () => api.get('/trainers'),
    getById: (id) => api.get(`/trainers/${id}`),
    getActive: () => api.get('/trainers/active'),
    getByDay: (dayOfWeek) => api.get(`/trainers/day/${dayOfWeek}`),
    create: (data) => api.post('/trainers', data),
    update: (id, data) => api.put(`/trainers/${id}`, data),
    delete: (id) => api.delete(`/trainers/${id}`),
    addSpecialization: (trainerId, workoutTypeId) =>
        api.post(`/trainers/${trainerId}/specializations/${workoutTypeId}`),
    removeSpecialization: (trainerId, workoutTypeId) =>
        api.delete(`/trainers/${trainerId}/specializations/${workoutTypeId}`),
};