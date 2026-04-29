import api from './axiosConfig';

export const profileApi = {
    getProfile: () => api.get('/profile'),
    getUpcomingVisits: () => api.get('/profile/visits/upcoming'),
    getVisitHistory: () => api.get('/profile/visits/history'),
    cancelVisit: (visitId) => api.post(`/profile/visits/${visitId}/cancel`),
};
