import api from './axiosConfig';

export const sessionApi = {
    getAll: () => api.get('/sessions'),
    getById: (id) => api.get(`/sessions/${id}`),
    getByDate: (date) => api.get(`/sessions/date/${date}`),
    getByDateRange: (from, to) => api.get(`/sessions/date-range`, { params: { from, to } }),
    getByDay: (dayOfWeek) => api.get(`/sessions/day/${dayOfWeek}`),
    getByTrainer: (trainerId) => api.get(`/sessions/trainer/${trainerId}`),
    getToday: () => api.get('/sessions/today'),
    getBookedCount: (id) => api.get(`/sessions/${id}/booked-count`),
    hasAvailableSpots: (id) => api.get(`/sessions/${id}/available-spots`),
    create: (data) => api.post('/sessions', data),
    update: (id, data) => api.put(`/sessions/${id}`, data),
    updateStatus: (id, status) => api.patch(`/sessions/${id}/status`, null, { params: { status } }),
    delete: (id) => api.delete(`/sessions/${id}`),
    getUpcomingByWorkoutType: (workoutTypeId) => api.get(`/sessions/workout-type/${workoutTypeId}/upcoming`),
};
