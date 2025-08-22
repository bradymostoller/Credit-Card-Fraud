import React, { createContext, useContext, useEffect, useState } from 'react';
import { jwtDecode } from 'jwt-decode';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(localStorage.getItem('token'));
    const [loading, setLoading] = useState(true);

    const decodeToken = (jwt) => {
        try {
            const decoded = jwtDecode(jwt);
            return {
                email: decoded.sub,
                role: decoded.role,
            };
        } catch(err) {
            console.error('Failed to decode token:', err);
            return null;
        }
    };

    useEffect(() => {
        const storedToken = localStorage.getItem('token');
        if (storedToken) {
            const decoded = decodeToken(storedToken);
            if (decoded) {
                setToken(storedToken);
                setUser(decoded);
            }
        }
        setLoading(false);
    }, []);

    const login = async (email, password) => {
        try{
            const response = await fetch('http://localhost:8346/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json'},
                body: JSON.stringify({ email, password }),
            });

            if (!response.ok) throw new Error('Invalid credentials');
            const { token } = await response.json();
            const decoded = decodeToken(token);
            if (!decoded) throw new Error('Invalid Token');

            localStorage.setItem('token', token);
            setToken(token);
            setUser(decoded);
            return { success: true };
        } catch (error) {
            return { success: false, error: error.message }
        }
    };

    const register = async (name, email, password) => {
        try{
            const response = await fetch('http://localhost:8346/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json'},
                body: JSON.stringify({ name, email, password }),
            });

            if (!response.ok) throw new Error(await response.text() || 'Registration failed');
            const { token } = await response.json();
            const decoded = decodeToken(token);
            if (!decoded) throw new Error('Invalid token');

            localStorage.setItem('token', token);
            setToken(token);
            setUser(decoded);
            return { success: true };
        } catch (error) {
            return { success: false, error: error.message };
        }
    };

    const logout = () => {
        localStorage.removeItem('token');
        setToken(null);
        setUser(null);
    };

    const value = {
        user,
        token,
        login,
        register,
        logout,
        isAuthenticated: !!token,
        isAdmin: user?.role == 'ADMIN',
    };

    return (
        <AuthContext.Provider value={value}>
            {!loading && children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within an AuthProvider');
    return context;
};