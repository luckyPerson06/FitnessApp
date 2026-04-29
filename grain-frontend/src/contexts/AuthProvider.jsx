import { useState, useCallback } from 'react';
import { AuthContext } from './AuthContext';
import { authApi } from '../api/authApi';

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(() => {
        const storedUser = localStorage.getItem('user');
        return storedUser ? JSON.parse(storedUser) : null;
    });

    const [token, setToken] = useState(() => {
        return localStorage.getItem('token');
    });

    const [loading] = useState(false);

    const login = useCallback(async (email, password) => {
        const response = await authApi.login(email, password);
        const { token, email: userEmail, role, displayName, clientId } = response.data;

        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify({ email: userEmail, role, displayName, clientId }));

        setToken(token);
        setUser({ email: userEmail, role, displayName, clientId });

        return response.data;
    }, []);

    const register = useCallback(async (data) => {
        const response = await authApi.register(data);
        return response.data;
    }, []);

    const logout = useCallback(() => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setToken(null);
        setUser(null);
    }, []);

    const value = {
        user,
        token,
        loading,
        login,
        register,
        logout,
        isAuthenticated: !!token,
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};
