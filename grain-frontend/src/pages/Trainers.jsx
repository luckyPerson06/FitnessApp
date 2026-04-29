import { useState, useEffect, useCallback, useRef } from 'react';
import { trainerApi } from '../api/trainerApi';
import { useAuth } from '../contexts/useAuth';

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
    warning: '#D9A86C',
    shadowCard: '0 4px 20px rgba(77, 80, 68, 0.06)',
    shadowHover: '0 8px 30px rgba(77, 80, 68, 0.12)',
    glassBorder: 'rgba(180,180,170,0.4)',
    backgroundCard: 'rgba(255,255,255,0.65)',
    vacationBg: '#FFF3E0',
    firedBg: '#FFF0F0',
    probationBg: '#F0F7F0',
};

const SPACING = {
    xs: '6px',
    sm: '14px',
    md: '24px',
    lg: '36px',
    xl: '48px',
};

const containerStyle = {
    width: '100%',
    maxWidth: '900px',
    margin: '0 auto',
    padding: `0 ${SPACING.sm}`,
    boxSizing: 'border-box',
};

const headingStyle = {
    fontSize: 'clamp(28px, 5vw, 38px)',
    fontWeight: '400',
    color: colors.primaryDark,
    fontFamily: FONT_HEADING,
    letterSpacing: '0.5px',
    lineHeight: 1.2,
    margin: 0,
};

const trainerStatusLabels = {
    ACTIVE: 'Активен',
    VACATION: 'В отпуске',
    PROBATION: 'На испытательном сроке',
};

const statusOptions = Object.entries(trainerStatusLabels).map(([val, label]) => ({ value: val, label }));

const statusStyle = (status) => {
    const map = {
        VACATION: { bg: colors.vacationBg, color: colors.warning },
        FIRED: { bg: colors.firedBg, color: colors.error },
        PROBATION: { bg: colors.probationBg, color: colors.success },
    };
    return map[status] || { bg: 'transparent', color: colors.textMuted };
};

const emptyForm = {
    id: null,
    firstName: '',
    lastName: '',
    photoPath: '',
    description: '',
    status: 'ACTIVE',
};

const inputStyle = {
    width: '100%',
    padding: '14px 16px',
    fontSize: '15px',
    fontFamily: FONT_BODY,
    border: '1px solid rgba(180,180,170,0.5)',
    borderRadius: '10px',
    outline: 'none',
    backgroundColor: '#FAFAF8',
    color: colors.textPrimary,
    boxSizing: 'border-box',
};

const textareaStyle = {
    ...inputStyle,
    minHeight: '100px',
    resize: 'vertical',
};

const EditIcon = () => (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
    </svg>
);

const PlusIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <line x1="12" y1="5" x2="12" y2="19" />
        <line x1="5" y1="12" x2="19" y2="12" />
    </svg>
);

