import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { subscriptionApi } from '../api/subscriptionApi';
import { workoutTypeApi } from '../api/workoutTypeApi';
import { useAuth } from '../contexts/useAuth';
import ReactDOM from 'react-dom';

const FONT_HEADING = "'Cormorant Garamond', 'Times New Roman', serif";
const FONT_BODY = "'Inter', 'Segoe UI', sans-serif";

const colors = {
    primary: '#979B81',
    primaryDark: '#4D5044',
    primaryHover: '#7E8A6A',
    primaryLight: '#E4E7D6',
    textPrimary: '#0F0F10',
    textSecondary: '#5F6256',
    textOnPrimary: '#FFFFFF',
    error: '#C87B7B',
    success: '#7FA37A',
    glassBorder: 'rgba(180,180,170,0.5)',
    backgroundCard: 'rgba(255,255,255,0.7)',
    shadowCard: '0 2px 16px rgba(77,80,68,0.06)',
    shadowHover: '0 6px 28px rgba(77,80,68,0.12)',
};

const SPACING = { xs: '6px', sm: '14px', md: '24px', lg: '36px' };

const headingStyle = {
    fontSize: 'clamp(28px, 5vw, 40px)',
    fontWeight: '300',
    color: colors.primaryDark,
    textAlign: 'center',
    fontFamily: FONT_HEADING,
    letterSpacing: '1px',
    margin: 0,
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

const errorTextStyle = {
    color: colors.error,
    fontSize: '12px',
    margin: '4px 0 0',
    fontFamily: FONT_BODY,
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

const emptyForm = {
    id: null,
    name: '',
    price: 0,
    subscriptionType: 'LIMITED',
    maxVisits: 1,
    durationDays: 30,
    status: 'ACTIVE',
    workoutTypeIds: [],
};

const PlusIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" strokeWidth="2" strokeLinecap="round"
         strokeLinejoin="round">
        <line x1="12" y1="5" x2="12" y2="19" />
        <line x1="5" y1="12" x2="19" y2="12" />
    </svg>
);

const EditIcon = () => (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" strokeWidth="1.8" strokeLinecap="round"
         strokeLinejoin="round">
        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
    </svg>
);

const DropdownArrow = ({ open }) => (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
         stroke={colors.primaryDark} strokeWidth="2" strokeLinecap="round"
         strokeLinejoin="round"
         style={{
             transform: open ? 'rotate(180deg)' : 'rotate(0)',
             transition: 'transform 0.2s ease',
             marginLeft: '12px',
             flexShrink: 0,
         }}>
        <polyline points="6 9 12 15 18 9" />
    </svg>
);

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
    maxWidth: '560px',
    width: '100%',
    maxHeight: '85vh',
    boxShadow: '0 20px 60px rgba(0,0,0,0.15)',
    fontFamily: FONT_BODY,
};

const chipStyle = {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '4px',
    padding: '6px 12px',
    backgroundColor: colors.primaryLight,
    borderRadius: '20px',
    fontSize: '13px',
    fontFamily: FONT_BODY,
    color: colors.primaryDark,
};

const chipRemoveStyle = {
    background: 'none',
    border: 'none',
    cursor: 'pointer',
    color: colors.error,
    fontSize: '16px',
    lineHeight: 1,
    padding: '0 2px',
};

const filterBtnStyle = (active) => ({
    width: '100%',
    padding: '10px 12px',
    fontSize: '16px',
    fontFamily: FONT_BODY,
    fontWeight: active ? '600' : '400',
    color: active ? colors.primaryDark : colors.textSecondary,
    backgroundColor: active ? colors.primaryLight : 'transparent',
    border: 'none',
    borderRadius: '8px',
    cursor: 'pointer',
    textAlign: 'left',
    transition: 'all 0.15s ease',
});

