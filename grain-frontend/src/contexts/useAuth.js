import { useContext } from 'react';
import { AuthContext } from './AuthContext';

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within AuthProvider');
    }
    return context;
};

export const useIsAdmin = () => {
    const { user } = useAuth();
    return user?.role === 'ADMIN';
};

export const useIsClient = () => {
    const { user } = useAuth();
    return user?.role === 'CLIENT';
};
