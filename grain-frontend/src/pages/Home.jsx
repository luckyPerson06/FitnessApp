import { useState, useEffect, useCallback } from 'react';
import { clubInfoApi } from '../api/clubInfoApi';
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
    shadowCard: '0 8px 32px rgba(77, 80, 68, 0.08)',
    glassBorder: 'rgba(180,180,170,0.5)',
    backgroundCard: 'rgba(255,255,255,0.65)',
};

const SPACING = {
    xs: '6px',
    sm: '14px',
    md: '22px',
    lg: '36px',
};

const containerStyle = {
    width: '100%',
    maxWidth: '1100px',
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
    marginBottom: SPACING.xs,
};

const cardStyle = {
    backgroundColor: colors.backgroundCard,
    backdropFilter: 'blur(16px)',
    WebkitBackdropFilter: 'blur(16px)',
    borderRadius: '20px',
    border: `1px solid ${colors.glassBorder}`,
    boxShadow: colors.shadowCard,
    padding: `${SPACING.md} ${SPACING.sm}`,
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

const textareaStyle = {
    ...inputStyle,
    minHeight: '100px',
    resize: 'vertical',
};

const btnPrimary = {
    padding: '12px 22px',
    borderRadius: '12px',
    border: 'none',
    backgroundColor: colors.primaryDark,
    color: colors.textOnPrimary,
    fontFamily: FONT_BODY,
    fontSize: '14px',
    fontWeight: '600',
    cursor: 'pointer',
    minHeight: '48px',
};

const btnDanger = {
    ...btnPrimary,
    backgroundColor: 'transparent',
    color: colors.error,
    border: `1px solid ${colors.error}`,
};

const editIconBtn = {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '36px',
    height: '36px',
    borderRadius: '8px',
    color: colors.primaryDark,
    backgroundColor: 'rgba(255,255,255,0.6)',
    border: `1px solid ${colors.glassBorder}`,
    cursor: 'pointer',
    transition: 'all 0.2s ease',
    flexShrink: 0,
};

const EditIcon = () => (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" strokeWidth="1.8"
         strokeLinecap="round" strokeLinejoin="round">
        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
    </svg>
);

const PhoneIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
         stroke={colors.primaryDark} strokeWidth="1.8"
         strokeLinecap="round" strokeLinejoin="round">
        <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z" />
    </svg>
);

const EmailIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
         stroke={colors.primaryDark} strokeWidth="1.8"
         strokeLinecap="round" strokeLinejoin="round">
        <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
        <polyline points="22,6 12,13 2,6" />
    </svg>
);

const ClockIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
         stroke={colors.primaryDark} strokeWidth="1.8"
         strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="10" />
        <polyline points="12 6 12 12 16 14" />
    </svg>
);

const InstagramIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
         stroke={colors.primaryDark} strokeWidth="1.8"
         strokeLinecap="round" strokeLinejoin="round">
        <rect x="2" y="2" width="20" height="20" rx="5" ry="5" />
        <circle cx="12" cy="12" r="5" />
        <line x1="17.5" y1="6.5" x2="17.51" y2="6.5" strokeWidth="2" />
    </svg>
);

