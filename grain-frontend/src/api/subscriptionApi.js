import api from './axiosConfig';

export const subscriptionApi = {
    getAll: () => api.get('/subscriptions'),
    getById: (id) => api.get(`/subscriptions/${id}`),
    getActive: () => api.get('/subscriptions/active'),
    getByType: (type) => api.get(`/subscriptions/type/${type}`),
    create: (data) => api.post('/subscriptions', data),
    update: (id, data) => api.put(`/subscriptions/${id}`, data),
    delete: (id) => api.delete(`/subscriptions/${id}`),
    addWorkoutType: (subId, workoutTypeId) =>
        api.post(`/subscriptions/${subId}/workout-types/${workoutTypeId}`),
    removeWorkoutType: (subId, workoutTypeId) =>
        api.delete(`/subscriptions/${subId}/workout-types/${workoutTypeId}`),
};
