import { useState, useCallback } from 'react';
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

const PHONE_MIN_DIGITS = 8;
const PHONE_MAX_DIGITS = 15;
const PASSWORD_MIN_LENGTH = 6;

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

const inputErrorStyle = {
    ...inputStyle,
    border: '1px solid #C87B7B',
    backgroundColor: '#FFF8F8',
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

const stepIndicatorStyle = (active) => ({
    width: '40px',
    height: '4px',
    borderRadius: '2px',
    backgroundColor: active ? '#4D5044' : '#DADCCD',
    transition: 'background-color 0.3s',
});

const errorTextStyle = {
    color: colors.error,
    fontSize: '12px',
    margin: '6px 0 0',
    fontFamily: FONT_BODY,
};

const validatePhone = (phone) => {
    if (!phone) {
        return 'Телефон обязателен';
    }
    if (!phone.startsWith('+')) {
        return 'Номер должен начинаться с +';
    }
    const digitsOnly = phone.replaceAll(/\D/g, '');
    if (digitsOnly.length < PHONE_MIN_DIGITS) {
        return 'Номер слишком короткий';
    }
    if (digitsOnly.length > PHONE_MAX_DIGITS) {
        return 'Номер слишком длинный';
    }
    if (!/^\+[\d\s()-]{7,15}$/.test(phone)) {
        return 'Допустимы только цифры, пробелы, скобки и дефис';
    }
    return null;
};

const validateEmail = (email) => {
    if (!email) {
        return 'Почта обязательна';
    }
    if (!email.includes('@')) {
        return 'Адрес почты должен содержать символ @';
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        return 'Укажите почту в формате anna@kvetka.by';
    }
    return null;
};

const validatePassword = (password) => {
    if (!password) {
        return 'Пароль обязателен';
    }
    if (password.length < PASSWORD_MIN_LENGTH) {
        return `Минимум ${PASSWORD_MIN_LENGTH} символов`;
    }
    return null;
};

const getSubmitButtonText = (step, loading) => {
    if (step === 1) {
        return 'Далее';
    }
    if (loading) {
        return 'Регистрация...';
    }
    return 'Завершить';
};

const StepLabel = ({ label, active }) => (
    <span style={{
        fontSize: '13px',
        fontWeight: active ? '600' : '400',
        color: active ? colors.primaryDark : colors.textMuted,
        fontFamily: FONT_BODY,
        letterSpacing: '0.5px',
        transition: 'color 0.3s',
    }}>
        {label}
    </span>
);

StepLabel.propTypes = {
    label: PropTypes.string.isRequired,
    active: PropTypes.bool.isRequired,
};

const EyeSvg = ({ open }) => (
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
);

EyeSvg.propTypes = {
    open: PropTypes.bool.isRequired,
};

function Register() {
    const [step, setStep] = useState(1);
    const [formData, setFormData] = useState({
        lastName: '',
        firstName: '',
        middleName: '',
        phoneNumber: '',
        email: '',
        password: '',
    });
    const [fieldErrors, setFieldErrors] = useState({});
    const [showPassword, setShowPassword] = useState(false);
    const [serverError, setServerError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { register } = useAuth();

    const clearFieldError = useCallback((name) => {
        setFieldErrors((prev) => {
            if (!prev[name]) return prev;
            const next = { ...prev };
            delete next[name];
            return next;
        });
    }, []);

    const handleChange = useCallback((e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        clearFieldError(name);
        setServerError('');
    }, [clearFieldError]);

    const validateStep = useCallback(() => {
        const errors = {};
        if (step === 1) {
            if (!formData.lastName) errors.lastName = 'Фамилия обязательна';
            if (!formData.firstName) errors.firstName = 'Имя обязательно';
            const phoneError = validatePhone(formData.phoneNumber);
            if (phoneError) errors.phoneNumber = phoneError;
        }
        if (step === 2) {
            const emailError = validateEmail(formData.email);
            if (emailError) errors.email = emailError;
            const passwordError = validatePassword(formData.password);
            if (passwordError) errors.password = passwordError;
        }
        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    }, [step, formData]);

    const handleNext = useCallback((e) => {
        e.preventDefault();
        setServerError('');
        if (validateStep()) setStep(2);
    }, [validateStep]);

    const handleSubmit = useCallback(async (e) => {
        e.preventDefault();
        setServerError('');
        if (!validateStep()) return;
        setLoading(true);
        try {
            await register(formData);
            navigate('/login', { state: { message: 'Регистрация прошла успешно. Войдите в свой аккаунт.' } });
        } catch (err) {
            const message = err.response?.data?.message || err.response?.data?.error || 'Не удалось завершить регистрацию. Попробуйте позже.';
            setServerError(message);
        } finally {
            setLoading(false);
        }
    }, [validateStep, formData, register, navigate]);

    const submitButtonText = getSubmitButtonText(step, loading);

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
                        marginBottom: '4px',
                    }}>
                        Регистрация
                    </h2>

                    <div style={{
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        gap: '12px',
                        marginBottom: 'clamp(16px, 3vw, 24px)',
                    }}>
                        <StepLabel label="Шаг 1" active={step === 1} />
                        <div style={{
                            width: '24px',
                            height: '1px',
                            backgroundColor: colors.borderDefault,
                        }} />
                        <StepLabel label="Шаг 2" active={step === 2} />
                    </div>

                    <div style={{
                        display: 'flex',
                        justifyContent: 'center',
                        gap: '8px',
                        marginBottom: 'clamp(20px, 3vw, 28px)',
                    }}>
                        <div style={stepIndicatorStyle(step >= 1)} />
                        <div style={stepIndicatorStyle(step >= 2)} />
                    </div>

                    <form onSubmit={step === 1 ? handleNext : handleSubmit} noValidate>
                        {step === 1 && (
                            <>
                                <div style={{ marginBottom: '16px' }}>
                                    <label htmlFor="lastName" style={{ display: 'block', marginBottom: '8px', color: colors.textSecondary, fontSize: '14px', fontWeight: '500', fontFamily: FONT_BODY }}>
                                        Фамилия
                                    </label>
                                    <input id="lastName" name="lastName" value={formData.lastName} onChange={handleChange}
                                           placeholder="Иванова" style={fieldErrors.lastName ? inputErrorStyle : inputStyle} />
                                    {fieldErrors.lastName && <p style={errorTextStyle}>{fieldErrors.lastName}</p>}
                                </div>
                                <div style={{ marginBottom: '16px' }}>
                                    <label htmlFor="firstName" style={{ display: 'block', marginBottom: '8px', color: colors.textSecondary, fontSize: '14px', fontWeight: '500', fontFamily: FONT_BODY }}>
                                        Имя
                                    </label>
                                    <input id="firstName" name="firstName" value={formData.firstName} onChange={handleChange}
                                           placeholder="Анна" style={fieldErrors.firstName ? inputErrorStyle : inputStyle} />
                                    {fieldErrors.firstName && <p style={errorTextStyle}>{fieldErrors.firstName}</p>}
                                </div>
                                <div style={{ marginBottom: '16px' }}>
                                    <label htmlFor="middleName" style={{ display: 'block', marginBottom: '8px', color: colors.textSecondary, fontSize: '14px', fontWeight: '500', fontFamily: FONT_BODY }}>
                                        Отчество
                                    </label>
                                    <input id="middleName" name="middleName" value={formData.middleName} onChange={handleChange}
                                           placeholder="Ивановна" style={inputStyle} />
                                </div>
                                <div style={{ marginBottom: '8px' }}>
                                    <label htmlFor="phoneNumber" style={{ display: 'block', marginBottom: '8px', color: colors.textSecondary, fontSize: '14px', fontWeight: '500', fontFamily: FONT_BODY }}>
                                        Телефон
                                    </label>
                                    <input id="phoneNumber" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange}
                                           placeholder="+375 29 123-45-67" style={fieldErrors.phoneNumber ? inputErrorStyle : inputStyle} />
                                    {fieldErrors.phoneNumber && <p style={errorTextStyle}>{fieldErrors.phoneNumber}</p>}
                                </div>
                            </>
                        )}

                        {step === 2 && (
                            <>
                                <div style={{ marginBottom: '16px' }}>
                                    <label htmlFor="email" style={{ display: 'block', marginBottom: '8px', color: colors.textSecondary, fontSize: '14px', fontWeight: '500', fontFamily: FONT_BODY }}>
                                        Почта
                                    </label>
                                    <input id="email" name="email" type="email" value={formData.email} onChange={handleChange}
                                           placeholder="anna@kvetka.by" style={fieldErrors.email ? inputErrorStyle : inputStyle} />
                                    {fieldErrors.email && <p style={errorTextStyle}>{fieldErrors.email}</p>}
                                </div>
                                <div style={{ marginBottom: '8px' }}>
                                    <label htmlFor="password" style={{ display: 'block', marginBottom: '8px', color: colors.textSecondary, fontSize: '14px', fontWeight: '500', fontFamily: FONT_BODY }}>
                                        Пароль
                                    </label>
                                    <div style={{ position: 'relative' }}>
                                        <input id="password" name="password" type={showPassword ? 'text' : 'password'}
                                               value={formData.password} onChange={handleChange}
                                               placeholder={`Минимум ${PASSWORD_MIN_LENGTH} символов`}
                                               autoComplete="new-password"
                                               style={{ ...(fieldErrors.password ? inputErrorStyle : inputStyle), paddingRight: '48px' }} />
                                        <button type="button" onClick={() => setShowPassword((prev) => !prev)}
                                                style={{
                                                    position: 'absolute', right: '14px', top: '50%',
                                                    transform: 'translateY(-50%)', background: 'none',
                                                    border: 'none', cursor: 'pointer', padding: '4px', lineHeight: 0,
                                                }}
                                                aria-label={showPassword ? 'Скрыть пароль' : 'Показать пароль'}>
                                            <EyeSvg open={showPassword} />
                                        </button>
                                    </div>
                                    {fieldErrors.password && <p style={errorTextStyle}>{fieldErrors.password}</p>}
                                </div>
                            </>
                        )}

                        {serverError && (
                            <div style={{
                                backgroundColor: '#FFF0F0',
                                border: `1px solid ${colors.error}`,
                                borderRadius: '10px',
                                padding: '14px 18px',
                                marginTop: '16px',
                                color: colors.error,
                                fontSize: '14px',
                                textAlign: 'center',
                                fontFamily: FONT_BODY,
                            }}>
                                {serverError}
                            </div>
                        )}

                        <div style={{ display: 'flex', gap: '12px', marginTop: serverError ? '16px' : '28px' }}>
                            {step === 2 && (
                                <button type="button" onClick={() => { setStep(1); setServerError(''); }}
                                        style={{
                                            flex: 1, padding: '15px', fontSize: '15px', fontWeight: '500',
                                            color: colors.textSecondary, backgroundColor: 'transparent',
                                            border: `1px solid ${colors.borderDefault}`, borderRadius: '12px',
                                            cursor: 'pointer', fontFamily: FONT_BODY,
                                        }}>
                                    Назад
                                </button>
                            )}
                            <button type="submit" disabled={loading} style={{ ...btnStyle(loading), flex: 1, opacity: loading ? 0.5 : 1 }}>
                                {submitButtonText}
                            </button>
                        </div>
                    </form>

                    <p style={{
                        textAlign: 'center',
                        marginTop: 'clamp(20px, 3vw, 28px)',
                        color: colors.textSecondary,
                        fontSize: '15px',
                        fontFamily: FONT_BODY,
                    }}>
                        Уже есть аккаунт?{' '}
                        <Link to="/login" style={{
                            color: colors.primaryDark,
                            textDecoration: 'none',
                            fontWeight: '500',
                        }}>
                            Войти
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}

export default Register;
