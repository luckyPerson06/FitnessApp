import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { sessionApi } from '../api/sessionApi';
import { trainerApi } from '../api/trainerApi';
import { workoutTypeApi } from '../api/workoutTypeApi';
import { useAuth } from '../contexts/useAuth';

const FONT_HEADING = "'Cormorant Garamond', 'Times New Roman', serif";
const FONT_BODY = "'Inter', 'Segoe UI', sans-serif";

const colors = {
    primary: '#979B81', primaryDark: '#4D5044', primaryHover: '#7E8A6A',
    primaryLight: '#E4E7D6',
    textPrimary: '#0F0F10', textSecondary: '#5F6256', textMuted: '#8C8F84',
    textOnPrimary: '#FFFFFF',
    error: '#C87B7B', success: '#7FA37A',
    glassBorder: 'rgba(180,180,170,0.5)',
    backgroundCard: 'rgba(255,255,255,0.75)',
    shadowCard: '0 2px 16px rgba(77,80,68,0.06)',
    shadowHover: '0 6px 28px rgba(77,80,68,0.12)',
    bookedLow: '#C8D5C0', bookedMid: '#AEB79C', bookedHigh: '#979B81',
    bookedFull: '#C87B7B',
    pastBg: 'rgba(245,245,240,0.6)',
    pastText: '#B0B3A8',
    fullBg: 'rgba(250,245,245,0.6)',
};

const SPACING = { xs: '4px', sm: '12px', md: '20px', lg: '32px' };
const DAYS = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
const COLOR_OPTIONS = [
    '#979B81', '#4D5044', '#D6BFA6', '#C8D5C0', '#AEB79C',
    '#D9A86C', '#C87B7B', '#7FA37A', '#8FA7A3', '#B9BCA6',
];
const emptyForm = {
    trainerId: '', workoutTypeId: '', sessionDate: '',
    startTime: '09:00', endTime: '10:00',
    maxParticipants: 12, colorCode: '#979B81', room: '',
};

const inputStyle = {
    width: '100%', padding: '14px 16px', fontSize: '15px',
    fontFamily: FONT_BODY, border: '1px solid rgba(180,180,170,0.5)',
    borderRadius: '10px', outline: 'none', backgroundColor: '#FAFAF8',
    color: colors.textPrimary, boxSizing: 'border-box',
};

const inputErrorStyle = {
    ...inputStyle,
    border: '1px solid #C87B7B', backgroundColor: '#FFF8F8',
};

const errorTextStyle = {
    color: colors.error, fontSize: '12px',
    margin: '4px 0 0', fontFamily: FONT_BODY,
};

const dropdownBtnStyle = (open) => ({
    width: '100%', padding: '14px 16px', fontSize: '15px',
    fontFamily: FONT_BODY, fontWeight: '500', textAlign: 'left',
    border: '1px solid rgba(180,180,170,0.5)',
    borderRadius: open ? '10px 10px 0 0' : '10px',
    outline: 'none', backgroundColor: '#FAFAF8',
    color: colors.textPrimary, cursor: 'pointer',
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    boxSizing: 'border-box',
});

const dropdownOptStyle = (active) => ({
    width: '100%', padding: '14px 16px', fontSize: '15px',
    fontFamily: FONT_BODY, fontWeight: active ? '600' : '400',
    color: active ? colors.primaryDark : colors.textSecondary,
    backgroundColor: active ? colors.primaryLight : 'white',
    border: 'none', cursor: 'pointer', textAlign: 'left',
    transition: 'background-color 0.15s ease',
});

const PlusIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" strokeWidth="2"
         strokeLinecap="round" strokeLinejoin="round">
        <line x1="12" y1="5" x2="12" y2="19" />
        <line x1="5" y1="12" x2="19" y2="12" />
    </svg>
);

const formatDate = (d) => {
    if (!d) return '';
    const dt = new Date(d);
    return dt.toLocaleDateString('ru-RU', {
        day: 'numeric', month: 'long', weekday: 'short',
    });
};

const formatTime = (t) => t ? t.slice(0, 5) : '';

const toLocalDateString = (date) => {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
};

const parseLocalDate = (dateStr) => {
    if (!dateStr) return null;
    const [y, m, d] = dateStr.split('-').map(Number);
    return new Date(y, m - 1, d);
};

