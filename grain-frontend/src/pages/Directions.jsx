import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { workoutTypeApi } from '../api/workoutTypeApi';
import { sessionApi } from '../api/sessionApi';
import { trainerApi } from '../api/trainerApi';
import { useAuth } from '../contexts/useAuth';
import PropTypes from 'prop-types';

const FONT_HEADING = "'Cormorant Garamond', 'Times New Roman', serif";
const FONT_BODY = "'Inter', 'Segoe UI', sans-serif";

const colors = {
    primary: '#979B81',
    primaryDark: '#4D5044',
    primaryHover: '#7E8A6A',
    primaryLight: '#E4E7D6',
    textPrimary: '#0F0F10',
    textSecondary: '#5F6256',
    textMuted: '#6B6F64',
    textOnPrimary: '#FFFFFF',
    error: '#C87B7B',
    success: '#7FA37A',
    shadowCard: '0 8px 32px rgba(77,80,68,0.08)',
    shadowHover: '0 12px 40px rgba(77,80,68,0.14)',
    glassBorder: 'rgba(180,180,170,0.5)',
    backgroundCard: 'rgba(255,255,255,0.65)',
    starFilled: '#D6BFA6',
    starEmpty: '#DADCCD',
    highlightBg: 'rgba(214,191,166,0.4)',
    borderSearch: 'rgba(151,155,129,0.35)',
};

const SPACING = { xs: '6px', sm: '14px', md: '22px', lg: '36px' };
const containerStyle = {
    width: '100%',
    maxWidth: '1200px',
    margin: '0 auto',
    padding: `0 ${SPACING.sm}`,
    boxSizing: 'border-box',
};
const headingStyle = {
    fontSize: 'clamp(26px, 5vw, 36px)',
    fontWeight: '400',
    color: colors.primaryDark,
    textAlign: 'center',
    fontFamily: FONT_HEADING,
    letterSpacing: '0.5px',
    lineHeight: 1.2,
    margin: 0,
};
const infoBlockStyle = {
    padding: '14px 18px',
    borderRadius: '14px',
    fontSize: '14px',
    fontFamily: FONT_BODY,
    marginBottom: SPACING.sm,
    border: '1px solid',
};
const difficultyLabels = {
    BEGINNER: 'Начальный',
    INTERMEDIATE: 'Средний',
    ADVANCED: 'Продвинутый',
    ALL_LEVELS: 'Для всех',
};
const difficultyOptions = [
    { value: 'ALL_LEVELS', label: 'Для всех уровней' },
    { value: 'BEGINNER', label: 'Начальный' },
    { value: 'INTERMEDIATE', label: 'Средний' },
    { value: 'ADVANCED', label: 'Продвинутый' },
];
const difficultyStars = {
    ALL_LEVELS: 0,
    BEGINNER: 1,
    INTERMEDIATE: 2,
    ADVANCED: 3,
};
const emptyForm = {
    id: null,
    name: '',
    description: '',
    difficultyLevel: 'ALL_LEVELS',
    contraindications: '',
    benefits: [],
    iconPath: '',
    isActive: true,
    trainerIds: [],
};

const dropdownBtnStyle = (open) => ({
    width: '100%',
    padding: '12px 16px',
    fontSize: '14px',
    fontFamily: FONT_BODY,
    fontWeight: '500',
    textAlign: 'left',
    border: '1px solid rgba(180,180,170,0.5)',
    borderRadius: open ? '10px 10px 0 0' : '10px',
    outline: 'none',
    backgroundColor: '#FAFAF8',
    color: colors.textPrimary,
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    boxSizing: 'border-box',
});

const dropdownOptStyle = (active) => ({
    width: '100%',
    padding: '12px 16px',
    fontSize: '14px',
    fontFamily: FONT_BODY,
    fontWeight: active ? '600' : '400',
    color: active ? colors.primaryDark : colors.textSecondary,
    backgroundColor: active ? colors.primaryLight : 'white',
    border: 'none',
    cursor: 'pointer',
    textAlign: 'left',
    transition: 'background-color 0.15s ease',
});

const errorTextStyle = {
    color: colors.error,
    fontSize: '12px',
    margin: '4px 0 0',
    fontFamily: FONT_BODY,
};

const inputStyle = {
    width: '100%',
    padding: '12px 16px',
    fontSize: '14px',
    fontFamily: FONT_BODY,
    border: '1px solid rgba(180,180,170,0.5)',
    borderRadius: '10px',
    outline: 'none',
    backgroundColor: '#FAFAF8',
    color: colors.textPrimary,
    boxSizing: 'border-box',
};

const inputErrorStyle = {
    ...inputStyle,
    border: '1px solid #C87B7B',
    backgroundColor: '#FFF8F8',
};

const cardImageStyle = {
    width: '100%',
    height: '140px',
    objectFit: 'cover',
    flexShrink: 0,
};

const cardPlaceholderStyle = {
    width: '100%',
    height: '80px',
    flexShrink: 0,
    backgroundColor: colors.primaryLight,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
};

const cardContentStyle = {
    padding: SPACING.md,
    display: 'flex',
    flexDirection: 'column',
    gap: SPACING.sm,
    flex: 1,
};

const cardDescriptionStyle = {
    fontSize: '14px',
    color: colors.textSecondary,
    fontFamily: FONT_BODY,
    lineHeight: 1.5,
    margin: 0,
    display: '-webkit-box',
    WebkitLineClamp: 3,
    WebkitBoxOrient: 'vertical',
    overflow: 'hidden',
    minHeight: '63px',
};

const EditIcon = () => (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
    </svg>
);

const PlusIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <line x1="12" y1="5" x2="12" y2="19" />
        <line x1="5" y1="12" x2="19" y2="12" />
    </svg>
);

const Stars = ({ count }) => (
    <span style={{
        display: 'inline-flex',
        gap: '4px',
        marginLeft: '8px',
        verticalAlign: 'middle',
    }}>
        {[1, 2, 3].map((s) => (
            <svg key={s} width="16" height="16" viewBox="0 0 24 24"
                 fill={s <= count ? colors.starFilled : 'transparent'}
                 stroke={s <= count ? colors.starFilled : colors.starEmpty}
                 strokeWidth="1.5">
                <polygon
                    points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"
                />
            </svg>
        ))}
    </span>
);

Stars.propTypes = { count: PropTypes.number.isRequired };

