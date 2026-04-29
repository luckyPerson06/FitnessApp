import api from './axiosConfig';

export const visitApi = {
    getAll: () => api.get('/visits'),
    getById: (id) => api.get(`/visits/${id}`),
    getByClient: (clientId) => api.get(`/visits/client/${clientId}`),
    getBySession: (sessionId) => api.get(`/visits/session/${sessionId}`),
    getToday: () => api.get('/visits/today'),
    book: (sessionId, subscriptionId) =>
        api.post('/visits/book', null, { params: { sessionId, subscriptionId } }),
    cancel: (id) => api.patch(`/visits/${id}/cancel`),
    markAttendance: (id, attended) =>
        api.patch(`/visits/${id}/attendance`, null, { params: { attended } }),
    delete: (id) => api.delete(`/visits/${id}`),
};