function Prices() {
    const [subscriptions, setSubscriptions] = useState([]);
    const [workoutTypes, setWorkoutTypes] = useState([]);
    const [form, setForm] = useState(emptyForm);
    const [editingId, setEditingId] = useState(null);
    const [isNew, setIsNew] = useState(true);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [fieldErrors, setFieldErrors] = useState({});
    const [selectedTypes, setSelectedTypes] = useState([]);
    const [typeFormOpen, setTypeFormOpen] = useState(false);
    const [subTypeOpen, setSubTypeOpen] = useState(false);
    const typeFormRef = useRef(null);
    const subTypeRef = useRef(null);
    const navigate = useNavigate();
    const { user } = useAuth();
    const isAdmin = user?.role === 'ADMIN';

    const fetchData = useCallback(async () => {
        try {
            const [s, t] = await Promise.all([
                subscriptionApi.getActive(),
                workoutTypeApi.getActive(),
            ]);
            setSubscriptions(s.data);
            setWorkoutTypes(t.data);
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
            if (typeFormRef.current
                && !typeFormRef.current.contains(e.target)
                && !e.target.closest('[style*="z-index: 200"]')) {
                setTypeFormOpen(false);
            }
            if (subTypeRef.current
                && !subTypeRef.current.contains(e.target)) {
                setSubTypeOpen(false);
            }
        };
        document.addEventListener('mousedown', handler);
        return () => document.removeEventListener('mousedown', handler);
    }, []);

    const handleEdit = (sub, e) => {
        e.stopPropagation();
        setForm({
            id: sub.id,
            name: sub.name,
            price: sub.price || 0,
            subscriptionType: sub.subscriptionType || 'LIMITED',
            maxVisits: sub.maxVisits || 1,
            durationDays: sub.durationDays || 30,
            status: sub.status || 'ACTIVE',
            workoutTypeIds: sub.workoutTypeIds || [],
        });
        setIsNew(false);
        setEditingId(sub.id);
        setFieldErrors({});
        setError('');
    };

    const handleNew = () => {
        setForm(emptyForm);
        setIsNew(true);
        setEditingId('new');
        setFieldErrors({});
        setError('');
    };

    const handleCancel = () => {
        setForm(emptyForm);
        setEditingId(null);
        setIsNew(true);
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

    const toggleWorkoutType = (id) => setForm((prev) => ({
        ...prev,
        workoutTypeIds: prev.workoutTypeIds.includes(id)
            ? prev.workoutTypeIds.filter((i) => i !== id)
            : [...prev.workoutTypeIds, id],
    }));

    const toggleTypeFilter = (id) => setSelectedTypes((prev) =>
        prev.includes(id)
            ? prev.filter((i) => i !== id)
            : [...prev, id]
    );

    const handleSave = async () => {
        const errors = {};
        if (!form.name.trim()) {
            errors.name = 'Название обязательно';
        }
        if (!form.price || form.price <= 0) {
            errors.price = 'Цена должна быть больше 0';
        }
        if (!form.durationDays || form.durationDays < 1) {
            errors.durationDays = 'Срок обязателен';
        }
        if (!form.workoutTypeIds || form.workoutTypeIds.length === 0) {
            errors.workoutTypeIds = 'Выберите хотя бы одно направление';
        }
        setFieldErrors(errors);
        if (Object.keys(errors).length > 0) return;

        setSaving(true);
        setError('');
        setSuccess('');
        try {
            const payload = { ...form, price: parseFloat(form.price) };
            if (isNew) {
                await subscriptionApi.create(payload);
            } else {
                await subscriptionApi.update(form.id, payload);
            }
            setSuccess(isNew ? 'Абонемент создан' : 'Абонемент обновлён');
            setEditingId(null);
            setForm(emptyForm);
            setIsNew(true);
            fetchData();
        } catch (err2) {
            const msg =
                err2.response?.data?.message || 'Не удалось сохранить';
            if (msg.includes('уже существует')) {
                setFieldErrors((prev) => ({ ...prev, name: msg }));
            } else {
                setError(msg);
            }
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id, e) => {
        e.stopPropagation();
        if (!id) return;
        if (!window.confirm('Удалить?')) return;
        try {
            await subscriptionApi.delete(id);
            fetchData();
            setSuccess('Абонемент удалён');
        } catch (err) {
            const msg =
                err.response?.data?.message || 'Не удалось удалить';
            setError(msg);
        }
    };

    const filteredSubs = selectedTypes.length > 0
        ? subscriptions.filter((s) =>
            selectedTypes.every((id) => s.workoutTypeIds?.includes(id))
        )
        : subscriptions;


    const [typeDropdownStyle, setTypeDropdownStyle] = useState({});

    const openTypeDropdown = () => {
        setTypeFormOpen(true);
        requestAnimationFrame(() => {
            if (typeFormRef.current) {
                const rect = typeFormRef.current.getBoundingClientRect();
                const spaceBelow = window.innerHeight - rect.bottom;
                const listHeight = 200;
                const top = spaceBelow >= listHeight
                    ? rect.bottom
                    : rect.top - listHeight;
                setTypeDropdownStyle({
                    position: 'fixed',
                    left: rect.left,
                    top: Math.max(0, top),
                    width: rect.width,
                    zIndex: 200,
                    backgroundColor: 'white',
                    border: '1px solid rgba(180,180,170,0.5)',
                    borderTop: 'none',
                    borderRadius: '0 0 10px 10px',
                    boxShadow: '0 12px 40px rgba(77,80,68,0.2)',
                    overflow: 'hidden',
                    maxHeight: `${listHeight}px`,
                    overflowY: 'auto',
                });
            }
        });
    };

    useEffect(() => {
        if (typeFormOpen) {
            const handler = () => openTypeDropdown();
            window.addEventListener('resize', handler);
            window.addEventListener('scroll', handler, true);
            return () => {
                window.removeEventListener('resize', handler);
                window.removeEventListener('scroll', handler, true);
            };
        }
    }, [typeFormOpen]);

    if (loading) {
        return (
            <div style={{
                minHeight: '60vh',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontFamily: FONT_BODY,
                color: colors.textSecondary,
            }}>
                Загрузка...
            </div>
        );
    }

    const messageStyle = {
        padding: '12px 16px',
        borderRadius: '10px',
        fontFamily: FONT_BODY,
        fontSize: '14px',
        marginBottom: SPACING.sm,
        textAlign: 'center',
        maxWidth: '800px',
        marginLeft: 'auto',
        marginRight: 'auto',
    };

    return (
        <div style={{
            padding: `${SPACING.sm} ${SPACING.sm} ${SPACING.lg}`,
        }}>
            <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: SPACING.xs,
                marginBottom: SPACING.md,
            }}>
                <h1 style={headingStyle}>Абонементы</h1>
                {isAdmin && editingId !== 'new' && (
                    <button
                        type="button"
                        onClick={handleNew}
                        aria-label="Добавить"
                        style={{
                            width: '40px',
                            height: '40px',
                            borderRadius: '10px',
                            color: colors.primaryDark,
                            backgroundColor: 'rgba(255,255,255,0.8)',
                            border: `1px solid ${colors.glassBorder}`,
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
                            e.currentTarget.style.color = colors.primaryDark;
                        }}
                        onBlur={(e) => {
                            e.currentTarget.style.backgroundColor =
                                'rgba(255,255,255,0.8)';
                            e.currentTarget.style.color = colors.primaryDark;
                        }}
                    >
                        <PlusIcon />
                    </button>
                )}
            </div>

            {error && (
                <div style={{
                    ...messageStyle,
                    backgroundColor: '#FFF0F0',
                    color: colors.error,
                }}>
                    {error}
                </div>
            )}
            {success && (
                <div style={{
                    ...messageStyle,
                    backgroundColor: '#F0F7F0',
                    color: colors.success,
                }}>
                    {success}
                </div>
            )}

            {(editingId === 'new' || editingId !== null) && (
                <div style={modalOverlayStyle} onClick={handleCancel}>
                    <div
                        style={modalContentStyle}
                        onClick={(e) => e.stopPropagation()}
                    >
                        <h3 style={{
                            fontFamily: FONT_HEADING,
                            color: colors.primaryDark,
                            fontSize: '24px',
                            textAlign: 'center',
                            marginTop: 0,
                            marginBottom: SPACING.md,
                        }}>
                            {isNew
                                ? 'Новый абонемент'
                                : 'Редактирование'}
                        </h3>
                        <div style={{ display: 'grid', gap: SPACING.sm }}>
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
                                    value={form.name}
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
                                        Цена (BYN)
                                    </label>
                                    <input
                                        name="price"
                                        type="number"
                                        step="0.01"
                                        min="0"
                                        value={form.price}
                                        onChange={handleChange}
                                        style={
                                            fieldErrors.price
                                                ? inputErrorStyle
                                                : inputStyle
                                        }
                                    />
                                    {fieldErrors.price && (
                                        <p style={errorTextStyle}>
                                            {fieldErrors.price}
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
                                        Срок (дней)
                                    </label>
                                    <input
                                        name="durationDays"
                                        type="number"
                                        min="1"
                                        value={form.durationDays}
                                        onChange={handleChange}
                                        style={
                                            fieldErrors.durationDays
                                                ? inputErrorStyle
                                                : inputStyle
                                        }
                                    />
                                    {fieldErrors.durationDays && (
                                        <p style={errorTextStyle}>
                                            {fieldErrors.durationDays}
                                        </p>
                                    )}
                                </div>
                            </div>

                            <div style={{
                                display: 'grid',
                                gridTemplateColumns: '1fr 1fr',
                                gap: SPACING.sm,
                            }}>
                                <div
                                    style={{ position: 'relative' }}
                                    ref={subTypeRef}
                                >
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
                                            setSubTypeOpen(!subTypeOpen)
                                        }
                                        style={dropdownBtnStyle(subTypeOpen)}
                                    >
                                        {form.subscriptionType === 'LIMITED'
                                            ? 'Лимитированный'
                                            : 'Безлимитный'}
                                        <DropdownArrow open={subTypeOpen} />
                                    </button>
                                    {subTypeOpen && (
                                        <div style={{
                                            position: 'absolute',
                                            top: '100%',
                                            left: 0,
                                            right: 0,
                                            zIndex: 20,
                                            backgroundColor: 'white',
                                            border:
                                                '1px solid rgba(180,180,170,0.5)',
                                            borderTop: 'none',
                                            borderRadius: '0 0 10px 10px',
                                            boxShadow:
                                                '0 8px 24px rgba(77,80,68,0.12)',
                                            overflow: 'hidden',
                                        }}>
                                            {['LIMITED', 'UNLIMITED'].map(
                                                (v) => (
                                                    <button
                                                        key={v}
                                                        type="button"
                                                        onClick={() => {
                                                            setForm((prev) => ({
                                                                ...prev,
                                                                subscriptionType: v,
                                                            }));
                                                            setSubTypeOpen(
                                                                false
                                                            );
                                                        }}
                                                        style={dropdownOptStyle(
                                                            form.subscriptionType === v
                                                        )}
                                                        onMouseOver={(e) =>
                                                            e.currentTarget
                                                                .style.backgroundColor =
                                                                colors.primaryLight
                                                        }
                                                        onMouseOut={(e) => {
                                                            e.currentTarget.style.backgroundColor =
                                                                form.subscriptionType === v
                                                                    ? colors.primaryLight
                                                                    : 'white';
                                                        }}
                                                    >
                                                        {v === 'LIMITED'
                                                            ? 'Лимитированный'
                                                            : 'Безлимитный'}
                                                    </button>
                                                )
                                            )}
                                        </div>
                                    )}
                                </div>
                                {form.subscriptionType === 'LIMITED' && (
                                    <div>
                                        <label style={{
                                            display: 'block',
                                            marginBottom: '4px',
                                            fontSize: '13px',
                                            fontWeight: '600',
                                            fontFamily: FONT_BODY,
                                            color: colors.textSecondary,
                                        }}>
                                            Макс. занятий
                                        </label>
                                        <input
                                            name="maxVisits"
                                            type="number"
                                            min="1"
                                            value={form.maxVisits}
                                            onChange={handleChange}
                                            style={inputStyle}
                                        />
                                    </div>
                                )}
                            </div>

                            <div style={{ position: 'relative' }} ref={typeFormRef}>
                                <label style={{
                                    display: 'block',
                                    marginBottom: '4px',
                                    fontSize: '13px',
                                    fontWeight: '600',
                                    fontFamily: FONT_BODY,
                                    color: colors.textSecondary,
                                }}>
                                    Направления
                                </label>
                                <button
                                    type="button"
                                    onClick={openTypeDropdown}
                                    style={dropdownBtnStyle(typeFormOpen)}
                                >
                                    Выбрать направления
                                    <DropdownArrow open={typeFormOpen} />
                                </button>
                                {typeFormOpen && ReactDOM.createPortal(
                                    <div style={typeDropdownStyle}>
                                        {workoutTypes.map((wt) => (
                                            <button
                                                key={wt.id}
                                                type="button"
                                                onMouseDown={(e) => {
                                                    e.preventDefault();
                                                    e.stopPropagation();
                                                    toggleWorkoutType(wt.id);
                                                }}
                                                style={{
                                                    ...dropdownOptStyle(
                                                        form.workoutTypeIds.includes(wt.id)
                                                    ),
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    gap: '8px',
                                                    width: '100%',
                                                }}
                                                >
                                                <span style={{ fontSize: '18px' }}>
                                                    {form.workoutTypeIds.includes(wt.id)
                                                        ? '✓'
                                                        : '○'}
                                                </span>
                                                {wt.name}
                                            </button>
                                        ))}
                                    </div>,
                                    document.body
                                )}
                                {form.workoutTypeIds.length > 0 && !typeFormOpen && (
                                    <div style={{
                                        display: 'flex',
                                        flexWrap: 'wrap',
                                        gap: SPACING.xs,
                                        marginTop: SPACING.xs,
                                    }}>
                                        {form.workoutTypeIds.map((id) => {
                                            const wt = workoutTypes.find((w) => w.id === id);
                                            if (!wt) return null;
                                            return (
                                                <span key={id} style={chipStyle}>
                                                    {wt.name}
                                                    <button
                                                        type="button"
                                                        onClick={() => toggleWorkoutType(id)}
                                                        style={chipRemoveStyle}
                                                    >
                                                        ×
                                                    </button>
                                                </span>
                                            );
                                        })}
                                    </div>
                                )}
                            </div>
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
                                    transition: 'all 0.2s ease',
                                }}
                                onMouseOver={(e) => {
                                    e.currentTarget.style.backgroundColor =
                                        colors.error;
                                    e.currentTarget.style.color = '#FFF';
                                }}
                                onFocus={(e) => {
                                    e.currentTarget.style.backgroundColor =
                                        colors.error;
                                    e.currentTarget.style.color = '#FFF';
                                }}
                                onMouseOut={(e) => {
                                    e.currentTarget.style.backgroundColor =
                                        'transparent';
                                    e.currentTarget.style.color = colors.error;
                                }}
                                onBlur={(e) => {
                                    e.currentTarget.style.backgroundColor =
                                        'transparent';
                                    e.currentTarget.style.color = colors.error;
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
                                    transition: 'all 0.2s ease',
                                }}
                                onMouseOver={(e) => {
                                    if (!saving) {
                                        e.currentTarget.style.backgroundColor =
                                            colors.primaryHover;
                                    }
                                }}
                                onFocus={(e) => {
                                    if (!saving) {
                                        e.currentTarget.style.backgroundColor =
                                            colors.primaryHover;
                                    }
                                }}
                                onMouseOut={(e) => {
                                    if (!saving) {
                                        e.currentTarget.style.backgroundColor =
                                            colors.primaryDark;
                                    }
                                }}
                                onBlur={(e) => {
                                    if (!saving) {
                                        e.currentTarget.style.backgroundColor =
                                            colors.primaryDark;
                                    }
                                }}
                            >
                                {saving
                                    ? 'Сохранение...'
                                    : isNew
                                        ? 'Создать'
                                        : 'Сохранить'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {typeFormOpen && (
                <div
                    style={{
                        position: 'fixed',
                        inset: 0,
                        zIndex: 101,
                    }}
                    onClick={() => setTypeFormOpen(false)}
                />
            )}



            <div style={{
                display: 'flex',
                gap: SPACING.md,
                maxWidth: '1200px',
                margin: '0 auto',
                padding: `0 ${SPACING.sm}`,
                alignItems: 'flex-start',
            }}>
                <div style={{
                    width: '240px',
                    flexShrink: 0,
                    backgroundColor: colors.backgroundCard,
                    borderRadius: '16px',
                    border: `1px solid ${colors.glassBorder}`,
                    padding: SPACING.sm,
                    position: 'sticky',
                    top: '80px',
                }}>
                    <div style={{
                        fontSize: '17px',
                        fontWeight: '600',
                        fontFamily: FONT_BODY,
                        color: colors.primaryDark,
                        marginBottom: SPACING.sm,
                        textAlign: 'center',
                        paddingBottom: SPACING.sm,
                        borderBottom: `1px solid ${colors.glassBorder}`,
                    }}>
                        Направления
                    </div>
                    <div style={{
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '2px',
                        maxHeight: '500px',
                        overflowY: 'auto',
                    }}>
                        {workoutTypes.map((wt) => (
                            <button
                                key={wt.id}
                                type="button"
                                onClick={() => toggleTypeFilter(wt.id)}
                                style={filterBtnStyle(
                                    selectedTypes.includes(wt.id)
                                )}
                                onMouseOver={(e) => {
                                    if (!selectedTypes.includes(wt.id)) {
                                        e.currentTarget.style.backgroundColor =
                                            '#F5F5F0';
                                    }
                                }}
                                onMouseOut={(e) => {
                                    if (!selectedTypes.includes(wt.id)) {
                                        e.currentTarget.style.backgroundColor =
                                            'transparent';
                                    }
                                }}
                            >
                                {wt.name}
                            </button>
                        ))}
                    </div>
                    {selectedTypes.length > 0 && (
                        <button
                            type="button"
                            onClick={() => setSelectedTypes([])}
                            style={{
                                marginTop: SPACING.sm,
                                width: '100%',
                                padding: '8px',
                                borderRadius: '8px',
                                border: 'none',
                                backgroundColor: 'transparent',
                                color: colors.error,
                                fontFamily: FONT_BODY,
                                fontSize: '14px',
                                cursor: 'pointer',
                            }}
                        >
                            Сбросить
                        </button>
                    )}
                </div>

                <div style={{
                    flex: 1,
                    display: 'grid',
                    gridTemplateColumns:
                        'repeat(auto-fill, minmax(300px, 1fr))',
                    gap: SPACING.md,
                }}>
                    {filteredSubs.map((sub) => (
                        <div
                            key={sub.id}
                            style={{
                                backgroundColor: colors.backgroundCard,
                                backdropFilter: 'blur(16px)',
                                borderRadius: '18px',
                                border:
                                    `1px solid ${colors.glassBorder}`,
                                boxShadow: colors.shadowCard,
                                padding: SPACING.md,
                                display: 'flex',
                                flexDirection: 'column',
                                transition:
                                    'box-shadow 0.3s ease, transform 0.2s ease',
                            }}
                            onMouseOver={(e) => {
                                e.currentTarget.style.boxShadow =
                                    colors.shadowHover;
                                e.currentTarget.style.transform =
                                    'translateY(-2px)';
                            }}
                            onFocus={(e) => {
                                e.currentTarget.style.boxShadow =
                                    colors.shadowHover;
                                e.currentTarget.style.transform =
                                    'translateY(-2px)';
                            }}
                            onMouseOut={(e) => {
                                e.currentTarget.style.boxShadow =
                                    colors.shadowCard;
                                e.currentTarget.style.transform =
                                    'translateY(0)';
                            }}
                            onBlur={(e) => {
                                e.currentTarget.style.boxShadow =
                                    colors.shadowCard;
                                e.currentTarget.style.transform =
                                    'translateY(0)';
                            }}
                        >
                            <h3 style={{
                                fontSize: '22px',
                                fontWeight: '600',
                                color: colors.textPrimary,
                                fontFamily: FONT_HEADING,
                                margin: `0 0 ${SPACING.xs}`,
                            }}>
                                {sub.name}
                            </h3>
                            <div style={{
                                fontSize: '34px',
                                fontWeight: '300',
                                color: colors.primaryDark,
                                fontFamily: FONT_BODY,
                                marginBottom: SPACING.xs,
                            }}>
                                {parseFloat(sub.price).toFixed(2)} BYN
                            </div>
                            <div style={{
                                fontSize: '15px',
                                color: colors.textSecondary,
                                fontFamily: FONT_BODY,
                                marginBottom: SPACING.sm,
                            }}>
                                {sub.subscriptionType === 'UNLIMITED'
                                    ? `Безлимитный / ${sub.durationDays} дн.`
                                    : `${sub.maxVisits} занятий / ${sub.durationDays} дн.`
                                }
                            </div>
                            <div style={{
                                borderTop:
                                    `1px solid ${colors.glassBorder}`,
                                paddingTop: SPACING.sm,
                            }}>
                                <div style={{
                                    fontSize: '13px',
                                    color: colors.textSecondary,
                                    fontFamily: FONT_BODY,
                                    marginBottom: SPACING.xs,
                                    textTransform: 'uppercase',
                                    letterSpacing: '0.5px',
                                }}>
                                    Направления
                                </div>
                                <div style={{
                                    display: 'flex',
                                    flexWrap: 'wrap',
                                    gap: SPACING.xs,
                                }}>
                                    {sub.workoutTypeIds?.length > 0
                                        ? workoutTypes
                                            .filter((wt) =>
                                                sub.workoutTypeIds.includes(
                                                    wt.id
                                                )
                                            )
                                            .map((wt) => (
                                                <button
                                                    key={wt.id}
                                                    type="button"
                                                    onClick={() =>
                                                        navigate(
                                                            `/directions?open=${wt.id}`
                                                        )
                                                    }
                                                    style={{
                                                        padding: '6px 12px',
                                                        borderRadius: '20px',
                                                        border:
                                                            `1px solid ${colors.glassBorder}`,
                                                        backgroundColor:
                                                            'white',
                                                        color: colors.primaryDark,
                                                        fontFamily: FONT_BODY,
                                                        fontSize: '12px',
                                                        cursor: 'pointer',
                                                        transition:
                                                            'all 0.2s ease',
                                                    }}
                                                    onMouseOver={(e) => {
                                                        e.currentTarget.style.backgroundColor =
                                                            colors.primaryLight;
                                                    }}
                                                    onMouseOut={(e) => {
                                                        e.currentTarget.style.backgroundColor =
                                                            'white';
                                                    }}
                                                >
                                                    {wt.name}
                                                </button>
                                            ))
                                        : (
                                            <span style={{
                                                color: colors.textSecondary,
                                                fontSize: '13px',
                                                fontFamily: FONT_BODY,
                                            }}>
                                                —
                                            </span>
                                        )}
                                </div>
                            </div>
                            <div style={{ flex: 1 }} />
                            {isAdmin && (
                                <div style={{
                                    display: 'flex',
                                    gap: SPACING.xs,
                                    marginTop: SPACING.sm,
                                    paddingTop: SPACING.sm,
                                    borderTop:
                                        `1px solid ${colors.glassBorder}`,
                                }}>
                                    <button
                                        type="button"
                                        onClick={(e) =>
                                            handleEdit(sub, e)
                                        }
                                        style={{
                                            flex: 1,
                                            padding: '10px',
                                            borderRadius: '8px',
                                            border: 'none',
                                            cursor: 'pointer',
                                            fontFamily: FONT_BODY,
                                            fontSize: '13px',
                                            fontWeight: '600',
                                            backgroundColor:
                                            colors.primaryLight,
                                            color: colors.primaryDark,
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                            gap: '4px',
                                            transition: 'all 0.2s ease',
                                        }}
                                        onMouseOver={(e) => {
                                            e.currentTarget.style
                                                .backgroundColor =
                                                colors.primaryDark;
                                            e.currentTarget.style.color =
                                                '#FFF';
                                        }}
                                        onFocus={(e) => {
                                            e.currentTarget.style
                                                .backgroundColor =
                                                colors.primaryDark;
                                            e.currentTarget.style.color =
                                                '#FFF';
                                        }}
                                        onMouseOut={(e) => {
                                            e.currentTarget.style
                                                .backgroundColor =
                                                colors.primaryLight;
                                            e.currentTarget.style.color =
                                                colors.primaryDark;
                                        }}
                                        onBlur={(e) => {
                                            e.currentTarget.style
                                                .backgroundColor =
                                                colors.primaryLight;
                                            e.currentTarget.style.color =
                                                colors.primaryDark;
                                        }}
                                    >
                                        <EditIcon /> Изменить
                                    </button>
                                    <button
                                        type="button"
                                        onClick={(e) =>
                                            handleDelete(sub.id, e)
                                        }
                                        style={{
                                            flex: 1,
                                            padding: '10px',
                                            borderRadius: '8px',
                                            border:
                                                `1px solid ${colors.error}`,
                                            cursor: 'pointer',
                                            fontFamily: FONT_BODY,
                                            fontSize: '13px',
                                            fontWeight: '600',
                                            backgroundColor: 'transparent',
                                            color: colors.error,
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
                                        Удалить
                                    </button>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

export default Prices;