function Trainers() {
    const [trainers, setTrainers] = useState([]);
    const [form, setForm] = useState(emptyForm);
    const [editingId, setEditingId] = useState(null);
    const [isNew, setIsNew] = useState(true);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [statusOpen, setStatusOpen] = useState(false);
    const statusRef = useRef(null);
    const fileInputRef = useRef(null);
    const { user } = useAuth();
    const isAdmin = user?.role === 'ADMIN';

    const fetchTrainers = useCallback(async () => {
        try {
            const response = await trainerApi.getAll();
            setTrainers(response.data);
        } catch {
            setError('Не удалось загрузить тренеров');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        let cancelled = false;
        if (!cancelled) fetchTrainers();
        return () => { cancelled = true; };
    }, [fetchTrainers]);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (statusRef.current && !statusRef.current.contains(event.target)) {
                setStatusOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleEdit = (trainer, e) => {
        e.stopPropagation();
        setForm({
            id: trainer.id,
            firstName: trainer.firstName || '',
            lastName: trainer.lastName || '',
            photoPath: trainer.photoPath || '',
            description: trainer.description || '',
            status: trainer.status || 'ACTIVE',
        });
        setIsNew(false);
        setEditingId(trainer.id);
        setError('');
        setSuccess('');
        setStatusOpen(false);
    };

    const handleNew = () => {
        setForm(emptyForm);
        setIsNew(true);
        setEditingId('new');
        setError('');
        setSuccess('');
        setStatusOpen(false);
    };

    const handleCancel = () => {
        setForm(emptyForm);
        setEditingId(null);
        setIsNew(true);
        setError('');
        setStatusOpen(false);
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = () => setForm((prev) => ({ ...prev, photoPath: reader.result }));
        reader.readAsDataURL(file);
    };

    const handleStatusSelect = (value) => {
        setForm((prev) => ({ ...prev, status: value }));
        setStatusOpen(false);
    };

    const handleSave = async () => {
        if (!form.firstName.trim() || !form.lastName.trim()) {
            setError('Имя и фамилия обязательны');
            return;
        }
        setSaving(true);
        setError('');
        setSuccess('');
        try {
            const payload = {
                firstName: form.firstName,
                lastName: form.lastName,
                photoPath: form.photoPath || null,
                description: form.description,
                status: form.status,
            };
            if (isNew) {
                await trainerApi.create(payload);
                setSuccess('Тренер создан');
            } else {
                await trainerApi.update(form.id, payload);
                setSuccess('Тренер обновлён');
            }
            setForm(emptyForm);
            setEditingId(null);
            setIsNew(true);
            fetchTrainers();
        } catch {
            setError('Не удалось сохранить тренера');
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (trainer, e) => {
        e.stopPropagation();
        if (!trainer.id) return;
        if (!window.confirm('Удалить тренера?')) return;
        try {
            await trainerApi.delete(trainer.id);
            fetchTrainers();
            setSuccess('Тренер удалён');
        } catch {
            setError('Не удалось удалить тренера');
        }
    };

    if (loading) {
        return (
            <div style={{ minHeight: '60vh', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: FONT_BODY, color: colors.textMuted, fontSize: '16px' }}>
                Загрузка...
            </div>
        );
    }

    return (
        <div style={{ padding: `${SPACING.sm} ${SPACING.sm} 0` }}>
            <div style={containerStyle}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: SPACING.md, flexWrap: 'wrap', gap: SPACING.sm }}>
                    <h1 style={headingStyle}>Наши тренеры</h1>
                    {isAdmin && editingId !== 'new' && (
                        <button type="button" onClick={handleNew} style={{
                            padding: '14px 24px', borderRadius: '14px', border: 'none',
                            backgroundColor: colors.primaryDark, color: colors.textOnPrimary,
                            fontFamily: FONT_BODY, fontSize: '16px', fontWeight: '600',
                            cursor: 'pointer', minHeight: '52px', transition: 'all 0.2s ease',
                            display: 'flex', alignItems: 'center', gap: '8px',
                        }}
                                onMouseOver={(e) => e.currentTarget.style.backgroundColor = colors.primaryHover}
                                onFocus={(e) => e.currentTarget.style.backgroundColor = colors.primaryHover}
                                onMouseOut={(e) => e.currentTarget.style.backgroundColor = colors.primaryDark}
                                onBlur={(e) => e.currentTarget.style.backgroundColor = colors.primaryDark}>
                            <PlusIcon /> Добавить тренера
                        </button>
                    )}
                </div>

                {error && (
                    <div style={{ padding: '14px 18px', backgroundColor: '#FFF0F0', borderRadius: '12px', color: colors.error, fontFamily: FONT_BODY, fontSize: '15px', marginBottom: SPACING.sm }}>
                        {error}
                    </div>
                )}
                {success && (
                    <div style={{ padding: '14px 18px', backgroundColor: '#F0F7F0', borderRadius: '12px', color: colors.success, fontFamily: FONT_BODY, fontSize: '15px', marginBottom: SPACING.sm }}>
                        {success}
                    </div>
                )}

                {editingId === 'new' && (
                    <div style={{
                        backgroundColor: colors.backgroundCard, backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)',
                        borderRadius: '20px', border: `1px solid ${colors.glassBorder}`,
                        padding: SPACING.lg, marginBottom: SPACING.md,
                        maxWidth: '600px', marginLeft: 'auto', marginRight: 'auto',
                    }}>
                        <h3 style={{ fontFamily: FONT_HEADING, color: colors.primaryDark, marginBottom: SPACING.md, fontSize: '24px', textAlign: 'center', marginTop: 0 }}>
                            Новый тренер
                        </h3>
                        <TrainerForm form={form} handleChange={handleChange} handleFileChange={handleFileChange}
                                     fileInputRef={fileInputRef} statusOpen={statusOpen} setStatusOpen={setStatusOpen}
                                     statusRef={statusRef} statusOptions={statusOptions} handleStatusSelect={handleStatusSelect} />
                        <div style={{ display: 'flex', gap: SPACING.sm, justifyContent: 'flex-end', marginTop: SPACING.md }}>
                            <ActionButton label="Отмена" onClick={handleCancel} isDanger />
                            <ActionButton label={saving ? 'Сохранение...' : 'Создать'} onClick={handleSave} disabled={saving} />
                        </div>
                    </div>
                )}

                {trainers.length === 0 && !loading && editingId !== 'new' && (
                    <p style={{ textAlign: 'center', color: colors.textMuted, fontFamily: FONT_BODY, fontSize: '18px', padding: SPACING.xl }}>
                        Тренеры пока не добавлены
                    </p>
                )}

                <div style={{ display: 'flex', flexDirection: 'column', gap: SPACING.md, paddingBottom: SPACING.xl }}>
                    {trainers.map((trainer) => (
                        <div key={trainer.id}>
                            {editingId === trainer.id ? (
                                <div style={{
                                    backgroundColor: colors.backgroundCard, backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)',
                                    borderRadius: '20px', border: `1px solid ${colors.glassBorder}`,
                                    padding: SPACING.lg, position: 'relative', zIndex: 1,
                                }}>
                                    <h3 style={{ fontFamily: FONT_HEADING, color: colors.primaryDark, marginBottom: SPACING.md, fontSize: '22px', textAlign: 'center', marginTop: 0 }}>
                                        Редактирование тренера
                                    </h3>
                                    <TrainerForm form={form} handleChange={handleChange} handleFileChange={handleFileChange}
                                                 fileInputRef={fileInputRef} statusOpen={statusOpen} setStatusOpen={setStatusOpen}
                                                 statusRef={statusRef} statusOptions={statusOptions} handleStatusSelect={handleStatusSelect} />
                                    <div style={{ display: 'flex', gap: SPACING.sm, justifyContent: 'flex-end', marginTop: SPACING.md }}>
                                        <ActionButton label="Отмена" onClick={handleCancel} isDanger />
                                        <ActionButton label={saving ? 'Сохранение...' : 'Сохранить'} onClick={handleSave} disabled={saving} />
                                    </div>
                                </div>
                            ) : (
                                <div style={{
                                    backgroundColor: colors.backgroundCard, backdropFilter: 'blur(16px)', WebkitBackdropFilter: 'blur(16px)',
                                    borderRadius: '20px', border: `1px solid ${colors.glassBorder}`,
                                    boxShadow: colors.shadowCard, padding: SPACING.md,
                                    display: 'flex', gap: SPACING.md, transition: 'box-shadow 0.3s ease',
                                    alignItems: 'flex-start', flexWrap: 'wrap',
                                }}>
                                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: SPACING.md, flex: 1, minWidth: '250px', flexWrap: 'wrap' }}>
                                        {trainer.photoPath ? (
                                            <img src={trainer.photoPath} alt="" style={{
                                                width: '110px', height: '110px', borderRadius: '50%',
                                                objectFit: 'cover', border: `2px solid ${colors.glassBorder}`, flexShrink: 0,
                                            }} />
                                        ) : (
                                            <div style={{
                                                width: '110px', height: '110px', borderRadius: '50%',
                                                backgroundColor: colors.primaryLight, flexShrink: 0,
                                                display: 'flex', alignItems: 'center', justifyContent: 'center',
                                                fontSize: '32px', color: colors.primary, fontFamily: FONT_HEADING,
                                            }}>
                                                {trainer.firstName?.charAt(0)}{trainer.lastName?.charAt(0)}
                                            </div>
                                        )}
                                        <div style={{ flex: 1, minWidth: '200px' }}>
                                            <h3 style={{
                                                fontSize: '22px', fontWeight: '500', color: colors.textPrimary,
                                                fontFamily: FONT_HEADING, margin: `0 0 ${SPACING.xs}`,
                                            }}>
                                                {trainer.lastName} {trainer.firstName}
                                            </h3>
                                            {trainer.status && trainer.status !== 'ACTIVE' && (
                                                <div style={{
                                                    marginBottom: SPACING.xs, padding: '6px 14px', borderRadius: '20px',
                                                    fontSize: '13px', fontFamily: FONT_BODY, fontWeight: '600',
                                                    display: 'inline-block',
                                                    backgroundColor: statusStyle(trainer.status).bg,
                                                    color: statusStyle(trainer.status).color,
                                                }}>
                                                    {trainerStatusLabels[trainer.status]}
                                                </div>
                                            )}
                                            {trainer.description && (
                                                <p style={{
                                                    fontSize: '16px', color: colors.textSecondary, fontFamily: FONT_BODY,
                                                    lineHeight: 1.7, margin: `${SPACING.xs} 0 0`, whiteSpace: 'pre-line',
                                                }}>
                                                    {trainer.description}
                                                </p>
                                            )}
                                        </div>
                                    </div>
                                    {isAdmin && (
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: SPACING.xs, flexShrink: 0, alignSelf: 'flex-start' }}>
                                            <button type="button" onClick={(e) => handleEdit(trainer, e)}
                                                    style={{
                                                        padding: '12px 20px', borderRadius: '10px', border: 'none', cursor: 'pointer',
                                                        fontFamily: FONT_BODY, fontSize: '15px', fontWeight: '600',
                                                        backgroundColor: colors.primaryLight, color: colors.primaryDark,
                                                        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px', minHeight: '48px', transition: 'all 0.2s ease', width: '100%',
                                                    }}
                                                    onMouseOver={(e) => { e.currentTarget.style.backgroundColor = colors.primary; e.currentTarget.style.color = colors.textOnPrimary; }}
                                                    onFocus={(e) => { e.currentTarget.style.backgroundColor = colors.primary; e.currentTarget.style.color = colors.textOnPrimary; }}
                                                    onMouseOut={(e) => { e.currentTarget.style.backgroundColor = colors.primaryLight; e.currentTarget.style.color = colors.primaryDark; }}
                                                    onBlur={(e) => { e.currentTarget.style.backgroundColor = colors.primaryLight; e.currentTarget.style.color = colors.primaryDark; }}>
                                                <EditIcon /> Изменить
                                            </button>
                                            <button type="button" onClick={(e) => handleDelete(trainer, e)}
                                                    style={{
                                                        padding: '12px 20px', borderRadius: '10px', border: `1px solid ${colors.error}`, cursor: 'pointer',
                                                        fontFamily: FONT_BODY, fontSize: '15px', fontWeight: '600',
                                                        backgroundColor: 'transparent', color: colors.error, minHeight: '48px', transition: 'all 0.2s ease', width: '100%',
                                                    }}
                                                    onMouseOver={(e) => { e.currentTarget.style.backgroundColor = colors.error; e.currentTarget.style.color = colors.textOnPrimary; }}
                                                    onFocus={(e) => { e.currentTarget.style.backgroundColor = colors.error; e.currentTarget.style.color = colors.textOnPrimary; }}
                                                    onMouseOut={(e) => { e.currentTarget.style.backgroundColor = 'transparent'; e.currentTarget.style.color = colors.error; }}
                                                    onBlur={(e) => { e.currentTarget.style.backgroundColor = 'transparent'; e.currentTarget.style.color = colors.error; }}>
                                                Удалить
                                            </button>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

const TrainerForm = ({ form, handleChange, handleFileChange, fileInputRef, statusOpen, setStatusOpen, statusRef, statusOptions, handleStatusSelect }) => (
    <div style={{ display: 'grid', gap: SPACING.md }}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: SPACING.md }}>
            <div>
                <label style={{ display: 'block', marginBottom: '6px', fontSize: '14px', fontWeight: '600', fontFamily: FONT_BODY, color: colors.textSecondary }}>Фамилия</label>
                <input name="lastName" value={form.lastName} onChange={handleChange} style={inputStyle} placeholder="Смирнова" />
            </div>
            <div>
                <label style={{ display: 'block', marginBottom: '6px', fontSize: '14px', fontWeight: '600', fontFamily: FONT_BODY, color: colors.textSecondary }}>Имя</label>
                <input name="firstName" value={form.firstName} onChange={handleChange} style={inputStyle} placeholder="Анна" />
            </div>
        </div>
        <div>
            <label style={{ display: 'block', marginBottom: '6px', fontSize: '14px', fontWeight: '600', fontFamily: FONT_BODY, color: colors.textSecondary }}>Фото</label>
            <button type="button" onClick={() => fileInputRef.current?.click()}
                    style={{
                        width: '100%', padding: '14px 16px', fontSize: '15px', fontFamily: FONT_BODY,
                        border: '1px dashed rgba(180,180,170,0.5)', borderRadius: '10px',
                        backgroundColor: '#FAFAF8', color: colors.textMuted, cursor: 'pointer',
                        textAlign: 'left', minHeight: '52px', transition: 'all 0.2s ease',
                    }}
                    onMouseOver={(e) => e.currentTarget.style.borderColor = colors.primary}
                    onFocus={(e) => e.currentTarget.style.borderColor = colors.primary}
                    onMouseOut={(e) => e.currentTarget.style.borderColor = 'rgba(180,180,170,0.5)'}
                    onBlur={(e) => e.currentTarget.style.borderColor = 'rgba(180,180,170,0.5)'}>
                {form.photoPath ? 'Фото выбрано' : 'Выбрать файл...'}
            </button>
            <input type="file" accept="image/*" ref={fileInputRef} onChange={handleFileChange} style={{ display: 'none' }} />
            {form.photoPath && (
                <img src={form.photoPath} alt="" style={{
                    width: '72px', height: '72px', borderRadius: '50%',
                    objectFit: 'cover', marginTop: SPACING.sm,
                    border: `2px solid ${colors.glassBorder}`,
                }} />
            )}
        </div>
        <div>
            <label style={{ display: 'block', marginBottom: '6px', fontSize: '14px', fontWeight: '600', fontFamily: FONT_BODY, color: colors.textSecondary }}>Описание</label>
            <textarea name="description" value={form.description} onChange={handleChange} style={textareaStyle} placeholder="Опыт, сертификаты, направления..." />
        </div>
        <div style={{ position: 'relative', zIndex: 10 }} ref={statusRef}>
            <label style={{ display: 'block', marginBottom: '6px', fontSize: '14px', fontWeight: '600', fontFamily: FONT_BODY, color: colors.textSecondary }}>Статус</label>
            <button type="button" onClick={() => setStatusOpen(!statusOpen)}
                    style={{
                        width: '100%', padding: '14px 16px', fontSize: '15px', fontFamily: FONT_BODY, fontWeight: '500',
                        textAlign: 'left', border: '1px solid rgba(180,180,170,0.5)',
                        borderRadius: statusOpen ? '10px 10px 0 0' : '10px', outline: 'none',
                        backgroundColor: '#FAFAF8', color: colors.textPrimary, cursor: 'pointer',
                        display: 'flex', alignItems: 'center', justifyContent: 'space-between', boxSizing: 'border-box',
                    }}>
                {statusOptions.find((o) => o.value === form.status)?.label || 'Активен'}
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={colors.primaryDark} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
                     style={{ transform: statusOpen ? 'rotate(180deg)' : 'rotate(0)', transition: 'transform 0.2s ease', marginLeft: '12px', flexShrink: 0 }}>
                    <polyline points="6 9 12 15 18 9" />
                </svg>
            </button>
            {statusOpen && (
                <div style={{
                    position: 'absolute', top: '100%', left: 0, right: 0, zIndex: 100,
                    backgroundColor: 'white', border: '1px solid rgba(180,180,170,0.5)', borderTop: 'none',
                    borderRadius: '0 0 10px 10px', boxShadow: '0 12px 32px rgba(77,80,68,0.18)', overflow: 'hidden',
                }}>
                    {statusOptions.map((opt) => (
                        <button key={opt.value} type="button"
                                onClick={() => handleStatusSelect(opt.value)}
                                style={{
                                    width: '100%', padding: '14px 16px', fontSize: '15px', fontFamily: FONT_BODY,
                                    fontWeight: opt.value === form.status ? '600' : '400',
                                    color: opt.value === form.status ? colors.primaryDark : colors.textSecondary,
                                    backgroundColor: opt.value === form.status ? colors.primaryLight : 'white',
                                    border: 'none', cursor: 'pointer', textAlign: 'left', transition: 'background-color 0.15s ease',
                                }}
                                onMouseOver={(e) => e.currentTarget.style.backgroundColor = colors.primaryLight}
                                onMouseOut={(e) => e.currentTarget.style.backgroundColor = opt.value === form.status ? colors.primaryLight : 'white'}>
                            {opt.label}
                        </button>
                    ))}
                </div>
            )}
        </div>
    </div>
);

const ActionButton = ({ label, onClick, disabled, isDanger }) => (
    <button type="button" onClick={onClick} disabled={disabled}
            style={{
                padding: '14px 24px', borderRadius: '14px', cursor: disabled ? 'not-allowed' : 'pointer',
                fontFamily: FONT_BODY, fontSize: '16px', fontWeight: '600', minHeight: '52px', transition: 'all 0.2s ease',
                border: isDanger ? `1px solid ${colors.error}` : 'none',
                backgroundColor: isDanger ? 'transparent' : colors.primaryDark,
                color: isDanger ? colors.error : colors.textOnPrimary,
                opacity: disabled ? 0.7 : 1,
            }}
            onMouseOver={(e) => {
                if (disabled) return;
                if (isDanger) { e.currentTarget.style.backgroundColor = colors.error; e.currentTarget.style.color = colors.textOnPrimary; }
                else e.currentTarget.style.backgroundColor = colors.primaryHover;
            }}
            onFocus={(e) => {
                if (disabled) return;
                if (isDanger) { e.currentTarget.style.backgroundColor = colors.error; e.currentTarget.style.color = colors.textOnPrimary; }
                else e.currentTarget.style.backgroundColor = colors.primaryHover;
            }}
            onMouseOut={(e) => {
                if (isDanger) { e.currentTarget.style.backgroundColor = 'transparent'; e.currentTarget.style.color = colors.error; }
                else e.currentTarget.style.backgroundColor = colors.primaryDark;
            }}
            onBlur={(e) => {
                if (isDanger) { e.currentTarget.style.backgroundColor = 'transparent'; e.currentTarget.style.color = colors.error; }
                else e.currentTarget.style.backgroundColor = colors.primaryDark;
            }}>
        {label}
    </button>
);

export default Trainers;