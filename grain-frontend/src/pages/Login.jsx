import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import PropTypes from 'prop-types';

const FONT_HEADING = "'Cormorant Garamond', 'Times New Roman', serif";
const FONT_BODY = "'Inter', 'Segoe UI', sans-serif";

const colors = {
    primary: '#979B81',
    primaryDark: '#4D5044',
    primaryHover: '#7E8A6A',
    bgMain: '#DADCCD',
    backgroundCard: 'rgba(255,255,255,0.9)',
    textPrimary: '#0F0F10',
    textSecondary: '#5F6256',
    textMuted: '#6B6F64',
    textOnPrimary: '#FFFFFF',
    borderDefault: '#DADCCD',
    error: '#C87B7B',
    shadowSoft: 'rgba(0,0,0,0.04)',
    shadowMedium: 'rgba(0,0,0,0.1)',
};

const inputStyle = {
    width: '100%',
    padding: '14px 16px',
    fontSize: '15px',
    border: '1px solid #DADCCD',
    borderRadius: '12px',
    outline: 'none',
    boxSizing: 'border-box',
    backgroundColor: '#FAFAF8',
    fontFamily: FONT_BODY,
};

const btnStyle = (loading) => ({
    width: '100%',
    padding: '15px',
    fontSize: '15px',
    fontWeight: '500',
    letterSpacing: '0.3px',
    color: '#FFFFFF',
    backgroundColor: '#4D5044',
    border: 'none',
    borderRadius: '12px',
    cursor: loading ? 'not-allowed' : 'pointer',
    opacity: loading ? 0.7 : 1,
    fontFamily: FONT_BODY,
});

const EyeIcon = ({ open, onClick }) => (
    <button
        type="button"
        onClick={onClick}
        aria-label={open ? 'Скрыть пароль' : 'Показать пароль'}
        style={{
            position: 'absolute',
            right: '14px',
            top: '50%',
            transform: 'translateY(-50%)',
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            padding: '4px',
            lineHeight: 0,
        }}
    >
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#8C8F84" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
            {open ? (
                <>
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <circle cx="12" cy="12" r="3" />
                </>
            ) : (
                <>
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <line x1="1" y1="1" x2="23" y2="23" strokeWidth="2" />
                </>
            )}
        </svg>
    </button>
);

EyeIcon.propTypes = {
    open: PropTypes.bool.isRequired,
    onClick: PropTypes.func.isRequired,
};

function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { login } = useAuth();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            const user = await login(email, password);
            navigate('/');
        } catch (err) {
            setError(err.response?.data?.message || 'Неверный email или пароль');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{
            minHeight: '100vh',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontFamily: FONT_HEADING,
            position: 'relative',
            overflow: 'hidden',
            margin: 0,
            padding: 0,
        }}>
            <div style={{
                position: 'fixed',
                inset: 0,
                backgroundColor: colors.bgMain,
                zIndex: 0,
            }} />

            <div style={{
                position: 'fixed',
                inset: 0,
                backgroundImage: 'url("/images/flowers/flower.png")',
                backgroundSize: 'cover',
                backgroundRepeat: 'no-repeat',
                backgroundPosition: 'center',
                opacity: 0.35,
                zIndex: 1,
                pointerEvents: 'none',
            }} />

            <div style={{ position: 'relative', zIndex: 2, width: '100%', maxWidth: '440px', padding: '16px' }}>
                <div style={{
                    backgroundColor: colors.backgroundCard,
                    borderRadius: '24px',
                    padding: 'clamp(32px, 5vw, 48px) clamp(24px, 4vw, 40px)',
                    boxShadow: `0 20px 60px ${colors.shadowSoft}, 0 4px 16px ${colors.shadowMedium}`,
                    backdropFilter: 'blur(10px)',
                    WebkitBackdropFilter: 'blur(10px)',
                }}>
                    <div style={{ textAlign: 'center', marginBottom: 'clamp(24px, 4vw, 36px)' }}>
                        <h1 style={{
                            fontSize: 'clamp(36px, 8vw, 48px)',
                            fontWeight: '300',
                            color: colors.primary,
                            letterSpacing: '10px',
                            margin: '0 0 8px 0',
                        }}>
                            KVETKA
                        </h1>
                        <p style={{
                            color: colors.textMuted,
                            fontSize: 'clamp(12px, 2.5vw, 14px)',
                            letterSpacing: '5px',
                            textTransform: 'uppercase',
                            margin: '0',
                            fontFamily: FONT_BODY,
                        }}>
                            Студия фитнеса
                        </p>
                    </div>

                    <h2 style={{
                        fontSize: 'clamp(22px, 5vw, 28px)',
                        fontWeight: '400',
                        color: colors.textMuted,
                        textAlign: 'center',
                        marginBottom: '10px',
                    }}>
                        Добро пожаловать
                    </h2>
                    <p style={{
                        color: colors.textSecondary,
                        textAlign: 'center',
                        fontSize: 'clamp(15px, 3vw, 17px)',
                        marginBottom: 'clamp(28px, 5vw, 36px)',
                        fontFamily: FONT_BODY,
                        lineHeight: 1.6,
                        letterSpacing: '0.2px',
                    }}>
                        Ваше пространство для тела и души
                    </p>

                    {error && (
                        <div style={{
                            backgroundColor: '#FFF0F0',
                            border: `1px solid ${colors.error}`,
                            borderRadius: '10px',
                            padding: '14px 18px',
                            marginBottom: '20px',
                            color: colors.error,
                            fontSize: '14px',
                            textAlign: 'center',
                            fontFamily: FONT_BODY,
                        }}>
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleSubmit}>
                        <div style={{ marginBottom: 'clamp(16px, 3vw, 20px)' }}>
                            <label htmlFor="email" style={{
                                display: 'block',
                                marginBottom: '8px',
                                color: colors.textSecondary,
                                fontSize: '14px',
                                fontWeight: '500',
                                fontFamily: FONT_BODY,
                            }}>
                                Почта
                            </label>
                            <input
                                id="email"
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                placeholder="your@kvetka.by"
                                style={inputStyle}
                            />
                        </div>

                        <div style={{ marginBottom: 'clamp(24px, 4vw, 32px)' }}>
                            <label htmlFor="password" style={{
                                display: 'block',
                                marginBottom: '8px',
                                color: colors.textSecondary,
                                fontSize: '14px',
                                fontWeight: '500',
                                fontFamily: FONT_BODY,
                            }}>
                                Пароль
                            </label>
                            <div style={{ position: 'relative' }}>
                                <input
                                    id="password"
                                    type={showPassword ? 'text' : 'password'}
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                    placeholder="••••••••"
                                    style={{ ...inputStyle, paddingRight: '48px' }}
                                />
                                <EyeIcon open={showPassword} onClick={() => setShowPassword(!showPassword)} />
                            </div>
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            style={btnStyle(loading)}
                        >
                            {loading ? 'Вход...' : 'Войти'}
                        </button>
                    </form>

                    <p style={{
                        textAlign: 'center',
                        marginTop: 'clamp(20px, 3vw, 28px)',
                        color: colors.textSecondary,
                        fontSize: '15px',
                        fontFamily: FONT_BODY,
                    }}>
                        Нет аккаунта?{' '}
                        <Link to="/register" style={{
                            color: colors.primaryDark,
                            textDecoration: 'none',
                            fontWeight: '500',
                        }}>
                            Зарегистрироваться
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}

export default Login;