const formatDate = (d) => {
    if (!d) return '';
    const dt = new Date(d);
    return dt.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'long',
        weekday: 'short',
    });
};

const formatTime = (t) => t ? t.slice(0, 5) : '';

const highlightText = (text, query) => {
    if (!query || !text) return text;
    const escaped = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const regex = new RegExp(`(${escaped})`, 'gi');
    const parts = text.split(regex);
    return parts.map((part, i) =>
        regex.test(part)
            ? <mark key={i} style={{
                backgroundColor: colors.highlightBg,
                borderRadius: '3px',
                padding: '0 2px',
            }}>{part}</mark>
            : part
    );
};

const getAdminButtonStyle = (baseColor) => ({
    width: '32px',
    height: '32px',
    borderRadius: '6px',
    color: baseColor,
    backgroundColor: 'rgba(255,255,255,0.6)',
    border: `1px solid ${colors.glassBorder}`,
    cursor: 'pointer',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    transition: 'all 0.2s ease',
});


const applyHover = (e, active) => {
    if (active) {
        e.currentTarget.style.backgroundColor = colors.primaryLight;
    } else {
        e.currentTarget.style.backgroundColor = 'white';
    }
};

function Directions() {
    const [workoutTypes, setWorkoutTypes] = useState([]);
    const [allTrainers, setAllTrainers] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [difficultyFilter, setDifficultyFilter] = useState('ALL');
    const [difficultyOpen, setDifficultyOpen] = useState(false);
    const difficultyRef = useRef(null);
    const [selectedType, setSelectedType] = useState(null);
    const [upcomingSessions, setUpcomingSessions] = useState([]);
    const [typeTrainers, setTypeTrainers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [editingForm, setEditingForm] = useState(emptyForm);
    const [showForm, setShowForm] = useState(false);
    const [isNew, setIsNew] = useState(true);
    const [saving, setSaving] = useState(false);
    const [benefitInput, setBenefitInput] = useState('');
    const [diffOpen, setDiffOpen] = useState(false);
    const [trainerOpen, setTrainerOpen] = useState(false);
    const [fieldErrors, setFieldErrors] = useState({});
    const diffRef = useRef(null);
    const trainerRef = useRef(null);
    const fileInputRef = useRef(null);
    const { user } = useAuth();
    const isAdmin = user?.role === 'ADMIN';
    const navigate = useNavigate();

    useEffect(() => {
        const handler = (e) => {
            if (difficultyRef.current && !difficultyRef.current.contains(e.target)) {
                setDifficultyOpen(false);
            }
            if (diffRef.current && !diffRef.current.contains(e.target)) {
                setDiffOpen(false);
            }
            if (trainerRef.current && !trainerRef.current.contains(e.target)) {
                setTrainerOpen(false);
            }
        };
        document.addEventListener('mousedown', handler);
        return () => document.removeEventListener('mousedown', handler);
    }, []);

    const diffFilterOptions = [
        { value: 'ALL', label: 'Любая сложность' },
        ...difficultyOptions,
    ];

    const currentLabel = diffFilterOptions.find(
        (o) => o.value === difficultyFilter
    )?.label || 'Любая сложность';

    const fetchData = useCallback(async () => {
        try {
            const [t, tr] = await Promise.all([
                workoutTypeApi.getActive(),
                trainerApi.getAll(),
            ]);
            setWorkoutTypes(t.data);
            setAllTrainers(tr.data);
        } catch {
            setError('Не удалось загрузить направления');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        let cancelled = false;
        if (!cancelled) fetchData();
        return () => { cancelled = true; };
    }, [fetchData]);

    const filteredTypes = workoutTypes.filter((wt) => {
        const query = searchQuery.toLowerCase();
        const matchesSearch = !searchQuery
            || wt.name.toLowerCase().includes(query)
            || (wt.description && wt.description.toLowerCase().includes(query));
        const matchesDifficulty = difficultyFilter === 'ALL'
            || wt.difficultyLevel === difficultyFilter;
        return matchesSearch && matchesDifficulty;
    });

    const handleOpenModal = useCallback(async (type) => {
        setSelectedType(type);
        try {
            const [s, tr] = await Promise.all([
                sessionApi.getUpcomingByWorkoutType(type.id),
                workoutTypeApi.getTrainers(type.id),
            ]);
            setUpcomingSessions(s.data || []);
            setTypeTrainers(tr.data || []);
        } catch {
            setUpcomingSessions([]);
            setTypeTrainers([]);
        }
    }, []);

    const handleCloseModal = useCallback(() => {
        setSelectedType(null);
        setUpcomingSessions([]);
        setTypeTrainers([]);
    }, []);

    useEffect(() => {
        if (workoutTypes.length === 0) return;
        const params = new URLSearchParams(window.location.search);
        const openId = params.get('open');
        if (openId) {
            const type = workoutTypes.find((t) => t.id === parseInt(openId));
            if (type) handleOpenModal(type);
        }
    }, [workoutTypes, handleOpenModal]);

    const handleEdit = async (type, e) => {
        e.stopPropagation();
        try {
            const res = await workoutTypeApi.getTrainers(type.id);
            setEditingForm({
                id: type.id,
                name: type.name || '',
                description: type.description || '',
                difficultyLevel: type.difficultyLevel || 'ALL_LEVELS',
                contraindications: type.contraindications || '',
                benefits: type.benefits || [],
                iconPath: type.iconPath || '',
                isActive: type.isActive ?? true,
                trainerIds: (res.data || []).map((t) => t.id),
            });
        } catch {
            setEditingForm({
                id: type.id,
                name: type.name || '',
                description: type.description || '',
                difficultyLevel: type.difficultyLevel || 'ALL_LEVELS',
                contraindications: type.contraindications || '',
                benefits: type.benefits || [],
                iconPath: type.iconPath || '',
                isActive: type.isActive ?? true,
                trainerIds: [],
            });
        }
        setIsNew(false);
        setShowForm(true);
        setFieldErrors({});
        setError('');
        setSuccess('');
    };

    const handleNew = () => {
        setEditingForm(emptyForm);
        setIsNew(true);
        setShowForm(true);
        setFieldErrors({});
        setError('');
        setSuccess('');
    };

    const handleCancel = () => {
        setEditingForm(emptyForm);
        setShowForm(false);
        setIsNew(true);
        setBenefitInput('');
        setFieldErrors({});
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setEditingForm((prev) => ({ ...prev, [name]: value }));
        if (fieldErrors[name]) {
            setFieldErrors((prev) => {
                const next = { ...prev };
                delete next[name];
                return next;
            });
        }
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = () => setEditingForm(
            (prev) => ({ ...prev, iconPath: reader.result })
        );
        reader.readAsDataURL(file);
    };

    const addBenefit = () => {
        const trimmed = benefitInput.trim();
        if (trimmed && !editingForm.benefits.includes(trimmed)) {
            setEditingForm((prev) => ({
                ...prev,
                benefits: [...prev.benefits, trimmed],
            }));
        }
        setBenefitInput('');
    };

    const removeBenefit = (i) => setEditingForm((prev) => ({
        ...prev,
        benefits: prev.benefits.filter((_, idx) => idx !== i),
    }));

    const toggleFormTrainer = (tid) => setEditingForm((prev) => ({
        ...prev,
        trainerIds: prev.trainerIds.includes(tid)
            ? prev.trainerIds.filter((id) => id !== tid)
            : [...prev.trainerIds, tid],
    }));

    const handleSave = async () => {
        const errors = {};
        if (!editingForm.name.trim()) {
            errors.name = 'Название обязательно';
        }
        setFieldErrors(errors);
        if (Object.keys(errors).length > 0) return;

        setSaving(true);
        setError('');
        setSuccess('');
        try {
            const payload = {
                name: editingForm.name,
                description: editingForm.description,
                difficultyLevel: editingForm.difficultyLevel,
                contraindications: editingForm.contraindications,
                benefits: editingForm.benefits,
                iconPath: editingForm.iconPath,
                isActive: editingForm.isActive,
                trainerIds: editingForm.trainerIds,
            };
            if (isNew) {
                await workoutTypeApi.create(payload);
            } else {
                await workoutTypeApi.update(editingForm.id, payload);
            }
            setSuccess(isNew ? 'Направление создано' : 'Направление обновлено');
            setShowForm(false);
            setEditingForm(emptyForm);
            setIsNew(true);
            setBenefitInput('');
            fetchData();
        } catch (err2) {
            setError(
                err2.response?.data?.message || 'Не удалось сохранить направление'
            );
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id, e) => {
        e.stopPropagation();
        if (!id) return;
        if (!window.confirm('Удалить направление?')) return;
        try {
            await workoutTypeApi.delete(id);
            fetchData();
            setSuccess('Направление удалено');
        } catch (err) {
            const message = err.response?.data?.message || 'Не удалось удалить направление';
            setError(message);
        }
    };

    const adminBtnHoverIn = (e) => {
        e.currentTarget.style.backgroundColor = colors.primaryDark;
        e.currentTarget.style.color = '#FFFFFF';
    };

    const adminBtnHoverOut = (e) => {
        e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.6)';
        e.currentTarget.style.color = colors.primaryDark;
    };

    if (loading) {
        return (
            <div style={{
                minHeight: '60vh',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontFamily: FONT_BODY,
                color: colors.textMuted,
                fontSize: '16px',
            }}>
                Загрузка...
            </div>
        );
    }

    const modalOverlayStyle = {
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(0,0,0,0.3)',
        backdropFilter: 'blur(4px)',
        zIndex: 100,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: SPACING.sm,
    };

    const modalContentStyle = {
        backgroundColor: 'white',
        borderRadius: '24px',
        padding: `${SPACING.lg} ${SPACING.md}`,
        maxWidth: '640px',
        width: '100%',
        maxHeight: '85vh',
        overflowY: 'auto',
        boxShadow: '0 20px 60px rgba(0,0,0,0.15)',
        fontFamily: FONT_BODY,
    };

    const sessionButtonStyle = {
        display: 'flex',
        justifyContent: 'space-between',
        padding: '10px 12px',
        backgroundColor: 'white',
        borderRadius: '10px',
        fontSize: '13px',
        fontFamily: FONT_BODY,
        color: colors.primaryDark,
        border: `1px solid ${colors.glassBorder}`,
        cursor: 'pointer',
        width: '100%',
        textAlign: 'left',
    };

    return (
        <>
            <div style={{ padding: `${SPACING.sm} ${SPACING.sm} 0` }}>
                <div style={containerStyle}>
                    <div style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: SPACING.xs,
                        marginBottom: SPACING.sm,
                    }}>
                        <h1 style={headingStyle}>Направления</h1>
                        {isAdmin && !showForm && (
                            <button
                                type="button"
                                onClick={handleNew}
                                aria-label="Добавить направление"
                                style={getAdminButtonStyle(colors.primaryDark)}
                                onMouseOver={adminBtnHoverIn}
                                onFocus={adminBtnHoverIn}
                                onMouseOut={adminBtnHoverOut}
                                onBlur={adminBtnHoverOut}
                            >
                                <PlusIcon />
                            </button>
                        )}
                    </div>

                    {success && (
                        <div style={{
                            padding: '12px 16px',
                            backgroundColor: '#F0F7F0',
                            borderRadius: '10px',
                            color: colors.success,
                            fontFamily: FONT_BODY,
                            fontSize: '14px',
                            marginBottom: SPACING.sm,
                            textAlign: 'center',
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
                                    fontSize: '24px',
                                    textAlign: 'center',
                                    marginTop: 0,
                                    marginBottom: SPACING.md,
                                }}>
                                    {isNew
                                        ? 'Новое направление'
                                        : 'Редактирование направления'}
                                </h3>

                                {error && (
                                    <div style={{
                                        padding: '12px 16px',
                                        backgroundColor: '#FFF0F0',
                                        borderRadius: '10px',
                                        color: colors.error,
                                        fontFamily: FONT_BODY,
                                        fontSize: '14px',
                                        marginBottom: SPACING.sm,
                                    }}>
                                        {error}
                                    </div>
                                )}

                                <div style={{
                                    display: 'grid',
                                    gap: SPACING.sm,
                                    gridTemplateColumns: '1fr 1fr',
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
                                            Название
                                        </label>
                                        <input
                                            name="name"
                                            value={editingForm.name}
                                            onChange={handleChange}
                                            style={
                                                fieldErrors.name
                                                    ? inputErrorStyle
                                                    : inputStyle
                                            }
                                        />
                                        {fieldErrors.name && (
                                            <p style={errorTextStyle}>
                                                {fieldErrors.name}
                                            </p>
                                        )}
                                    </div>

                                    <div style={{ position: 'relative' }}
                                         ref={diffRef}>
                                        <label style={{
                                            display: 'block',
                                            marginBottom: '4px',
                                            fontSize: '13px',
                                            fontWeight: '600',
                                            fontFamily: FONT_BODY,
                                            color: colors.textSecondary,
                                        }}>
                                            Сложность
                                        </label>
                                        <button
                                            type="button"
                                            onClick={() => setDiffOpen(!diffOpen)}
                                            style={dropdownBtnStyle(diffOpen)}
                                        >
                                            {difficultyOptions.find(
                                                (o) => o.value === editingForm.difficultyLevel
                                            )?.label || 'Для всех уровней'}
                                            <svg
                                                width="14" height="14"
                                                viewBox="0 0 24 24" fill="none"
                                                stroke={colors.primaryDark}
                                                strokeWidth="2"
                                                strokeLinecap="round"
                                                strokeLinejoin="round"
                                                style={{
                                                    transform: diffOpen
                                                        ? 'rotate(180deg)'
                                                        : 'rotate(0)',
                                                    transition: 'transform 0.2s ease',
                                                    marginLeft: '12px',
                                                    flexShrink: 0,
                                                }}
                                            >
                                                <polyline points="6 9 12 15 18 9" />
                                            </svg>
                                        </button>
                                        {diffOpen && (
                                            <div style={{
                                                position: 'absolute',
                                                top: '100%',
                                                left: 0,
                                                right: 0,
                                                zIndex: 10,
                                                backgroundColor: 'white',
                                                border: '1px solid rgba(180,180,170,0.5)',
                                                borderTop: 'none',
                                                borderRadius: '0 0 10px 10px',
                                                boxShadow:
                                                    '0 8px 24px rgba(77,80,68,0.12)',
                                                overflow: 'hidden',
                                            }}>
                                                {difficultyOptions.map((o) => (
                                                    <button
                                                        key={o.value}
                                                        type="button"
                                                        onClick={() => {
                                                            setEditingForm((prev) => ({
                                                                ...prev,
                                                                difficultyLevel: o.value,
                                                            }));
                                                            setDiffOpen(false);
                                                        }}
                                                        style={dropdownOptStyle(
                                                            o.value === editingForm.difficultyLevel
                                                        )}
                                                        onMouseOver={(e) =>
                                                            e.currentTarget.style.backgroundColor =
                                                                colors.primaryLight
                                                        }
                                                        onMouseOut={(e) =>
                                                            applyHover(
                                                                e,
                                                                o.value === editingForm.difficultyLevel
                                                            )
                                                        }
                                                    >
                                                        {o.label}
                                                    </button>
                                                ))}
                                            </div>
                                        )}
                                    </div>
                                </div>

                                <div style={{ marginTop: SPACING.sm }}>
                                    <label style={{
                                        display: 'block',
                                        marginBottom: '4px',
                                        fontSize: '13px',
                                        fontWeight: '600',
                                        fontFamily: FONT_BODY,
                                        color: colors.textSecondary,
                                    }}>
                                        Иконка
                                    </label>
                                    <button
                                        type="button"
                                        onClick={() => fileInputRef.current?.click()}
                                        style={{
                                            width: '100%',
                                            padding: '12px 16px',
                                            fontSize: '14px',
                                            fontFamily: FONT_BODY,
                                            border: '1px dashed rgba(180,180,170,0.5)',
                                            borderRadius: '10px',
                                            backgroundColor: '#FAFAF8',
                                            color: colors.textMuted,
                                            cursor: 'pointer',
                                            textAlign: 'left',
                                            minHeight: '48px',
                                        }}
                                    >
                                        {editingForm.iconPath
                                            ? 'Иконка выбрана'
                                            : 'Выбрать файл...'}
                                    </button>
                                    <input
                                        type="file"
                                        accept="image/*"
                                        ref={fileInputRef}
                                        onChange={handleFileChange}
                                        style={{ display: 'none' }}
                                    />
                                    {editingForm.iconPath && (
                                        <img
                                            src={editingForm.iconPath}
                                            alt=""
                                            style={{
                                                width: '100%',
                                                maxHeight: '120px',
                                                objectFit: 'contain',
                                                marginTop: SPACING.xs,
                                                border: `1px solid ${colors.glassBorder}`,
                                                borderRadius: '8px',
                                            }}
                                        />
                                    )}
                                </div>

                                <div style={{
                                    marginTop: SPACING.sm,
                                    position: 'relative',
                                }} ref={trainerRef}>
                                    <label style={{
                                        display: 'block',
                                        marginBottom: '4px',
                                        fontSize: '13px',
                                        fontWeight: '600',
                                        fontFamily: FONT_BODY,
                                        color: colors.textSecondary,
                                    }}>
                                        Тренеры
                                    </label>
                                    {!isNew && editingForm.trainerIds.length > 0 && (
                                        <div style={{
                                            display: 'flex',
                                            flexWrap: 'wrap',
                                            gap: SPACING.xs,
                                            marginBottom: SPACING.xs,
                                        }}>
                                            {editingForm.trainerIds.map((tid) => {
                                                const tr = allTrainers.find(
                                                    (t) => t.id === tid
                                                );
                                                if (!tr) return null;
                                                return (
                                                    <span key={tid} style={{
                                                        display: 'inline-flex',
                                                        alignItems: 'center',
                                                        gap: '4px',
                                                        padding: '6px 12px',
                                                        backgroundColor: colors.primaryLight,
                                                        borderRadius: '20px',
                                                        fontSize: '13px',
                                                        fontFamily: FONT_BODY,
                                                        color: colors.primaryDark,
                                                    }}>
                                                        {tr.lastName} {tr.firstName}
                                                        <button
                                                            type="button"
                                                            onClick={() =>
                                                                toggleFormTrainer(tid)
                                                            }
                                                            style={{
                                                                background: 'none',
                                                                border: 'none',
                                                                cursor: 'pointer',
                                                                color: colors.error,
                                                                fontSize: '16px',
                                                                lineHeight: 1,
                                                                padding: '0 2px',
                                                            }}
                                                        >
                                                            ×
                                                        </button>
                                                    </span>
                                                );
                                            })}
                                        </div>
                                    )}
                                    <button
                                        type="button"
                                        onClick={() =>
                                            setTrainerOpen(!trainerOpen)
                                        }
                                        style={dropdownBtnStyle(trainerOpen)}
                                    >
                                        Выбрать тренеров
                                        <svg
                                            width="14" height="14"
                                            viewBox="0 0 24 24" fill="none"
                                            stroke={colors.primaryDark}
                                            strokeWidth="2"
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            style={{
                                                transform: trainerOpen
                                                    ? 'rotate(180deg)'
                                                    : 'rotate(0)',
                                                transition: 'transform 0.2s ease',
                                                marginLeft: '12px',
                                                flexShrink: 0,
                                            }}
                                        >
                                            <polyline points="6 9 12 15 18 9" />
                                        </svg>
                                    </button>
                                    {trainerOpen && (
                                        <div style={{
                                            position: 'absolute',
                                            top: '100%',
                                            left: 0,
                                            right: 0,
                                            zIndex: 20,
                                            backgroundColor: 'white',
                                            border: '1px solid rgba(180,180,170,0.5)',
                                            borderTop: 'none',
                                            borderRadius: '0 0 10px 10px',
                                            boxShadow:
                                                '0 8px 24px rgba(77,80,68,0.12)',
                                            overflow: 'hidden',
                                            maxHeight: '200px',
                                            overflowY: 'auto',
                                        }}>
                                            {allTrainers
                                                .filter((tr) =>
                                                    tr.status !== 'VACATION'
                                                )
                                                .map((tr) => (
                                                    <button
                                                        key={tr.id}
                                                        type="button"
                                                        onClick={() =>
                                                            toggleFormTrainer(tr.id)
                                                        }
                                                        style={{
                                                            ...dropdownOptStyle(
                                                                editingForm.trainerIds.includes(tr.id)
                                                            ),
                                                            display: 'flex',
                                                            alignItems: 'center',
                                                            gap: '8px',
                                                            width: '100%',
                                                        }}
                                                        onMouseOver={(e) =>
                                                            e.currentTarget.style.backgroundColor =
                                                                colors.primaryLight
                                                        }
                                                        onMouseOut={(e) =>
                                                            applyHover(
                                                                e,
                                                                editingForm.trainerIds.includes(tr.id)
                                                            )
                                                        }
                                                    >
                                                        <span style={{ fontSize: '18px' }}>
                                                            {editingForm.trainerIds.includes(tr.id)
                                                                ? '✓' : '○'}
                                                        </span>
                                                        {tr.lastName} {tr.firstName}
                                                    </button>
                                                ))}
                                        </div>
                                    )}
                                </div>

                                {isNew && editingForm.trainerIds.length > 0 && (
                                    <div style={{
                                        display: 'flex',
                                        flexWrap: 'wrap',
                                        gap: SPACING.xs,
                                        marginTop: SPACING.xs,
                                    }}>
                                        {editingForm.trainerIds.map((tid) => {
                                            const tr = allTrainers.find(
                                                (t) => t.id === tid
                                            );
                                            if (!tr) return null;
                                            return (
                                                <span key={tid} style={{
                                                    display: 'inline-flex',
                                                    alignItems: 'center',
                                                    gap: '4px',
                                                    padding: '6px 12px',
                                                    backgroundColor: colors.primaryLight,
                                                    borderRadius: '20px',
                                                    fontSize: '13px',
                                                    fontFamily: FONT_BODY,
                                                    color: colors.primaryDark,
                                                }}>
                                                    {tr.lastName} {tr.firstName}
                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            toggleFormTrainer(tid)
                                                        }
                                                        style={{
                                                            background: 'none',
                                                            border: 'none',
                                                            cursor: 'pointer',
                                                            color: colors.error,
                                                            fontSize: '16px',
                                                            lineHeight: 1,
                                                            padding: '0 2px',
                                                        }}
                                                    >
                                                        ×
                                                    </button>
                                                </span>
                                            );
                                        })}
                                    </div>
                                )}

                                <div style={{ marginTop: SPACING.sm }}>
                                    <label style={{
                                        display: 'block',
                                        marginBottom: '4px',
                                        fontSize: '13px',
                                        fontWeight: '600',
                                        fontFamily: FONT_BODY,
                                        color: colors.textSecondary,
                                    }}>
                                        Описание
                                    </label>
                                    <textarea
                                        name="description"
                                        value={editingForm.description}
                                        onChange={handleChange}
                                        style={{
                                            width: '100%',
                                            padding: '12px 16px',
                                            fontSize: '14px',
                                            fontFamily: FONT_BODY,
                                            border:
                                                '1px solid rgba(180,180,170,0.5)',
                                            borderRadius: '10px',
                                            outline: 'none',
                                            backgroundColor: '#FAFAF8',
                                            color: colors.textPrimary,
                                            boxSizing: 'border-box',
                                            minHeight: '80px',
                                            resize: 'vertical',
                                        }}
                                    />
                                </div>

                                <div style={{ marginTop: SPACING.sm }}>
                                    <label style={{
                                        display: 'block',
                                        marginBottom: '4px',
                                        fontSize: '13px',
                                        fontWeight: '600',
                                        fontFamily: FONT_BODY,
                                        color: colors.textSecondary,
                                    }}>
                                        Противопоказания
                                    </label>
                                    <textarea
                                        name="contraindications"
                                        value={editingForm.contraindications}
                                        onChange={handleChange}
                                        style={{
                                            width: '100%',
                                            padding: '12px 16px',
                                            fontSize: '14px',
                                            fontFamily: FONT_BODY,
                                            border:
                                                '1px solid rgba(180,180,170,0.5)',
                                            borderRadius: '10px',
                                            outline: 'none',
                                            backgroundColor: '#FAFAF8',
                                            color: colors.textPrimary,
                                            boxSizing: 'border-box',
                                            minHeight: '60px',
                                            resize: 'vertical',
                                        }}
                                    />
                                </div>

                                <div style={{ marginTop: SPACING.sm }}>
                                    <label style={{
                                        display: 'block',
                                        marginBottom: '4px',
                                        fontSize: '13px',
                                        fontWeight: '600',
                                        fontFamily: FONT_BODY,
                                        color: colors.textSecondary,
                                    }}>
                                        Преимущества
                                    </label>
                                    <div style={{
                                        display: 'flex',
                                        gap: SPACING.xs,
                                        marginBottom: SPACING.xs,
                                    }}>
                                        <input
                                            value={benefitInput}
                                            onChange={(e) =>
                                                setBenefitInput(e.target.value)
                                            }
                                            style={{
                                                flex: 1,
                                                padding: '12px 16px',
                                                fontSize: '14px',
                                                fontFamily: FONT_BODY,
                                                border:
                                                    '1px solid rgba(180,180,170,0.5)',
                                                borderRadius: '10px',
                                                outline: 'none',
                                                backgroundColor: '#FAFAF8',
                                                color: colors.textPrimary,
                                            }}
                                            placeholder="Добавить преимущество"
                                            onKeyDown={(e) => {
                                                if (e.key === 'Enter') {
                                                    e.preventDefault();
                                                    addBenefit();
                                                }
                                            }}
                                        />
                                        <button
                                            type="button"
                                            onClick={addBenefit}
                                            style={{
                                                padding: '12px 16px',
                                                borderRadius: '10px',
                                                border: 'none',
                                                backgroundColor: colors.primaryDark,
                                                color: '#FFF',
                                                fontFamily: FONT_BODY,
                                                fontSize: '14px',
                                                fontWeight: '600',
                                                cursor: 'pointer',
                                            }}
                                        >
                                            +
                                        </button>
                                    </div>
                                    {editingForm.benefits.length > 0 && (
                                        <div style={{
                                            display: 'flex',
                                            flexWrap: 'wrap',
                                            gap: SPACING.xs,
                                        }}>
                                            {editingForm.benefits.map((b, i) => (
                                                <span key={i} style={{
                                                    display: 'inline-flex',
                                                    alignItems: 'center',
                                                    gap: '4px',
                                                    padding: '6px 12px',
                                                    backgroundColor: colors.primaryLight,
                                                    borderRadius: '8px',
                                                    fontSize: '13px',
                                                    fontFamily: FONT_BODY,
                                                    color: colors.primaryDark,
                                                }}>
                                                    {b}
                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            removeBenefit(i)
                                                        }
                                                        style={{
                                                            background: 'none',
                                                            border: 'none',
                                                            cursor: 'pointer',
                                                            color: colors.error,
                                                            fontSize: '16px',
                                                            lineHeight: 1,
                                                            padding: '0 2px',
                                                        }}
                                                    >
                                                        ×
                                                    </button>
                                                </span>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                <div style={{
                                    display: 'flex',
                                    gap: SPACING.sm,
                                    justifyContent: 'flex-end',
                                    marginTop: SPACING.md,
                                }}>
                                    <button
                                        type="button"
                                        onClick={handleCancel}
                                        style={{
                                            padding: '12px 22px',
                                            borderRadius: '12px',
                                            border: `1px solid ${colors.error}`,
                                            backgroundColor: 'transparent',
                                            color: colors.error,
                                            fontFamily: FONT_BODY,
                                            fontSize: '14px',
                                            fontWeight: '600',
                                            cursor: 'pointer',
                                            minHeight: '48px',
                                        }}
                                    >
                                        Отмена
                                    </button>
                                    <button
                                        type="button"
                                        onClick={handleSave}
                                        disabled={saving}
                                        style={{
                                            padding: '12px 22px',
                                            borderRadius: '12px',
                                            border: 'none',
                                            backgroundColor: colors.primaryDark,
                                            color: '#FFF',
                                            fontFamily: FONT_BODY,
                                            fontSize: '14px',
                                            fontWeight: '600',
                                            cursor: 'pointer',
                                            minHeight: '48px',
                                            opacity: saving ? 0.7 : 1,
                                        }}
                                    >
                                        {saving
                                            ? 'Сохранение...'
                                            : isNew ? 'Создать' : 'Сохранить'}
                                    </button>
                                </div>
                            </div>
                        </div>
                    )}

                    <div style={{
                        display: 'flex',
                        justifyContent: 'center',
                        gap: SPACING.xs,
                        marginBottom: SPACING.lg,
                        marginTop: SPACING.sm,
                        flexWrap: 'wrap',
                    }}>
                        <input
                            type="text"
                            placeholder="Поиск по названию или описанию..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            style={{
                                width: '100%',
                                maxWidth: '380px',
                                padding: '14px 20px',
                                fontSize: '15px',
                                fontFamily: FONT_BODY,
                                border: `2px solid ${colors.borderSearch}`,
                                borderRadius: '16px',
                                outline: 'none',
                                backgroundColor: 'rgba(255,255,255,0.8)',
                                color: colors.textPrimary,
                                boxShadow:
                                    '0 2px 12px rgba(151,155,129,0.08)',
                            }}
                        />
                        <div style={{
                            position: 'relative',
                            minWidth: '180px',
                        }} ref={difficultyRef}>
                            <button
                                type="button"
                                onClick={() =>
                                    setDifficultyOpen(!difficultyOpen)
                                }
                                style={{
                                    width: '100%',
                                    padding: '14px 18px 14px 16px',
                                    fontSize: '15px',
                                    fontFamily: FONT_BODY,
                                    fontWeight: '500',
                                    border:
                                        `2px solid ${colors.borderSearch}`,
                                    borderRadius: difficultyOpen
                                        ? '16px 16px 0 0'
                                        : '16px',
                                    outline: 'none',
                                    backgroundColor: 'rgba(255,255,255,0.8)',
                                    color: colors.textPrimary,
                                    boxShadow:
                                        '0 2px 12px rgba(151,155,129,0.08)',
                                    cursor: 'pointer',
                                    textAlign: 'left',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'space-between',
                                }}
                            >
                                {currentLabel}
                                <svg
                                    width="14" height="14"
                                    viewBox="0 0 24 24" fill="none"
                                    stroke={colors.primaryDark}
                                    strokeWidth="2"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    style={{
                                        transform: difficultyOpen
                                            ? 'rotate(180deg)'
                                            : 'rotate(0)',
                                        transition: 'transform 0.2s ease',
                                        marginLeft: '12px',
                                        flexShrink: 0,
                                    }}
                                >
                                    <polyline points="6 9 12 15 18 9" />
                                </svg>
                            </button>
                            {difficultyOpen && (
                                <div style={{
                                    position: 'absolute',
                                    top: '100%',
                                    left: 0,
                                    right: 0,
                                    zIndex: 10,
                                    backgroundColor: 'white',
                                    border:
                                        `2px solid ${colors.borderSearch}`,
                                    borderTop: 'none',
                                    borderRadius: '0 0 16px 16px',
                                    boxShadow:
                                        '0 8px 24px rgba(77,80,68,0.12)',
                                    overflow: 'hidden',
                                }}>
                                    {diffFilterOptions.map((o) => (
                                        <button
                                            key={o.value}
                                            type="button"
                                            onClick={() => {
                                                setDifficultyFilter(o.value);
                                                setDifficultyOpen(false);
                                            }}
                                            style={{
                                                width: '100%',
                                                padding: '12px 16px',
                                                fontSize: '14px',
                                                fontFamily: FONT_BODY,
                                                fontWeight:
                                                    o.value === difficultyFilter
                                                        ? '600'
                                                        : '400',
                                                color:
                                                    o.value === difficultyFilter
                                                        ? colors.primaryDark
                                                        : colors.textSecondary,
                                                backgroundColor:
                                                    o.value === difficultyFilter
                                                        ? colors.primaryLight
                                                        : 'white',
                                                border: 'none',
                                                cursor: 'pointer',
                                                textAlign: 'left',
                                                transition:
                                                    'background-color 0.15s ease',
                                            }}
                                            onMouseOver={(e) =>
                                                e.currentTarget.style.backgroundColor =
                                                    colors.primaryLight
                                            }
                                            onMouseOut={(e) => {
                                                e.currentTarget.style.backgroundColor =
                                                    o.value === difficultyFilter
                                                        ? colors.primaryLight
                                                        : 'white';
                                            }}
                                        >
                                            {o.label}
                                        </button>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>

                    {filteredTypes.length === 0 ? (
                        <p style={{
                            textAlign: 'center',
                            color: colors.textMuted,
                            fontFamily: FONT_BODY,
                            fontSize: '20px',
                            padding: SPACING.lg,
                        }}>
                            Ничего не найдено
                        </p>
                    ) : (
                        <div style={{
                            display: 'grid',
                            gridTemplateColumns:
                                'repeat(auto-fill, minmax(300px, 1fr))',
                            gap: SPACING.md,
                            paddingBottom: SPACING.lg,
                            alignItems: 'stretch',
                        }}>
                            {filteredTypes.map((type) => (
                                <div key={type.id || type.name}
                                     style={{
                                         position: 'relative',
                                         height: '100%',
                                     }}>
                                    <button
                                        type="button"
                                        onClick={() => handleOpenModal(type)}
                                        style={{
                                            backgroundColor: colors.backgroundCard,
                                            backdropFilter: 'blur(16px)',
                                            WebkitBackdropFilter: 'blur(16px)',
                                            borderRadius: '20px',
                                            border:
                                                `1px solid ${colors.glassBorder}`,
                                            boxShadow: colors.shadowCard,
                                            padding: 0,
                                            display: 'flex',
                                            flexDirection: 'column',
                                            cursor: 'pointer',
                                            textAlign: 'left',
                                            width: '100%',
                                            height: '100%',
                                            transition:
                                                'box-shadow 0.3s ease',
                                            overflow: 'hidden',
                                        }}
                                        onMouseOver={(e) =>
                                            e.currentTarget.style.boxShadow =
                                                colors.shadowHover
                                        }
                                        onFocus={(e) =>
                                            e.currentTarget.style.boxShadow =
                                                colors.shadowHover
                                        }
                                        onMouseOut={(e) =>
                                            e.currentTarget.style.boxShadow =
                                                colors.shadowCard
                                        }
                                        onBlur={(e) =>
                                            e.currentTarget.style.boxShadow =
                                                colors.shadowCard
                                        }
                                    >
                                        {type.iconPath ? (
                                            <img
                                                src={type.iconPath}
                                                alt=""
                                                style={cardImageStyle}
                                            />
                                        ) : (
                                            <div style={cardPlaceholderStyle}>
                                                <span style={{
                                                    fontSize: '28px',
                                                    color: colors.primary,
                                                    fontFamily: FONT_HEADING,
                                                }}>
                                                    {type.name?.charAt(0) || '?'}
                                                </span>
                                            </div>
                                        )}
                                        <div style={cardContentStyle}>
                                            <h3 style={{
                                                fontSize: '20px',
                                                fontWeight: '500',
                                                color: colors.textPrimary,
                                                fontFamily: FONT_HEADING,
                                                margin: 0,
                                                paddingRight: isAdmin
                                                    ? '40px' : 0,
                                            }}>
                                                {highlightText(
                                                    type.name,
                                                    searchQuery
                                                )}
                                            </h3>
                                            <div style={{
                                                display: 'flex',
                                                alignItems: 'center',
                                                gap: '6px',
                                            }}>
                                                <span style={{
                                                    fontSize: '15px',
                                                    color: colors.textMuted,
                                                    fontFamily: FONT_BODY,
                                                    fontWeight: '500',
                                                }}>
                                                    Сложность:
                                                </span>
                                                {type.difficultyLevel && (
                                                    <Stars
                                                        count={
                                                            difficultyStars[
                                                                type.difficultyLevel
                                                                ] || 0
                                                        }
                                                    />
                                                )}
                                            </div>
                                            <p style={cardDescriptionStyle}>
                                                {type.description
                                                    ? highlightText(
                                                        type.description,
                                                        searchQuery
                                                    )
                                                    : '\u00A0'}
                                            </p>
                                            <span style={{
                                                fontSize: '14px',
                                                color: colors.primaryDark,
                                                fontFamily: FONT_BODY,
                                                fontWeight: '600',
                                                marginTop: 'auto',
                                            }}>
                                                Подробнее →
                                            </span>
                                        </div>
                                    </button>
                                    {isAdmin && (
                                        <div style={{
                                            position: 'absolute',
                                            top: SPACING.sm,
                                            right: SPACING.sm,
                                            display: 'flex',
                                            gap: SPACING.xs,
                                            zIndex: 2,
                                        }}>
                                            <button
                                                type="button"
                                                onClick={(e) =>
                                                    handleEdit(type, e)
                                                }
                                                aria-label="Редактировать"
                                                style={getAdminButtonStyle(
                                                    colors.primaryDark
                                                )}
                                                onMouseOver={adminBtnHoverIn}
                                                onFocus={adminBtnHoverIn}
                                                onMouseOut={adminBtnHoverOut}
                                                onBlur={adminBtnHoverOut}
                                            >
                                                <EditIcon />
                                            </button>
                                            <button
                                                type="button"
                                                onClick={(e) =>
                                                    handleDelete(type.id, e)
                                                }
                                                aria-label="Удалить"
                                                style={{
                                                    ...getAdminButtonStyle(
                                                        colors.error
                                                    ),
                                                    fontSize: '18px',
                                                    lineHeight: 1,
                                                }}
                                            >
                                                ×
                                            </button>
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            {selectedType && (
                <div style={modalOverlayStyle} onClick={handleCloseModal}>
                    <div style={{
                        ...modalContentStyle,
                        maxWidth: '560px',
                    }} onClick={(e) => e.stopPropagation()}>
                        <div style={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            marginBottom: SPACING.md,
                        }}>
                            <h2 style={{
                                fontSize: '24px',
                                fontWeight: '500',
                                color: colors.primaryDark,
                                fontFamily: FONT_HEADING,
                                margin: 0,
                            }}>
                                {selectedType.name}
                            </h2>
                            <button
                                type="button"
                                onClick={handleCloseModal}
                                style={{
                                    background: 'none',
                                    border: 'none',
                                    fontSize: '24px',
                                    cursor: 'pointer',
                                    color: colors.textMuted,
                                    minWidth: '44px',
                                    minHeight: '44px',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                }}
                                aria-label="Закрыть"
                            >
                                ✕
                            </button>
                        </div>

                        {selectedType.description && (
                            <div style={{
                                ...infoBlockStyle,
                                backgroundColor: '#FAFAF8',
                                borderColor: colors.glassBorder,
                            }}>
                                <p style={{
                                    fontSize: '15px',
                                    color: colors.textSecondary,
                                    lineHeight: 1.7,
                                    margin: 0,
                                }}>
                                    {selectedType.description}
                                </p>
                            </div>
                        )}

                        {selectedType.difficultyLevel && (
                            <div style={{
                                ...infoBlockStyle,
                                backgroundColor: colors.primaryLight,
                                borderColor: 'rgba(151,155,129,0.25)',
                            }}>
                                <strong>Уровень подготовленности:</strong>{' '}
                                {difficultyLabels[
                                    selectedType.difficultyLevel
                                    ] || selectedType.difficultyLevel}
                                <Stars
                                    count={
                                        difficultyStars[
                                            selectedType.difficultyLevel
                                            ] || 0
                                    }
                                />
                            </div>
                        )}

                        {selectedType.contraindications && (
                            <div style={{
                                ...infoBlockStyle,
                                backgroundColor: '#FFF5F5',
                                borderColor: 'rgba(200,123,123,0.25)',
                            }}>
                                <strong>Противопоказания:</strong>{' '}
                                {selectedType.contraindications}
                            </div>
                        )}

                        {selectedType.benefits?.length > 0 && (
                            <div style={{
                                ...infoBlockStyle,
                                backgroundColor: '#F5FAF5',
                                borderColor: 'rgba(127,163,122,0.25)',
                            }}>
                                <strong>Преимущества:</strong>
                                <ul style={{
                                    margin: '6px 0 0',
                                    paddingLeft: '20px',
                                }}>
                                    {selectedType.benefits.map((b, i) => (
                                        <li
                                            key={`${selectedType.id}-b-${i}`}
                                            style={{ marginBottom: '2px' }}
                                        >
                                            {b}
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        )}

                        <div style={{
                            ...infoBlockStyle,
                            backgroundColor: '#FAFAF8',
                            borderColor: colors.glassBorder,
                        }}>
                            <strong style={{
                                fontSize: '16px',
                                color: colors.primaryDark,
                                display: 'block',
                                marginBottom: SPACING.sm,
                            }}>
                                Тренеры направления
                            </strong>
                            <div style={{
                                display: 'flex',
                                flexWrap: 'wrap',
                                gap: SPACING.xs,
                            }}>
                                {typeTrainers.length > 0 ? (
                                    typeTrainers.map((tr) => (
                                        <button
                                            key={tr.id}
                                            type="button"
                                            onClick={() =>
                                                navigate('/trainers')
                                            }
                                            style={{
                                                padding: '8px 14px',
                                                backgroundColor: 'white',
                                                borderRadius: '20px',
                                                fontSize: '13px',
                                                fontFamily: FONT_BODY,
                                                color: colors.primaryDark,
                                                border:
                                                    `1px solid ${colors.glassBorder}`,
                                                cursor: 'pointer',
                                            }}
                                        >
                                            {tr.lastName} {tr.firstName}
                                        </button>
                                    ))
                                ) : (
                                    <span style={{
                                        color: colors.textMuted,
                                        fontSize: '13px',
                                        fontFamily: FONT_BODY,
                                    }}>
                                        Нет назначенных тренеров
                                    </span>
                                )}
                            </div>
                        </div>

                        {upcomingSessions.length > 0 && (
                            <div style={{
                                ...infoBlockStyle,
                                backgroundColor: '#FAFAF8',
                                borderColor: colors.glassBorder,
                                marginTop: SPACING.sm,
                            }}>
                                <strong style={{
                                    fontSize: '16px',
                                    color: colors.primaryDark,
                                    display: 'block',
                                    marginBottom: SPACING.sm,
                                }}>
                                    Ближайшие занятия
                                </strong>
                                <div style={{
                                    display: 'flex',
                                    flexDirection: 'column',
                                    gap: SPACING.xs,
                                }}>
                                    {upcomingSessions.slice(0, 5).map((s) => (
                                        <button
                                            key={s.id}
                                            type="button"
                                            onClick={() =>
                                                navigate('/schedule')
                                            }
                                            style={sessionButtonStyle}
                                        >
                                            <div>
                                                <div style={{
                                                    fontWeight: '600',
                                                }}>
                                                    {formatDate(s.sessionDate)},{' '}
                                                    {formatTime(s.startTime)}
                                                    –{formatTime(s.endTime)}
                                                </div>
                                                {s.room && (
                                                    <div style={{
                                                        color: colors.textSecondary,
                                                    }}>
                                                        Зал: {s.room}
                                                    </div>
                                                )}
                                            </div>
                                            <span style={{
                                                color: colors.primary,
                                                fontWeight: '600',
                                            }}>
                                                →
                                            </span>
                                        </button>
                                    ))}
                                </div>
                            </div>
                        )}

                        <button
                            type="button"
                            onClick={handleCloseModal}
                            style={{
                                width: '100%',
                                padding: '14px',
                                borderRadius: '14px',
                                border: 'none',
                                backgroundColor: colors.primaryDark,
                                color: '#FFF',
                                fontFamily: FONT_BODY,
                                fontSize: '15px',
                                fontWeight: '600',
                                cursor: 'pointer',
                                minHeight: '48px',
                                marginTop: SPACING.md,
                            }}
                        >
                            Закрыть
                        </button>
                    </div>
                </div>
            )}
        </>
    );
}

export default Directions;