function Schedule() {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const getMonday = (d) => {
        const n = new Date(d);
        n.setDate(n.getDate() - n.getDay() + 1);
        return toLocalDateString(n);
    };

    const todayMonday = getMonday(today);
    const maxMonday = (() => {
        const d = new Date(todayMonday);
        d.setDate(d.getDate() + 21);
        return toLocalDateString(d);
    })();

    const [weekStart, setWeekStart] = useState(todayMonday);
    const [selectedDate, setSelectedDate] = useState(
        toLocalDateString(today)
    );
    const [sessions, setSessions] = useState([]);
    const [trainers, setTrainers] = useState([]);
    const [workoutTypes, setWorkoutTypes] = useState([]);
    const [selectedSession, setSelectedSession] = useState(null);
    const [showForm, setShowForm] = useState(false);
    const [form, setForm] = useState(emptyForm);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [fieldErrors, setFieldErrors] = useState({});
    const [trainerOpen, setTrainerOpen] = useState(false);
    const [typeOpen, setTypeOpen] = useState(false);
    const [filterTrainer, setFilterTrainer] = useState('ALL');
    const [filterType, setFilterType] = useState('ALL');
    const trainerRef = useRef(null);
    const typeRef = useRef(null);
    const navigate = useNavigate();
    const { user } = useAuth();
    const isAdmin = user?.role === 'ADMIN';

    const fetchData = useCallback(async () => {
        try {
            const [s, t, wt] = await Promise.all([
                sessionApi.getAll(),
                trainerApi.getAll(),
                workoutTypeApi.getActive(),
            ]);
            setSessions(s.data || []);
            setTrainers(t.data || []);
            setWorkoutTypes(wt.data || []);
        } catch {
            setError('Не удалось загрузить данные');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        let cancelled = false;
        if (!cancelled) fetchData();
        return () => { cancelled = true; };
    }, [fetchData]);

    useEffect(() => {
        const handler = (e) => {
            if (trainerRef.current
                && !trainerRef.current.contains(e.target)) {
                setTrainerOpen(false);
            }
            if (typeRef.current
                && !typeRef.current.contains(e.target)) {
                setTypeOpen(false);
            }
        };
        document.addEventListener('mousedown', handler);
        return () => document.removeEventListener('mousedown', handler);
    }, []);

    useEffect(() => {
        const oneMonthAgo = new Date(today);
        oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1);
        const cutoff = toLocalDateString(oneMonthAgo);

        const oldSessions = sessions.filter((s) => {
            const d = s.sessionDate || s.startDate;
            return d && d < cutoff;
        });

        if (oldSessions.length > 0) {
            oldSessions.forEach((s) => {
                sessionApi.delete(s.id).catch(() => {});
            });
            setSessions((prev) =>
                prev.filter((s) => {
                    const d = s.sessionDate || s.startDate;
                    return !d || d >= cutoff;
                })
            );
        }
    }, [sessions, today]);

    const weekDays = Array.from({ length: 7 }, (_, i) => {
        const d = new Date(weekStart);
        d.setDate(d.getDate() + i);
        return toLocalDateString(d);
    });

    const isSessionPast = (s) => {
        const sessionDate = s.sessionDate || s.startDate;
        if (!sessionDate) return false;
        const sessionDateTime = new Date(
            `${sessionDate}T${s.endTime || '23:59'}`
        );
        return sessionDateTime < new Date();
    };

    const isSessionFull = (s) => {
        const bookedCount = s.bookedCount || 0;
        return s.maxParticipants > 0 && bookedCount >= s.maxParticipants;
    };

    const filteredSessions = sessions
        .filter((s) => {
            const date = s.sessionDate || s.startDate;
            if (!date || date !== selectedDate) return false;
            if (filterTrainer !== 'ALL'
                && s.trainerId !== parseInt(filterTrainer)) return false;
            if (filterType !== 'ALL'
                && s.workoutTypeId !== parseInt(filterType)) return false;
            return true;
        })
        .sort((a, b) =>
            (a.startTime || '').localeCompare(b.startTime || '')
        );

    const getBookedColor = (b, m) => {
        if (!m || m === 0) return colors.textMuted;
        const r = b / m;
        if (r >= 1) return colors.bookedFull;
        if (r >= 0.7) return colors.bookedHigh;
        if (r >= 0.4) return colors.bookedMid;
        return colors.bookedLow;
    };

    const handleOpenSession = (s) => setSelectedSession(s);
    const handleCloseSession = () => setSelectedSession(null);

    const handleNew = () => {
        setForm({ ...emptyForm, sessionDate: selectedDate });
        setShowForm(true);
        setFieldErrors({});
        setError('');
    };

    const handleCancel = () => {
        setForm(emptyForm);
        setShowForm(false);
        setFieldErrors({});
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
        if (fieldErrors[name]) {
            setFieldErrors((prev) => {
                const next = { ...prev };
                delete next[name];
                return next;
            });
        }
    };

    const checkOverlap = () => {
        if (!form.trainerId || !form.sessionDate
            || !form.startTime || !form.endTime) return false;
        return sessions.some((s) =>
            s.trainerId === parseInt(form.trainerId)
            && (s.sessionDate || s.startDate) === form.sessionDate
            && s.startTime < form.endTime
            && (s.endTime || '00:00') > form.startTime
        );
    };

    const handleSave = async () => {
        const errors = {};

        if (!form.trainerId) {
            errors.trainerId = 'Выберите тренера';
        }
        if (!form.workoutTypeId) {
            errors.workoutTypeId = 'Выберите тип';
        }
        if (!form.sessionDate) {
            errors.sessionDate = 'Выберите дату';
        } else {
            const sessionDateObj = parseLocalDate(form.sessionDate);
            if (sessionDateObj && sessionDateObj < today) {
                errors.sessionDate =
                    'Нельзя создать тренировку на прошедшую дату';
            }
        }
        if (!form.startTime) {
            errors.startTime = 'Введите время начала';
        }
        if (!form.endTime) {
            errors.endTime = 'Введите время окончания';
        }
        if (form.startTime && form.endTime
            && form.startTime >= form.endTime) {
            errors.endTime = 'Окончание должно быть позже начала';
        }
        if (form.startTime && form.sessionDate) {
            const sessionDateObj = parseLocalDate(form.sessionDate);
            if (sessionDateObj
                && toLocalDateString(sessionDateObj)
                === toLocalDateString(today)) {
                const now = new Date();
                const startObj = new Date(
                    `${form.sessionDate}T${form.startTime}`
                );
                if (startObj <= now) {
                    errors.startTime =
                        'Время начала уже прошло';
                }
            }
        }
        if (!form.room.trim()) {
            errors.room = 'Укажите зал';
        }
        if (!form.maxParticipants || form.maxParticipants < 1) {
            errors.maxParticipants = 'Минимум 1 место';
        }
        if (form.maxParticipants > 100) {
            errors.maxParticipants = 'Максимум 100 мест';
        }
        if (Object.keys(errors).length === 0 && checkOverlap()) {
            errors.trainerId =
                'У тренера уже есть тренировка в это время и в этот день';
        }

        setFieldErrors(errors);
        if (Object.keys(errors).length > 0) return;

        setSaving(true);
        setError('');
        try {
            await sessionApi.create({
                ...form,
                trainerId: parseInt(form.trainerId),
                workoutTypeId: parseInt(form.workoutTypeId),
                maxParticipants: parseInt(form.maxParticipants),
                dayOfWeek: new Date(form.sessionDate)
                    .toLocaleDateString('en-US', { weekday: 'long' })
                    .toUpperCase(),
            });
            setSuccess('Тренировка создана');
            setShowForm(false);
            setForm(emptyForm);
            fetchData();
        } catch (err2) {
            setError(
                err2.response?.data?.message || 'Не удалось сохранить'
            );
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id, e) => {
        if (e) e.stopPropagation();
        if (!id) return;
        if (!window.confirm('Удалить тренировку?')) return;
        try {
            await sessionApi.delete(id);
            setSelectedSession(null);
            fetchData();
            setSuccess('Тренировка удалена');
        } catch {
            setError('Не удалось удалить тренировку');
        }
    };

    const changeWeek = (dir) => {
        const d = new Date(weekStart);
        d.setDate(d.getDate() + dir * 7);
        const ns = toLocalDateString(d);
        if (ns < todayMonday || ns > maxMonday) return;
        setWeekStart(ns);
    };

    const canGoBack = weekStart > todayMonday;
    const canGoForward = weekStart < maxMonday;

    if (loading) {
        return (
            <div style={{
                minHeight: '60vh', display: 'flex',
                alignItems: 'center', justifyContent: 'center',
                fontFamily: FONT_BODY, color: colors.textSecondary,
                fontSize: '16px',
            }}>
                Загрузка...
            </div>
        );
    }

    const modalOverlayStyle = {
        position: 'fixed', inset: 0,
        backgroundColor: 'rgba(0,0,0,0.3)',
        backdropFilter: 'blur(4px)', zIndex: 100,
        display: 'flex', alignItems: 'center',
        justifyContent: 'center', padding: SPACING.sm,
    };

    const modalContentStyle = {
        backgroundColor: 'white', borderRadius: '24px',
        padding: `${SPACING.lg} ${SPACING.md}`,
        maxWidth: '560px', width: '100%',
        maxHeight: '85vh', overflowY: 'auto',
        boxShadow: '0 20px 60px rgba(0,0,0,0.15)',
        fontFamily: FONT_BODY,
    };

    return (
        <div style={{
            padding: `${SPACING.sm} ${SPACING.sm} ${SPACING.lg}`,
        }}>
            <style>{`
                input[type="date"]::-webkit-calendar-picker-indicator,
                input[type="time"]::-webkit-calendar-picker-indicator {
                    filter: invert(60%) sepia(10%) saturate(300%)
                            hue-rotate(40deg);
                    cursor: pointer; opacity: 0.7;
                    width: 22px; height: 22px;
                }
                input[type="date"]::-webkit-calendar-picker-indicator:hover,
                input[type="time"]::-webkit-calendar-picker-indicator:hover {
                    opacity: 1;
                }
                input[type="date"]::-webkit-datetime-edit,
                input[type="time"]::-webkit-datetime-edit {
                    color: #0F0F10;
                    font-family: 'Inter', 'Segoe UI', sans-serif;
                }
            `}</style>

            <div style={{ maxWidth: '1100px', margin: '0 auto' }}>
                <div style={{
                    display: 'flex', alignItems: 'center',
                    justifyContent: 'center', gap: SPACING.xs,
                    marginBottom: SPACING.md,
                }}>
                    <h1 style={{
                        fontSize: 'clamp(26px, 5vw, 38px)',
                        fontWeight: '300', color: colors.primaryDark,
                        fontFamily: FONT_HEADING, margin: 0,
                    }}>
                        Расписание
                    </h1>
                    {isAdmin && !showForm && (
                        <button
                            type="button" onClick={handleNew}
                            aria-label="Добавить"
                            style={{
                                width: '40px', height: '40px',
                                borderRadius: '10px',
                                color: colors.primaryDark,
                                backgroundColor: 'rgba(255,255,255,0.8)',
                                border:
                                    `1px solid ${colors.glassBorder}`,
                                cursor: 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                transition: 'all 0.2s ease',
                            }}
                            onMouseOver={(e) => {
                                e.currentTarget.style.backgroundColor =
                                    colors.primaryDark;
                                e.currentTarget.style.color = '#FFF';
                            }}
                            onFocus={(e) => {
                                e.currentTarget.style.backgroundColor =
                                    colors.primaryDark;
                                e.currentTarget.style.color = '#FFF';
                            }}
                            onMouseOut={(e) => {
                                e.currentTarget.style.backgroundColor =
                                    'rgba(255,255,255,0.8)';
                                e.currentTarget.style.color =
                                    colors.primaryDark;
                            }}
                            onBlur={(e) => {
                                e.currentTarget.style.backgroundColor =
                                    'rgba(255,255,255,0.8)';
                                e.currentTarget.style.color =
                                    colors.primaryDark;
                            }}
                        >
                            <PlusIcon />
                        </button>
                    )}
                </div>

                {error && (
                    <div style={{
                        padding: '12px 16px',
                        backgroundColor: '#FFF0F0',
                        borderRadius: '10px', color: colors.error,
                        fontFamily: FONT_BODY, fontSize: '14px',
                        marginBottom: SPACING.sm, textAlign: 'center',
                    }}>
                        {error}
                    </div>
                )}
                {success && (
                    <div style={{
                        padding: '12px 16px',
                        backgroundColor: '#F0F7F0',
                        borderRadius: '10px', color: colors.success,
                        fontFamily: FONT_BODY, fontSize: '14px',
                        marginBottom: SPACING.sm, textAlign: 'center',
                    }}>
                        {success}
                    </div>
                )}

                {showForm && (
                    <div style={modalOverlayStyle} onClick={handleCancel}>
                        <div style={modalContentStyle}
                             onClick={(e) => e.stopPropagation()}>
                            <h3 style={{
                                fontFamily: FONT_HEADING,
                                color: colors.primaryDark,
                                fontSize: '24px', textAlign: 'center',
                                marginTop: 0, marginBottom: SPACING.md,
                            }}>
                                Новая тренировка
                            </h3>
                            <div style={{
                                display: 'grid', gap: SPACING.sm,
                            }}>
                                <div style={{
                                    display: 'grid',
                                    gridTemplateColumns: '1fr 1fr',
                                    gap: SPACING.sm,
                                }}>
                                    <div style={{ position: 'relative' }}
                                         ref={trainerRef}>
                                        <label style={{
                                            display: 'block',
                                            marginBottom: '4px',
                                            fontSize: '13px',
                                            fontWeight: '600',
                                            fontFamily: FONT_BODY,
                                            color: colors.textSecondary,
                                        }}>
                                            Тренер
                                        </label>
                                        <button
                                            type="button"
                                            onClick={() =>
                                                setTrainerOpen(!trainerOpen)
                                            }
                                            style={
                                                fieldErrors.trainerId
                                                    ? {
                                                        ...dropdownBtnStyle(
                                                            trainerOpen
                                                        ),
                                                        border:
                                                            '1px solid #C87B7B',
                                                        backgroundColor:
                                                            '#FFF8F8',
                                                    }
                                                    : dropdownBtnStyle(
                                                        trainerOpen
                                                    )
                                            }
                                        >
                                            {trainers.find(
                                                (t) => t.id === parseInt(
                                                    form.trainerId
                                                )
                                            )?.lastName || 'Выбрать'}
                                            <svg width="14" height="14"
                                                 viewBox="0 0 24 24"
                                                 fill="none"
                                                 stroke={
                                                     colors.primaryDark
                                                 }
                                                 strokeWidth="2"
                                                 strokeLinecap="round"
                                                 strokeLinejoin="round"
                                                 style={{
                                                     transform: trainerOpen
                                                         ? 'rotate(180deg)'
                                                         : 'rotate(0)',
                                                     marginLeft: '12px',
                                                     flexShrink: 0,
                                                 }}>
                                                <polyline
                                                    points="6 9 12 15 18 9"
                                                />
                                            </svg>
                                        </button>
                                        {trainerOpen && (
                                            <div style={{
                                                position: 'absolute',
                                                top: '100%',
                                                left: 0, right: 0,
                                                zIndex: 20,
                                                backgroundColor: 'white',
                                                border: '1px solid rgba(180,180,170,0.5)',
                                                borderTop: 'none',
                                                borderRadius:
                                                    '0 0 10px 10px',
                                                boxShadow: '0 8px 24px rgba(77,80,68,0.12)',
                                                overflow: 'hidden',
                                                maxHeight: '200px',
                                                overflowY: 'auto',
                                            }}>
                                                {trainers
                                                    .filter((t) =>
                                                        t.status
                                                        !== 'VACATION'
                                                    )
                                                    .map((t) => (
                                                        <button
                                                            key={t.id}
                                                            type="button"
                                                            onClick={() => {
                                                                setForm(
                                                                    (prev) => ({
                                                                        ...prev,
                                                                        trainerId: t.id,
                                                                    })
                                                                );
                                                                setTrainerOpen(
                                                                    false
                                                                );
                                                            }}
                                                            style={dropdownOptStyle(
                                                                parseInt(
                                                                    form.trainerId
                                                                ) === t.id
                                                            )}
                                                        >
                                                            {t.lastName}{' '}
                                                            {t.firstName}
                                                        </button>
                                                    ))}
                                            </div>
                                        )}
                                        {fieldErrors.trainerId && (
                                            <p style={errorTextStyle}>
                                                {fieldErrors.trainerId}
                                            </p>
                                        )}
                                    </div>

                                    <div style={{ position: 'relative' }}
                                         ref={typeRef}>
                                        <label style={{
                                            display: 'block',
                                            marginBottom: '4px',
                                            fontSize: '13px',
                                            fontWeight: '600',
                                            fontFamily: FONT_BODY,
                                            color: colors.textSecondary,
                                        }}>
                                            Тип
                                        </label>
                                        <button
                                            type="button"
                                            onClick={() =>
                                                setTypeOpen(!typeOpen)
                                            }
                                            style={
                                                fieldErrors.workoutTypeId
                                                    ? {
                                                        ...dropdownBtnStyle(
                                                            typeOpen
                                                        ),
                                                        border:
                                                            '1px solid #C87B7B',
                                                        backgroundColor:
                                                            '#FFF8F8',
                                                    }
                                                    : dropdownBtnStyle(
                                                        typeOpen
                                                    )
                                            }
                                        >
                                            {workoutTypes.find(
                                                (wt) => wt.id === parseInt(
                                                    form.workoutTypeId
                                                )
                                            )?.name || 'Выбрать'}
                                            <svg width="14" height="14"
                                                 viewBox="0 0 24 24"
                                                 fill="none"
                                                 stroke={
                                                     colors.primaryDark
                                                 }
                                                 strokeWidth="2"
                                                 strokeLinecap="round"
                                                 strokeLinejoin="round"
                                                 style={{
                                                     transform: typeOpen
                                                         ? 'rotate(180deg)'
                                                         : 'rotate(0)',
                                                     marginLeft: '12px',
                                                     flexShrink: 0,
                                                 }}>
                                                <polyline
                                                    points="6 9 12 15 18 9"
                                                />
                                            </svg>
                                        </button>
                                        {typeOpen && (
                                            <div style={{
                                                position: 'absolute',
                                                top: '100%',
                                                left: 0, right: 0,
                                                zIndex: 20,
                                                backgroundColor: 'white',
                                                border: '1px solid rgba(180,180,170,0.5)',
                                                borderTop: 'none',
                                                borderRadius:
                                                    '0 0 10px 10px',
                                                boxShadow: '0 8px 24px rgba(77,80,68,0.12)',
                                                overflow: 'hidden',
                                                maxHeight: '200px',
                                                overflowY: 'auto',
                                            }}>
                                                {workoutTypes.map((wt) => (
                                                    <button
                                                        key={wt.id}
                                                        type="button"
                                                        onClick={() => {
                                                            setForm(
                                                                (prev) => ({
                                                                    ...prev,
                                                                    workoutTypeId: wt.id,
                                                                })
                                                            );
                                                            setTypeOpen(
                                                                false
                                                            );
                                                        }}
                                                        style={dropdownOptStyle(
                                                            parseInt(
                                                                form.workoutTypeId
                                                            ) === wt.id
                                                        )}
                                                    >
                                                        {wt.name}
                                                    </button>
                                                ))}
                                            </div>
                                        )}
                                        {fieldErrors.workoutTypeId && (
                                            <p style={errorTextStyle}>
                                                {fieldErrors.workoutTypeId}
                                            </p>
                                        )}
                                    </div>
                                </div>

                                <div>
                                    <label style={{
                                        display: 'block',
                                        marginBottom: '4px',
                                        fontSize: '13px',
                                        fontWeight: '600',
                                        fontFamily: FONT_BODY,
                                        color: colors.textSecondary,
                                    }}>
                                        Дата
                                    </label>
                                    <input
                                        name="sessionDate" type="date"
                                        value={form.sessionDate}
                                        onChange={handleChange}
                                        min={toLocalDateString(today)}
                                        style={
                                            fieldErrors.sessionDate
                                                ? inputErrorStyle
                                                : inputStyle
                                        }
                                    />
                                    {fieldErrors.sessionDate && (
                                        <p style={errorTextStyle}>
                                            {fieldErrors.sessionDate}
                                        </p>
                                    )}
                                </div>

                                <div style={{
                                    display: 'grid',
                                    gridTemplateColumns: '1fr 1fr',
                                    gap: SPACING.sm,
                                }}>
                                    <div>
                                        <label style={{
                                            display: 'block',
                                            marginBottom: '4px',
                                            fontSize: '13px',
                                            fontWeight: '600',
                                            fontFamily: FONT_BODY,
                                            color: colors.textSecondary,
                                        }}>
                                            Начало
                                        </label>
                                        <input
                                            name="startTime" type="time"
                                            value={form.startTime}
                                            onChange={handleChange}
                                            style={
                                                fieldErrors.startTime
                                                    ? inputErrorStyle
                                                    : inputStyle
                                            }
                                        />
                                        {fieldErrors.startTime && (
                                            <p style={errorTextStyle}>
                                                {fieldErrors.startTime}
                                            </p>
                                        )}
                                    </div>
                                    <div>
                                        <label style={{
                                            display: 'block',
                                            marginBottom: '4px',
                                            fontSize: '13px',
                                            fontWeight: '600',
                                            fontFamily: FONT_BODY,
                                            color: colors.textSecondary,
                                        }}>
                                            Конец
                                        </label>
                                        <input
                                            name="endTime" type="time"
                                            value={form.endTime}
                                            onChange={handleChange}
                                            style={
                                                fieldErrors.endTime
                                                    ? inputErrorStyle
                                                    : inputStyle
                                            }
                                        />
                                        {fieldErrors.endTime && (
                                            <p style={errorTextStyle}>
                                                {fieldErrors.endTime}
                                            </p>
                                        )}
                                    </div>
                                </div>

                                <div style={{
                                    display: 'grid',
                                    gridTemplateColumns: '1fr 1fr',
                                    gap: SPACING.sm,
                                }}>
                                    <div>
                                        <label style={{
                                            display: 'block',
                                            marginBottom: '4px',
                                            fontSize: '13px',
                                            fontWeight: '600',
                                            fontFamily: FONT_BODY,
                                            color: colors.textSecondary,
                                        }}>
                                            Мест
                                        </label>
                                        <input
                                            name="maxParticipants"
                                            type="number"
                                            min="1" max="100"
                                            value={form.maxParticipants}
                                            onChange={handleChange}
                                            style={
                                                fieldErrors.maxParticipants
                                                    ? inputErrorStyle
                                                    : inputStyle
                                            }
                                        />
                                        {fieldErrors.maxParticipants && (
                                            <p style={errorTextStyle}>
                                                {fieldErrors.maxParticipants}
                                            </p>
                                        )}
                                    </div>
                                    <div>
                                        <label style={{
                                            display: 'block',
                                            marginBottom: '4px',
                                            fontSize: '13px',
                                            fontWeight: '600',
                                            fontFamily: FONT_BODY,
                                            color: colors.textSecondary,
                                        }}>
                                            Зал
                                        </label>
                                        <input
                                            name="room"
                                            value={form.room}
                                            onChange={handleChange}
                                            placeholder="ЗЕМЛЯ"
                                            style={
                                                fieldErrors.room
                                                    ? inputErrorStyle
                                                    : inputStyle
                                            }
                                        />
                                        {fieldErrors.room && (
                                            <p style={errorTextStyle}>
                                                {fieldErrors.room}
                                            </p>
                                        )}
                                    </div>
                                </div>

                                <div>
                                    <label style={{
                                        display: 'block',
                                        marginBottom: '4px',
                                        fontSize: '13px',
                                        fontWeight: '600',
                                        fontFamily: FONT_BODY,
                                        color: colors.textSecondary,
                                    }}>
                                        Цвет
                                    </label>
                                    <div style={{
                                        display: 'flex', gap: SPACING.xs,
                                        flexWrap: 'wrap',
                                    }}>
                                        {COLOR_OPTIONS.map((c) => (
                                            <button
                                                key={c} type="button"
                                                onClick={() => setForm(
                                                    (prev) => ({
                                                        ...prev,
                                                        colorCode: c,
                                                    })
                                                )}
                                                style={{
                                                    width: '28px',
                                                    height: '28px',
                                                    borderRadius: '50%',
                                                    backgroundColor: c,
                                                    border:
                                                        form.colorCode === c
                                                            ? '3px solid #4D5044'
                                                            : '1px solid rgba(180,180,170,0.5)',
                                                    cursor: 'pointer',
                                                    transition:
                                                        'all 0.15s ease',
                                                }}
                                                onMouseOver={(e) => {
                                                    if (form.colorCode
                                                        !== c) {
                                                        e.currentTarget
                                                            .style.transform =
                                                            'scale(1.2)';
                                                    }
                                                }}
                                                onMouseOut={(e) => {
                                                    e.currentTarget
                                                        .style.transform =
                                                        'scale(1)';
                                                }}
                                            />
                                        ))}
                                    </div>
                                </div>
                            </div>

                            <div style={{
                                display: 'flex', gap: SPACING.sm,
                                justifyContent: 'flex-end',
                                marginTop: SPACING.md,
                            }}>
                                <button
                                    type="button" onClick={handleCancel}
                                    style={{
                                        padding: '14px 22px',
                                        borderRadius: '12px',
                                        border:
                                            `1px solid ${colors.error}`,
                                        backgroundColor: 'transparent',
                                        color: colors.error,
                                        fontFamily: FONT_BODY,
                                        fontSize: '15px',
                                        fontWeight: '600',
                                        cursor: 'pointer',
                                        minHeight: '48px',
                                        transition: 'all 0.2s ease',
                                    }}
                                    onMouseOver={(e) => {
                                        e.currentTarget.style
                                            .backgroundColor =
                                            colors.error;
                                        e.currentTarget.style.color =
                                            '#FFF';
                                    }}
                                    onFocus={(e) => {
                                        e.currentTarget.style
                                            .backgroundColor =
                                            colors.error;
                                        e.currentTarget.style.color =
                                            '#FFF';
                                    }}
                                    onMouseOut={(e) => {
                                        e.currentTarget.style
                                            .backgroundColor =
                                            'transparent';
                                        e.currentTarget.style.color =
                                            colors.error;
                                    }}
                                    onBlur={(e) => {
                                        e.currentTarget.style
                                            .backgroundColor =
                                            'transparent';
                                        e.currentTarget.style.color =
                                            colors.error;
                                    }}
                                >
                                    Отмена
                                </button>
                                <button
                                    type="button" onClick={handleSave}
                                    disabled={saving}
                                    style={{
                                        padding: '14px 22px',
                                        borderRadius: '12px',
                                        border: 'none',
                                        backgroundColor:
                                        colors.primaryDark,
                                        color: '#FFF',
                                        fontFamily: FONT_BODY,
                                        fontSize: '15px',
                                        fontWeight: '600',
                                        cursor: 'pointer',
                                        minHeight: '48px',
                                        opacity: saving ? 0.7 : 1,
                                        transition: 'all 0.2s ease',
                                    }}
                                    onMouseOver={(e) => {
                                        if (!saving) {
                                            e.currentTarget.style
                                                .backgroundColor =
                                                colors.primaryHover;
                                        }
                                    }}
                                    onFocus={(e) => {
                                        if (!saving) {
                                            e.currentTarget.style
                                                .backgroundColor =
                                                colors.primaryHover;
                                        }
                                    }}
                                    onMouseOut={(e) => {
                                        if (!saving) {
                                            e.currentTarget.style
                                                .backgroundColor =
                                                colors.primaryDark;
                                        }
                                    }}
                                    onBlur={(e) => {
                                        if (!saving) {
                                            e.currentTarget.style
                                                .backgroundColor =
                                                colors.primaryDark;
                                        }
                                    }}
                                >
                                    {saving
                                        ? 'Сохранение...'
                                        : 'Создать'}
                                </button>
                            </div>
                        </div>
                    </div>
                )}

                <div style={{
                    display: 'flex', alignItems: 'center',
                    justifyContent: 'center', gap: SPACING.xs,
                    marginBottom: SPACING.sm,
                }}>
                    <button
                        type="button"
                        onClick={() => changeWeek(-1)}
                        disabled={!canGoBack}
                        style={{
                            background: 'none',
                            border:
                                `1px solid ${colors.glassBorder}`,
                            borderRadius: '10px',
                            cursor: canGoBack
                                ? 'pointer' : 'default',
                            fontSize: '16px',
                            color: canGoBack
                                ? colors.primaryDark
                                : colors.textMuted,
                            opacity: canGoBack ? 1 : 0.3,
                            padding: '8px 14px',
                            fontFamily: FONT_BODY,
                            transition: 'all 0.2s ease',
                        }}
                        onMouseOver={(e) => {
                            if (canGoBack) {
                                e.currentTarget.style.backgroundColor =
                                    colors.primaryLight;
                            }
                        }}
                        onFocus={(e) => {
                            if (canGoBack) {
                                e.currentTarget.style.backgroundColor =
                                    colors.primaryLight;
                            }
                        }}
                        onMouseOut={(e) => {
                            e.currentTarget.style.backgroundColor =
                                'transparent';
                        }}
                        onBlur={(e) => {
                            e.currentTarget.style.backgroundColor =
                                'transparent';
                        }}
                    >
                        ←
                    </button>
                    <span style={{
                        fontFamily: FONT_BODY, fontSize: '15px',
                        color: colors.textPrimary, fontWeight: '500',
                        padding: '8px 12px',
                        backgroundColor: colors.backgroundCard,
                        borderRadius: '10px',
                        border:
                            `1px solid ${colors.glassBorder}`,
                    }}>
                        {formatDate(weekDays[0])} –{' '}
                        {formatDate(weekDays[6])}
                    </span>
                    <button
                        type="button"
                        onClick={() => changeWeek(1)}
                        disabled={!canGoForward}
                        style={{
                            background: 'none',
                            border:
                                `1px solid ${colors.glassBorder}`,
                            borderRadius: '10px',
                            cursor: canGoForward
                                ? 'pointer' : 'default',
                            fontSize: '16px',
                            color: canGoForward
                                ? colors.primaryDark
                                : colors.textMuted,
                            opacity: canGoForward ? 1 : 0.3,
                            padding: '8px 14px',
                            fontFamily: FONT_BODY,
                            transition: 'all 0.2s ease',
                        }}
                        onMouseOver={(e) => {
                            if (canGoForward) {
                                e.currentTarget.style.backgroundColor =
                                    colors.primaryLight;
                            }
                        }}
                        onFocus={(e) => {
                            if (canGoForward) {
                                e.currentTarget.style.backgroundColor =
                                    colors.primaryLight;
                            }
                        }}
                        onMouseOut={(e) => {
                            e.currentTarget.style.backgroundColor =
                                'transparent';
                        }}
                        onBlur={(e) => {
                            e.currentTarget.style.backgroundColor =
                                'transparent';
                        }}
                    >
                        →
                    </button>
                </div>

                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(7, 1fr)',
                    gap: SPACING.xs, marginBottom: SPACING.md,
                    backgroundColor: colors.backgroundCard,
                    borderRadius: '14px',
                    border:
                        `1px solid ${colors.glassBorder}`,
                    padding: SPACING.xs,
                }}>
                    {weekDays.map((date, i) => (
                        <button
                            key={date}
                            type="button"
                            onClick={() => setSelectedDate(date)}
                            style={{
                                padding: '10px 6px',
                                borderRadius: '10px',
                                border: date === selectedDate
                                    ? `2px solid ${colors.primaryDark}`
                                    : '1px solid transparent',
                                backgroundColor: date === selectedDate
                                    ? colors.primaryLight
                                    : 'transparent',
                                cursor: 'pointer',
                                fontFamily: FONT_BODY,
                                fontSize: '13px',
                                color: date === selectedDate
                                    ? colors.primaryDark
                                    : colors.textSecondary,
                                textAlign: 'center',
                                transition: 'all 0.15s ease',
                            }}
                            onMouseOver={(e) => {
                                if (date !== selectedDate) {
                                    e.currentTarget.style
                                        .backgroundColor =
                                        '#F5F5F0';
                                }
                            }}
                            onMouseOut={(e) => {
                                if (date !== selectedDate) {
                                    e.currentTarget.style
                                        .backgroundColor =
                                        'transparent';
                                }
                            }}
                        >
                            <div style={{ fontWeight: '600' }}>
                                {DAYS[i]}
                            </div>
                            <div>{new Date(date).getDate()}</div>
                        </button>
                    ))}
                </div>

                <div style={{
                    display: 'flex', gap: SPACING.md,
                    alignItems: 'flex-start',
                }}>
                    <div style={{
                        width: '220px', flexShrink: 0,
                        display: 'flex', flexDirection: 'column',
                        gap: SPACING.sm, position: 'sticky',
                        top: '80px',
                    }}>
                        <div style={{
                            backgroundColor: colors.backgroundCard,
                            borderRadius: '14px',
                            border:
                                `1px solid ${colors.glassBorder}`,
                            padding: SPACING.sm,
                        }}>
                            <div style={{
                                fontSize: '14px', fontWeight: '600',
                                fontFamily: FONT_BODY,
                                color: colors.primaryDark,
                                marginBottom: SPACING.sm,
                                textAlign: 'center',
                                paddingBottom: SPACING.sm,
                                borderBottom:
                                    `1px solid ${colors.glassBorder}`,
                            }}>
                                Тренер
                            </div>
                            <button
                                key="trainer-all" type="button"
                                onClick={() =>
                                    setFilterTrainer('ALL')
                                }
                                style={{
                                    width: '100%',
                                    padding: '10px 10px',
                                    fontSize: '15px',
                                    fontFamily: FONT_BODY,
                                    fontWeight:
                                        filterTrainer === 'ALL'
                                            ? '600' : '400',
                                    color:
                                        filterTrainer === 'ALL'
                                            ? colors.primaryDark
                                            : colors.textSecondary,
                                    backgroundColor:
                                        filterTrainer === 'ALL'
                                            ? colors.primaryLight
                                            : 'transparent',
                                    border: 'none',
                                    borderRadius: '6px',
                                    cursor: 'pointer',
                                    textAlign: 'left',
                                    marginBottom: '2px',
                                    transition:
                                        'all 0.15s ease',
                                }}
                                onMouseOver={(e) => {
                                    if (filterTrainer !== 'ALL') {
                                        e.currentTarget.style
                                            .backgroundColor =
                                            '#F5F5F0';
                                    }
                                }}
                                onMouseOut={(e) => {
                                    if (filterTrainer !== 'ALL') {
                                        e.currentTarget.style
                                            .backgroundColor =
                                            'transparent';
                                    }
                                }}
                            >
                                Все
                            </button>
                            {trainers
                                .filter((t) =>
                                    t.status !== 'VACATION'
                                )
                                .map((t) => (
                                    <button
                                        key={t.id} type="button"
                                        onClick={() =>
                                            setFilterTrainer(
                                                String(t.id)
                                            )
                                        }
                                        style={{
                                            width: '100%',
                                            padding: '10px 10px',
                                            fontSize: '15px',
                                            fontFamily: FONT_BODY,
                                            fontWeight:
                                                filterTrainer
                                                === String(t.id)
                                                    ? '600' : '400',
                                            color:
                                                filterTrainer
                                                === String(t.id)
                                                    ? colors.primaryDark
                                                    : colors.textSecondary,
                                            backgroundColor:
                                                filterTrainer
                                                === String(t.id)
                                                    ? colors.primaryLight
                                                    : 'transparent',
                                            border: 'none',
                                            borderRadius: '6px',
                                            cursor: 'pointer',
                                            textAlign: 'left',
                                            transition:
                                                'all 0.15s ease',
                                        }}
                                        onMouseOver={(e) => {
                                            if (filterTrainer
                                                !== String(t.id)) {
                                                e.currentTarget
                                                    .style
                                                    .backgroundColor =
                                                    '#F5F5F0';
                                            }
                                        }}
                                        onMouseOut={(e) => {
                                            if (filterTrainer
                                                !== String(t.id)) {
                                                e.currentTarget
                                                    .style
                                                    .backgroundColor =
                                                    'transparent';
                                            }
                                        }}
                                    >
                                        {t.lastName} {t.firstName}
                                    </button>
                                ))}
                        </div>
                        <div style={{
                            backgroundColor: colors.backgroundCard,
                            borderRadius: '14px',
                            border:
                                `1px solid ${colors.glassBorder}`,
                            padding: SPACING.sm,
                        }}>
                            <div style={{
                                fontSize: '14px', fontWeight: '600',
                                fontFamily: FONT_BODY,
                                color: colors.primaryDark,
                                marginBottom: SPACING.sm,
                                textAlign: 'center',
                                paddingBottom: SPACING.sm,
                                borderBottom:
                                    `1px solid ${colors.glassBorder}`,
                            }}>
                                Направление
                            </div>
                            <button
                                key="type-all" type="button"
                                onClick={() =>
                                    setFilterType('ALL')
                                }
                                style={{
                                    width: '100%',
                                    padding: '10px 10px',
                                    fontSize: '15px',
                                    fontFamily: FONT_BODY,
                                    fontWeight:
                                        filterType === 'ALL'
                                            ? '600' : '400',
                                    color:
                                        filterType === 'ALL'
                                            ? colors.primaryDark
                                            : colors.textSecondary,
                                    backgroundColor:
                                        filterType === 'ALL'
                                            ? colors.primaryLight
                                            : 'transparent',
                                    border: 'none',
                                    borderRadius: '6px',
                                    cursor: 'pointer',
                                    textAlign: 'left',
                                    marginBottom: '2px',
                                    transition:
                                        'all 0.15s ease',
                                }}
                                onMouseOver={(e) => {
                                    if (filterType !== 'ALL') {
                                        e.currentTarget.style
                                            .backgroundColor =
                                            '#F5F5F0';
                                    }
                                }}
                                onMouseOut={(e) => {
                                    if (filterType !== 'ALL') {
                                        e.currentTarget.style
                                            .backgroundColor =
                                            'transparent';
                                    }
                                }}
                            >
                                Все
                            </button>
                            {workoutTypes.map((wt) => (
                                <button
                                    key={wt.id} type="button"
                                    onClick={() =>
                                        setFilterType(
                                            String(wt.id)
                                        )
                                    }
                                    style={{
                                        width: '100%',
                                        padding: '10px 10px',
                                        fontSize: '15px',
                                        fontFamily: FONT_BODY,
                                        fontWeight:
                                            filterType
                                            === String(wt.id)
                                                ? '600' : '400',
                                        color:
                                            filterType
                                            === String(wt.id)
                                                ? colors.primaryDark
                                                : colors.textSecondary,
                                        backgroundColor:
                                            filterType
                                            === String(wt.id)
                                                ? colors.primaryLight
                                                : 'transparent',
                                        border: 'none',
                                        borderRadius: '6px',
                                        cursor: 'pointer',
                                        textAlign: 'left',
                                        transition:
                                            'all 0.15s ease',
                                    }}
                                    onMouseOver={(e) => {
                                        if (filterType
                                            !== String(wt.id)) {
                                            e.currentTarget
                                                .style
                                                .backgroundColor =
                                                '#F5F5F0';
                                        }
                                    }}
                                    onMouseOut={(e) => {
                                        if (filterType
                                            !== String(wt.id)) {
                                            e.currentTarget
                                                .style
                                                .backgroundColor =
                                                'transparent';
                                        }
                                    }}
                                >
                                    {wt.name}
                                </button>
                            ))}
                        </div>
                    </div>

                    <div style={{
                        flex: 1, display: 'flex',
                        flexDirection: 'column', gap: SPACING.sm,
                    }}>
                        {filteredSessions.length === 0 ? (
                            <p style={{
                                textAlign: 'center',
                                color: colors.textMuted,
                                fontFamily: FONT_BODY,
                                fontSize: '17px',
                                padding: SPACING.lg,
                            }}>
                                Нет тренировок
                            </p>
                        ) : (
                            filteredSessions.map((s) => {
                                const trainer = trainers.find(
                                    (t) => t.id === s.trainerId
                                );
                                const wt = workoutTypes.find(
                                    (t) => t.id === s.workoutTypeId
                                );
                                const bookedCount =
                                    s.bookedCount || 0;
                                const past = isSessionPast(s);
                                const full = isSessionFull(s);
                                const dimmed = past || full;
                                const spotsColor = getBookedColor(
                                    bookedCount, s.maxParticipants
                                );

                                return (
                                    <button
                                        key={s.id} type="button"
                                        onClick={() =>
                                            handleOpenSession(s)
                                        }
                                        style={{
                                            display: 'flex',
                                            alignItems: 'stretch',
                                            backgroundColor: dimmed
                                                ? colors.pastBg
                                                : colors.backgroundCard,
                                            backdropFilter: 'blur(16px)',
                                            borderRadius: '16px',
                                            border: full
                                                ? `1px solid ${colors.bookedFull}`
                                                : `1px solid ${colors.glassBorder}`,
                                            boxShadow: colors.shadowCard,
                                            cursor: 'pointer',
                                            textAlign: 'left',
                                            width: '100%',
                                            transition: 'all 0.2s ease',
                                            overflow: 'hidden',
                                            opacity: past ? 0.6 : 1,
                                        }}
                                        onMouseOver={(e) => {
                                            e.currentTarget.style
                                                .boxShadow =
                                                colors.shadowHover;
                                            e.currentTarget.style
                                                .transform =
                                                'translateY(-1px)';
                                        }}
                                        onFocus={(e) => {
                                            e.currentTarget.style
                                                .boxShadow =
                                                colors.shadowHover;
                                        }}
                                        onMouseOut={(e) => {
                                            e.currentTarget.style
                                                .boxShadow =
                                                colors.shadowCard;
                                            e.currentTarget.style
                                                .transform =
                                                'translateY(0)';
                                        }}
                                        onBlur={(e) => {
                                            e.currentTarget.style
                                                .boxShadow =
                                                colors.shadowCard;
                                        }}
                                    >
                                        <div style={{
                                            width: '8px',
                                            backgroundColor: past
                                                ? colors.textMuted
                                                : s.colorCode
                                                || colors.primary,
                                            flexShrink: 0,
                                        }} />
                                        <div style={{
                                            flex: 1, padding: SPACING.md,
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: SPACING.md,
                                            flexWrap: 'wrap',
                                        }}>
                                            <div style={{
                                                minWidth: '80px',
                                                textAlign: 'center',
                                            }}>
                                                <div style={{
                                                    fontSize: '20px',
                                                    fontWeight: '600',
                                                    color: dimmed
                                                        ? colors.pastText
                                                        : colors.primaryDark,
                                                    fontFamily: FONT_BODY,
                                                }}>
                                                    {formatTime(
                                                        s.startTime
                                                    )}
                                                </div>
                                                <div style={{
                                                    fontSize: '14px',
                                                    color: dimmed
                                                        ? colors.pastText
                                                        : colors.textMuted,
                                                    fontFamily: FONT_BODY,
                                                }}>
                                                    {formatTime(
                                                        s.endTime
                                                    )}
                                                </div>
                                            </div>
                                            <div style={{ flex: 1 }}>
                                                <div style={{
                                                    fontSize: '18px',
                                                    fontWeight: '600',
                                                    color: dimmed
                                                        ? colors.pastText
                                                        : colors.textPrimary,
                                                    fontFamily: FONT_BODY,
                                                }}>
                                                    {wt?.name
                                                        || 'Тренировка'}
                                                </div>
                                                <div style={{
                                                    display: 'flex',
                                                    gap: SPACING.xs,
                                                    flexWrap: 'wrap',
                                                    marginTop: '4px',
                                                }}>
                                                    {trainer && (
                                                        <span
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                navigate(
                                                                    '/trainers'
                                                                );
                                                            }}
                                                            style={{
                                                                fontSize:
                                                                    '15px',
                                                                color: dimmed
                                                                    ? colors.pastText
                                                                    : colors.primary,
                                                                fontFamily:
                                                                FONT_BODY,
                                                                background:
                                                                    'none',
                                                                border:
                                                                    'none',
                                                                cursor:
                                                                    'pointer',
                                                                padding: 0,
                                                                textDecoration:
                                                                    'underline',
                                                                transition:
                                                                    'color 0.15s ease',
                                                            }}
                                                            onMouseOver={
                                                                (e) => {
                                                                    e.currentTarget
                                                                        .style
                                                                        .color =
                                                                        dimmed
                                                                            ? colors.pastText
                                                                            : colors.primaryHover;
                                                                }
                                                            }
                                                            onMouseOut={
                                                                (e) => {
                                                                    e.currentTarget
                                                                        .style
                                                                        .color =
                                                                        dimmed
                                                                            ? colors.pastText
                                                                            : colors.primary;
                                                                }
                                                            }
                                                        >
                                                            {trainer.lastName}{' '}
                                                            {trainer.firstName}
                                                        </span>
                                                    )}
                                                    {wt && (
                                                        <span
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                navigate(
                                                                    `/directions?open=${wt.id}`
                                                                );
                                                            }}
                                                            style={{
                                                                fontSize:
                                                                    '14px',
                                                                color: dimmed
                                                                    ? colors.pastText
                                                                    : colors.textSecondary,
                                                                fontFamily:
                                                                FONT_BODY,
                                                                background:
                                                                    'none',
                                                                border:
                                                                    'none',
                                                                cursor:
                                                                    'pointer',
                                                                padding: 0,
                                                                transition:
                                                                    'color 0.15s ease',
                                                            }}
                                                            onMouseOver={
                                                                (e) => {
                                                                    e.currentTarget
                                                                        .style
                                                                        .color =
                                                                        dimmed
                                                                            ? colors.pastText
                                                                            : colors.primaryDark;
                                                                }
                                                            }
                                                            onMouseOut={
                                                                (e) => {
                                                                    e.currentTarget
                                                                        .style
                                                                        .color =
                                                                        dimmed
                                                                            ? colors.pastText
                                                                            : colors.textSecondary;
                                                                }
                                                            }
                                                        >
                                                            подробнее →
                                                        </span>
                                                    )}
                                                </div>
                                                {s.room && (
                                                    <div style={{
                                                        fontSize: '14px',
                                                        color: dimmed
                                                            ? colors.pastText
                                                            : colors.textMuted,
                                                        fontFamily:
                                                        FONT_BODY,
                                                        marginTop: '2px',
                                                    }}>
                                                        {s.room}
                                                    </div>
                                                )}
                                            </div>
                                            <div style={{
                                                textAlign: 'right',
                                                minWidth: '70px',
                                            }}>
                                                <div style={{
                                                    fontSize: '15px',
                                                    fontWeight: '600',
                                                    fontFamily: FONT_BODY,
                                                    color: dimmed
                                                        ? colors.pastText
                                                        : spotsColor,
                                                }}>
                                                    {bookedCount}/
                                                    {s.maxParticipants}
                                                </div>
                                                <div style={{
                                                    fontSize: '12px',
                                                    color: dimmed
                                                        ? colors.pastText
                                                        : colors.textMuted,
                                                    fontFamily: FONT_BODY,
                                                }}>
                                                    {full
                                                        ? 'заполнено'
                                                        : 'мест'}
                                                </div>
                                            </div>
                                        </div>
                                    </button>
                                );
                            })
                        )}
                    </div>
                </div>
            </div>

            {selectedSession && (
                <div style={modalOverlayStyle}
                     onClick={handleCloseSession}>
                    <div style={{
                        ...modalContentStyle,
                        maxWidth: '520px',
                    }} onClick={(e) => e.stopPropagation()}>
                        <div style={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            marginBottom: SPACING.md,
                        }}>
                            <h2 style={{
                                fontSize: '22px', fontWeight: '600',
                                color: colors.primaryDark,
                                fontFamily: FONT_BODY, margin: 0,
                            }}>
                                {workoutTypes.find(
                                    (wt) => wt.id
                                        === selectedSession.workoutTypeId
                                )?.name || 'Тренировка'}
                            </h2>
                            <button
                                type="button"
                                onClick={handleCloseSession}
                                style={{
                                    background: 'none', border: 'none',
                                    fontSize: '24px',
                                    cursor: 'pointer',
                                    color: colors.textMuted,
                                }}
                            >
                                ✕
                            </button>
                        </div>
                        <div style={{
                            display: 'grid', gap: SPACING.sm,
                        }}>
                            <div style={{
                                padding: '12px 16px',
                                backgroundColor: '#FAFAF8',
                                borderRadius: '10px',
                            }}>
                                <div style={{
                                    fontSize: '13px',
                                    color: colors.textMuted,
                                    marginBottom: '2px',
                                }}>
                                    Дата
                                </div>
                                <div style={{
                                    fontSize: '15px',
                                    fontWeight: '600',
                                    color: colors.textPrimary,
                                }}>
                                    {formatDate(
                                        selectedSession.sessionDate
                                        || selectedSession.startDate
                                    )}
                                </div>
                            </div>
                            <div style={{
                                padding: '12px 16px',
                                backgroundColor: '#FAFAF8',
                                borderRadius: '10px',
                            }}>
                                <div style={{
                                    fontSize: '13px',
                                    color: colors.textMuted,
                                    marginBottom: '2px',
                                }}>
                                    Время
                                </div>
                                <div style={{
                                    fontSize: '15px',
                                    fontWeight: '600',
                                    color: colors.textPrimary,
                                }}>
                                    {formatTime(
                                        selectedSession.startTime
                                    )}{' '}
                                    –{' '}
                                    {formatTime(
                                        selectedSession.endTime
                                    )}
                                </div>
                            </div>
                            {selectedSession.room && (
                                <div style={{
                                    padding: '12px 16px',
                                    backgroundColor: '#FAFAF8',
                                    borderRadius: '10px',
                                }}>
                                    <div style={{
                                        fontSize: '13px',
                                        color: colors.textMuted,
                                    }}>
                                        Зал
                                    </div>
                                    <div style={{
                                        fontSize: '15px',
                                        fontWeight: '600',
                                        color: colors.textPrimary,
                                    }}>
                                        {selectedSession.room}
                                    </div>
                                </div>
                            )}
                            <div style={{
                                padding: '12px 16px',
                                backgroundColor: '#FAFAF8',
                                borderRadius: '10px',
                            }}>
                                <div style={{
                                    fontSize: '13px',
                                    color: colors.textMuted,
                                    marginBottom: SPACING.xs,
                                }}>
                                    Тренер
                                </div>
                                {(() => {
                                    const t = trainers.find(
                                        (tr) => tr.id
                                            === selectedSession.trainerId
                                    );
                                    return t ? (
                                        <button
                                            type="button"
                                            onClick={() =>
                                                navigate('/trainers')
                                            }
                                            style={{
                                                padding: '8px 14px',
                                                borderRadius: '20px',
                                                border: `1px solid ${colors.glassBorder}`,
                                                backgroundColor:
                                                    'white',
                                                color: colors.primaryDark,
                                                fontFamily: FONT_BODY,
                                                fontSize: '13px',
                                                cursor: 'pointer',
                                            }}
                                        >
                                            {t.lastName} {t.firstName}
                                        </button>
                                    ) : (
                                        <span style={{
                                            color: colors.textMuted,
                                        }}>
                                            —
                                        </span>
                                    );
                                })()}
                            </div>
                            {isAdmin && (
                                <button
                                    type="button"
                                    onClick={() =>
                                        handleDelete(
                                            selectedSession.id
                                        )
                                    }
                                    style={{
                                        padding: '12px',
                                        borderRadius: '12px',
                                        border:
                                            `1px solid ${colors.error}`,
                                        cursor: 'pointer',
                                        fontFamily: FONT_BODY,
                                        fontSize: '14px',
                                        fontWeight: '600',
                                        backgroundColor: 'transparent',
                                        color: colors.error,
                                        minHeight: '48px',
                                        transition: 'all 0.2s ease',
                                    }}
                                    onMouseOver={(e) => {
                                        e.currentTarget.style
                                            .backgroundColor =
                                            colors.error;
                                        e.currentTarget.style.color =
                                            '#FFF';
                                    }}
                                    onFocus={(e) => {
                                        e.currentTarget.style
                                            .backgroundColor =
                                            colors.error;
                                        e.currentTarget.style.color =
                                            '#FFF';
                                    }}
                                    onMouseOut={(e) => {
                                        e.currentTarget.style
                                            .backgroundColor =
                                            'transparent';
                                        e.currentTarget.style.color =
                                            colors.error;
                                    }}
                                    onBlur={(e) => {
                                        e.currentTarget.style
                                            .backgroundColor =
                                            'transparent';
                                        e.currentTarget.style.color =
                                            colors.error;
                                    }}
                                >
                                    Удалить тренировку
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default Schedule;