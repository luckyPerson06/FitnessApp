import api from './axiosConfig';

export const adminApi = {
    getVisitsByDate: (date) => api.get('/admin/visits/by-date', { params: { date } }),
    getAttendanceStats: (sessionId) => api.get(`/admin/sessions/${sessionId}/attendance-stats`),
};