function Home() {
    const [clubInfo, setClubInfo] = useState(null);
    const [form, setForm] = useState(null);
    const [editing, setEditing] = useState(false);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const { user } = useAuth();
    const isAdmin = user?.role === 'ADMIN';

    const fetchClubInfo = useCallback(async () => {
        try {
            const response = await clubInfoApi.get();
            setClubInfo(response.data);
        } catch {
            setError('Не удалось загрузить информацию о клубе');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        let cancelled = false;
        if (!cancelled) fetchClubInfo();
        return () => { cancelled = true; };
    }, [fetchClubInfo]);

    const handleEdit = () => {
        setForm({
            aboutText: clubInfo?.aboutText || '',
            address: clubInfo?.address || '',
            phone: clubInfo?.phone || '',
            email: clubInfo?.email || '',
            workingHours: clubInfo?.workingHours || '',
            mapCoordinates: clubInfo?.mapCoordinates || '',
            instagramUrl: clubInfo?.socialLinks?.instagram || '',
            seoTitle: clubInfo?.seoTitle || '',
            seoDescription: clubInfo?.seoDescription || '',
        });
        setEditing(true);
        setError('');
        setSuccess('');
    };

    const handleCancel = () => {
        setEditing(false);
        setForm(null);
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm((prev) => ({ ...prev, [name]: value }));
    };

    const handleSave = async () => {
        setSaving(true);
        setError('');
        setSuccess('');
        try {
            const payload = {
                ...clubInfo,
                aboutText: form.aboutText,
                address: form.address,
                phone: form.phone,
                email: form.email,
                workingHours: form.workingHours,
                mapCoordinates: form.mapCoordinates,
                socialLinks: {
                    instagram: form.instagramUrl,
                    telegram: clubInfo?.socialLinks?.telegram || '',
                    vk: clubInfo?.socialLinks?.vk || '',
                    whatsapp: clubInfo?.socialLinks?.whatsapp || '',
                },
                seoTitle: form.seoTitle,
                seoDescription: form.seoDescription,
            };
            await clubInfoApi.update(payload);
            setSuccess('Информация обновлена');
            setEditing(false);
            setForm(null);
            fetchClubInfo();
        } catch {
            setError('Не удалось сохранить изменения');
        } finally {
            setSaving(false);
        }
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

    return (
        <>
            <div style={{ padding: `${SPACING.sm} ${SPACING.sm} 0` }}>
                <div style={containerStyle}>
                    <div style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: SPACING.xs,
                    }}>
                        <h1 style={{
                            fontSize: 'clamp(34px, 7vw, 48px)',
                            fontWeight: '300',
                            color: colors.primary,
                            letterSpacing: '10px',
                            marginBottom: '0',
                            fontFamily: FONT_HEADING,
                        }}>
                            KVETKA
                        </h1>
                        {isAdmin && !editing && (
                            <button
                                type="button"
                                onClick={handleEdit}
                                style={editIconBtn}
                                onMouseOver={(e) => {
                                    e.currentTarget.style.backgroundColor =
                                        colors.primaryDark;
                                    e.currentTarget.style.color = '#FFFFFF';
                                }}
                                onFocus={(e) => {
                                    e.currentTarget.style.backgroundColor =
                                        colors.primaryDark;
                                    e.currentTarget.style.color = '#FFFFFF';
                                }}
                                onMouseOut={(e) => {
                                    e.currentTarget.style.backgroundColor =
                                        'rgba(255,255,255,0.6)';
                                    e.currentTarget.style.color =
                                        colors.primaryDark;
                                }}
                                onBlur={(e) => {
                                    e.currentTarget.style.backgroundColor =
                                        'rgba(255,255,255,0.6)';
                                    e.currentTarget.style.color =
                                        colors.primaryDark;
                                }}
                            >
                                <EditIcon />
                            </button>
                        )}
                    </div>
                    <p style={{
                        color: colors.textMuted,
                        fontSize: 'clamp(12px, 2vw, 15px)',
                        letterSpacing: '8px',
                        textTransform: 'uppercase',
                        textAlign: 'center',
                        marginTop: SPACING.xs,
                        marginBottom: SPACING.lg,
                        fontFamily: FONT_BODY,
                    }}>
                        Пространство для тела и души
                    </p>

                    {error && (
                        <div style={{
                            padding: '12px 16px',
                            backgroundColor: '#FFF0F0',
                            borderRadius: '10px',
                            color: colors.error,
                            fontFamily: FONT_BODY,
                            fontSize: '14px',
                            marginBottom: SPACING.sm,
                            textAlign: 'center',
                        }}>
                            {error}
                        </div>
                    )}
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

                    {editing ? (
                        <div style={{ ...cardStyle, marginBottom: SPACING.lg }}>
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
                                        О клубе
                                    </label>
                                    <textarea
                                        name="aboutText"
                                        value={form.aboutText}
                                        onChange={handleChange}
                                        style={textareaStyle}
                                    />
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
                                        Адрес
                                    </label>
                                    <input
                                        name="address"
                                        value={form.address}
                                        onChange={handleChange}
                                        style={inputStyle}
                                    />
                                </div>
                                <div style={{
                                    display: 'grid',
                                    gridTemplateColumns:
                                        'repeat(auto-fit, minmax(200px, 1fr))',
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
                                            Телефон
                                        </label>
                                        <input
                                            name="phone"
                                            value={form.phone}
                                            onChange={handleChange}
                                            style={inputStyle}
                                        />
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
                                            Email
                                        </label>
                                        <input
                                            name="email"
                                            value={form.email}
                                            onChange={handleChange}
                                            style={inputStyle}
                                        />
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
                                        Часы работы
                                    </label>
                                    <input
                                        name="workingHours"
                                        value={form.workingHours}
                                        onChange={handleChange}
                                        style={inputStyle}
                                    />
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
                                        Координаты для карты
                                    </label>
                                    <input
                                        name="mapCoordinates"
                                        value={form.mapCoordinates}
                                        onChange={handleChange}
                                        style={inputStyle}
                                        placeholder="52.1234,26.5678"
                                    />
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
                                        Instagram
                                    </label>
                                    <input
                                        name="instagramUrl"
                                        value={form.instagramUrl}
                                        onChange={handleChange}
                                        style={inputStyle}
                                    />
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
                                        SEO заголовок
                                    </label>
                                    <input
                                        name="seoTitle"
                                        value={form.seoTitle}
                                        onChange={handleChange}
                                        style={inputStyle}
                                    />
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
                                        SEO описание
                                    </label>
                                    <textarea
                                        name="seoDescription"
                                        value={form.seoDescription}
                                        onChange={handleChange}
                                        style={{ ...textareaStyle, minHeight: '60px' }}
                                    />
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
                                    style={btnDanger}
                                >
                                    Отмена
                                </button>
                                <button
                                    type="button"
                                    onClick={handleSave}
                                    disabled={saving}
                                    style={{
                                        ...btnPrimary,
                                        opacity: saving ? 0.7 : 1,
                                    }}
                                >
                                    {saving ? 'Сохранение...' : 'Сохранить'}
                                </button>
                            </div>
                        </div>
                    ) : (
                        <>
                            {clubInfo?.aboutText && (
                                <div style={{ marginBottom: SPACING.lg }}>
                                    <h2 style={headingStyle}>О клубе</h2>
                                    <p style={{
                                        fontSize: 'clamp(15px, 2.5vw, 18px)',
                                        lineHeight: 1.7,
                                        color: colors.textSecondary,
                                        fontFamily: FONT_BODY,
                                        textAlign: 'center',
                                        maxWidth: '720px',
                                        margin: '0 auto',
                                    }}>
                                        {clubInfo.aboutText}
                                    </p>
                                </div>
                            )}
                        </>
                    )}
                </div>
            </div>

            {!editing && (
                <>
                    {clubInfo && (clubInfo.phone || clubInfo.email
                        || clubInfo.workingHours
                        || clubInfo.socialLinks?.instagram) && (
                        <div style={{ padding: `0 ${SPACING.sm} ${SPACING.lg}` }}>
                            <div style={{ ...containerStyle, maxWidth: '720px' }}>
                                <div style={cardStyle}>
                                    <h2 style={headingStyle}>Контакты</h2>
                                    <div style={{
                                        display: 'grid',
                                        gridTemplateColumns:
                                            'repeat(auto-fit, minmax(220px, 1fr))',
                                        gap: SPACING.xs,
                                    }}>
                                        {clubInfo.phone && (
                                            <ContactItem
                                                icon={<PhoneIcon />}
                                                text={clubInfo.phone}
                                                href={`tel:${clubInfo.phone}`}
                                            />
                                        )}
                                        {clubInfo.email && (
                                            <ContactItem
                                                icon={<EmailIcon />}
                                                text={clubInfo.email}
                                                href={`mailto:${clubInfo.email}`}
                                            />
                                        )}
                                        {clubInfo.workingHours && (
                                            <ContactItem
                                                icon={<ClockIcon />}
                                                text={clubInfo.workingHours}
                                            />
                                        )}
                                        {clubInfo.socialLinks?.instagram && (
                                            <ContactItem
                                                icon={<InstagramIcon />}
                                                text="Instagram"
                                                href={
                                                    clubInfo.socialLinks.instagram
                                                }
                                                external
                                            />
                                        )}
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    {clubInfo?.mapCoordinates && clubInfo?.address && (
                        <div style={{
                            padding: `0 ${SPACING.sm} ${SPACING.lg}`,
                        }}>
                            <div style={{
                                ...containerStyle,
                                maxWidth: '820px',
                            }}>
                                <h2 style={headingStyle}>
                                    Где мы находимся
                                </h2>
                                <p style={{
                                    textAlign: 'center',
                                    fontFamily: FONT_BODY,
                                    fontSize: 'clamp(15px, 2.5vw, 18px)',
                                    color: colors.textSecondary,
                                    marginBottom: SPACING.sm,
                                }}>
                                    {clubInfo.address}
                                </p>
                                <div style={{
                                    ...cardStyle,
                                    overflow: 'hidden',
                                    padding: '0',
                                    aspectRatio: '16/7',
                                    minHeight: '270px',
                                }}>
                                    <iframe
                                        title="Карта"
                                        src={`https://maps.google.com/maps?q=${clubInfo.mapCoordinates}&z=16&output=embed`}
                                        width="100%"
                                        height="100%"
                                        style={{ border: 'none' }}
                                        allowFullScreen=""
                                        loading="lazy"
                                        referrerPolicy="no-referrer-when-downgrade"
                                    />
                                </div>
                            </div>
                        </div>
                    )}
                </>
            )}
        </>
    );
}

const ContactItem = ({ icon, text, href, external }) => {
    const content = (
        <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            padding: '12px 16px',
            borderRadius: '10px',
            backgroundColor: 'rgba(255,255,255,0.5)',
            fontFamily: FONT_BODY,
            fontSize: 'clamp(14px, 2vw, 15px)',
            color: colors.textSecondary,
            minHeight: '48px',
        }}>
            <span style={{
                flexShrink: 0,
                display: 'flex',
                alignItems: 'center',
            }}>
                {icon}
            </span>
            <span style={{ lineHeight: 1.5 }}>{text}</span>
        </div>
    );
    if (href) {
        return (
            <a
                href={href}
                target={external ? '_blank' : undefined}
                rel={external ? 'noopener noreferrer' : undefined}
                style={{
                    textDecoration: 'none',
                    color: 'inherit',
                    display: 'block',
                }}
            >
                {content}
            </a>
        );
    }
    return content;
};

export default Home;