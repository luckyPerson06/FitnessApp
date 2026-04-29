import { useState, useEffect, useCallback } from 'react';
import { clientApi } from '../api/clientApi';
import { useAuth } from '../contexts/useAuth';

const FONT_HEADING = "'Cormorant Garamond', 'Times New Roman', serif";
const FONT_BODY = "'Inter', 'Segoe UI', sans-serif";

const colors = {
    primary: '#979B81',
    primaryDark: '#4D5044',
    primaryLight: '#E4E7D6',
    textPrimary: '#0F0F10',
    textSecondary: '#5F6256',
    textMuted: '#8C8F84',
    textOnPrimary: '#FFFFFF',
    error: '#C87B7B',
    glassBorder: 'rgba(180,180,170,0.5)',
    backgroundCard: 'rgba(255,255,255,0.75)',
    shadowCard: '0 2px 16px rgba(77,80,68,0.06)',
    headerBg: 'rgba(77,80,68,0.06)',
    rowHover: 'rgba(151,155,129,0.08)',
};

const SPACING = { xs: '6px', sm: '14px', md: '24px', lg: '36px' };

const headingStyle = {
    fontSize: 'clamp(26px, 5vw, 38px)',
    fontWeight: '300',
    color: colors.primaryDark,
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
    transition: 'border-color 0.2s ease, box-shadow 0.2s ease',
};

const tableCellStyle = {
    padding: '14px 16px',
    fontSize: '14px',
    fontFamily: FONT_BODY,
    color: colors.textPrimary,
    borderBottom: `1px solid ${colors.glassBorder}`,
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
};

const headerCellStyle = {
    ...tableCellStyle,
    fontWeight: '600',
    color: colors.primaryDark,
    fontSize: '13px',
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
    backgroundColor: colors.headerBg,
};

function AdminClients() {
    const [clients, setClients] = useState([]);
    const [filteredClients, setFilteredClients] = useState([]);
    const [searchFullName, setSearchFullName] = useState('');
    const [searchPhone, setSearchPhone] = useState('');
    const [searchEmail, setSearchEmail] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const { user } = useAuth();
    const isAdmin = user?.role === 'ADMIN';

    const fetchClients = useCallback(async () => {
        try {
            const response = await clientApi.getAll();
            setClients(response.data);
            setFilteredClients(response.data);
        } catch {
            setError('Не удалось загрузить список клиентов');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        let cancelled = false;
        if (!cancelled) fetchClients();
        return () => { cancelled = true; };
    }, [fetchClients]);

    useEffect(() => {
        const filtered = clients.filter((client) => {
            const matchName = searchFullName
                ? client.fullName
                    .toLowerCase()
                    .includes(searchFullName.toLowerCase())
                : true;
            const matchPhone = searchPhone
                ? (client.phoneNumber || '')
                    .toLowerCase()
                    .includes(searchPhone.toLowerCase())
                : true;
            const matchEmail = searchEmail
                ? client.email
                    .toLowerCase()
                    .includes(searchEmail.toLowerCase())
                : true;
            return matchName && matchPhone && matchEmail;
        });
        setFilteredClients(filtered);
    }, [clients, searchFullName, searchPhone, searchEmail]);

    const handleClearFilters = () => {
        setSearchFullName('');
        setSearchPhone('');
        setSearchEmail('');
    };

    const hasFilters = searchFullName || searchPhone || searchEmail;

    if (!isAdmin) {
        return (
            <div style={{
                minHeight: '60vh',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontFamily: FONT_BODY,
                color: colors.error,
                fontSize: '16px',
            }}>
                Нет доступа
            </div>
        );
    }

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
        <div style={{
            padding: `${SPACING.sm} ${SPACING.sm} ${SPACING.lg}`,
        }}>
            <div style={{ textAlign: 'center', marginBottom: SPACING.md }}>
                <h1 style={headingStyle}>Клиенты</h1>
            </div>

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
                    maxWidth: '900px',
                    marginLeft: 'auto',
                    marginRight: 'auto',
                }}>
                    {error}
                </div>
            )}

            <div style={{
                maxWidth: '1100px',
                margin: '0 auto',
                display: 'flex',
                gap: SPACING.md,
                alignItems: 'flex-start',
                flexWrap: 'wrap',
            }}>
                <div style={{
                    flex: 1,
                    minWidth: 0,
                    backgroundColor: colors.backgroundCard,
                    backdropFilter: 'blur(16px)',
                    WebkitBackdropFilter: 'blur(16px)',
                    borderRadius: '16px',
                    border: `1px solid ${colors.glassBorder}`,
                    boxShadow: colors.shadowCard,
                    overflow: 'hidden',
                }}>
                    <div style={{
                        maxHeight: '70vh',
                        overflowY: 'auto',
                    }}>
                        <table style={{
                            width: '100%',
                            borderCollapse: 'collapse',
                            tableLayout: 'fixed',
                        }}>
                            <colgroup>
                                <col style={{ width: '40%' }} />
                                <col style={{ width: '25%' }} />
                                <col style={{ width: '35%' }} />
                            </colgroup>
                            <thead>
                            <tr>
                                <th style={{
                                    ...headerCellStyle,
                                    position: 'sticky',
                                    top: 0,
                                    zIndex: 1,
                                }}>
                                    ФИО
                                </th>
                                <th style={{
                                    ...headerCellStyle,
                                    position: 'sticky',
                                    top: 0,
                                    zIndex: 1,
                                }}>
                                    Телефон
                                </th>
                                <th style={{
                                    ...headerCellStyle,
                                    position: 'sticky',
                                    top: 0,
                                    zIndex: 1,
                                }}>
                                    Почта
                                </th>
                            </tr>
                            </thead>
                            <tbody>
                            {filteredClients.length === 0 ? (
                                <tr>
                                    <td
                                        colSpan={3}
                                        style={{
                                            ...tableCellStyle,
                                            textAlign: 'center',
                                            color: colors.textMuted,
                                            padding: SPACING.lg,
                                        }}
                                    >
                                        {hasFilters
                                            ? 'Ничего не найдено'
                                            : 'Нет клиентов'}
                                    </td>
                                </tr>
                            ) : (
                                filteredClients.map((client) => (
                                    <tr
                                        key={client.id}
                                        style={{
                                            transition: 'background-color 0.15s ease',
                                        }}
                                        onMouseOver={(e) => {
                                            e.currentTarget.style
                                                .backgroundColor =
                                                colors.rowHover;
                                        }}
                                        onMouseOut={(e) => {
                                            e.currentTarget.style
                                                .backgroundColor =
                                                'transparent';
                                        }}
                                    >
                                        <td style={tableCellStyle}>
                                            {client.fullName}
                                        </td>
                                        <td style={{
                                            ...tableCellStyle,
                                            color: client.phoneNumber
                                                ? colors.textPrimary
                                                : colors.textMuted,
                                        }}>
                                            {client.phoneNumber || '—'}
                                        </td>
                                        <td style={tableCellStyle}>
                                            {client.email}
                                        </td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </div>
                    <div style={{
                        padding: '10px 16px',
                        borderTop: `1px solid ${colors.glassBorder}`,
                        fontSize: '13px',
                        color: colors.textMuted,
                        fontFamily: FONT_BODY,
                        backgroundColor: colors.headerBg,
                    }}>
                        Всего: {filteredClients.length}
                        {hasFilters && ` из ${clients.length}`}
                    </div>
                </div>

                <div style={{
                    width: '260px',
                    flexShrink: 0,
                    display: 'flex',
                    flexDirection: 'column',
                    gap: SPACING.sm,
                }}>
                    <div style={{
                        backgroundColor: colors.backgroundCard,
                        backdropFilter: 'blur(16px)',
                        WebkitBackdropFilter: 'blur(16px)',
                        borderRadius: '14px',
                        border: `1px solid ${colors.glassBorder}`,
                        boxShadow: colors.shadowCard,
                        padding: SPACING.sm,
                    }}>
                        <div style={{
                            fontSize: '15px',
                            fontWeight: '600',
                            fontFamily: FONT_BODY,
                            color: colors.primaryDark,
                            marginBottom: SPACING.sm,
                            textAlign: 'center',
                        }}>
                            Поиск
                        </div>
                        <div style={{
                            display: 'flex',
                            flexDirection: 'column',
                            gap: SPACING.sm,
                        }}>
                            <div>
                                <label style={{
                                    display: 'block',
                                    marginBottom: '4px',
                                    fontSize: '12px',
                                    fontWeight: '600',
                                    fontFamily: FONT_BODY,
                                    color: colors.textSecondary,
                                    textTransform: 'uppercase',
                                    letterSpacing: '0.5px',
                                }}>
                                    ФИО
                                </label>
                                <input
                                    type="text"
                                    value={searchFullName}
                                    onChange={(e) =>
                                        setSearchFullName(e.target.value)
                                    }
                                    placeholder="Иванов Иван"
                                    style={inputStyle}
                                    onFocus={(e) => {
                                        e.currentTarget.style.borderColor =
                                            colors.primary;
                                        e.currentTarget.style.boxShadow =
                                            '0 0 0 3px rgba(151,155,129,0.15)';
                                    }}
                                    onBlur={(e) => {
                                        e.currentTarget.style.borderColor =
                                            'rgba(180,180,170,0.5)';
                                        e.currentTarget.style.boxShadow =
                                            'none';
                                    }}
                                />
                            </div>
                            <div>
                                <label style={{
                                    display: 'block',
                                    marginBottom: '4px',
                                    fontSize: '12px',
                                    fontWeight: '600',
                                    fontFamily: FONT_BODY,
                                    color: colors.textSecondary,
                                    textTransform: 'uppercase',
                                    letterSpacing: '0.5px',
                                }}>
                                    Телефон
                                </label>
                                <input
                                    type="text"
                                    value={searchPhone}
                                    onChange={(e) =>
                                        setSearchPhone(e.target.value)
                                    }
                                    placeholder="+375 29"
                                    style={inputStyle}
                                    onFocus={(e) => {
                                        e.currentTarget.style.borderColor =
                                            colors.primary;
                                        e.currentTarget.style.boxShadow =
                                            '0 0 0 3px rgba(151,155,129,0.15)';
                                    }}
                                    onBlur={(e) => {
                                        e.currentTarget.style.borderColor =
                                            'rgba(180,180,170,0.5)';
                                        e.currentTarget.style.boxShadow =
                                            'none';
                                    }}
                                />
                            </div>
                            <div>
                                <label style={{
                                    display: 'block',
                                    marginBottom: '4px',
                                    fontSize: '12px',
                                    fontWeight: '600',
                                    fontFamily: FONT_BODY,
                                    color: colors.textSecondary,
                                    textTransform: 'uppercase',
                                    letterSpacing: '0.5px',
                                }}>
                                    Почта
                                </label>
                                <input
                                    type="text"
                                    value={searchEmail}
                                    onChange={(e) =>
                                        setSearchEmail(e.target.value)
                                    }
                                    placeholder="ivan@mail.com"
                                    style={inputStyle}
                                    onFocus={(e) => {
                                        e.currentTarget.style.borderColor =
                                            colors.primary;
                                        e.currentTarget.style.boxShadow =
                                            '0 0 0 3px rgba(151,155,129,0.15)';
                                    }}
                                    onBlur={(e) => {
                                        e.currentTarget.style.borderColor =
                                            'rgba(180,180,170,0.5)';
                                        e.currentTarget.style.boxShadow =
                                            'none';
                                    }}
                                />
                            </div>
                        </div>
                        {hasFilters && (
                            <button
                                type="button"
                                onClick={handleClearFilters}
                                style={{
                                    marginTop: SPACING.sm,
                                    width: '100%',
                                    padding: '10px',
                                    borderRadius: '8px',
                                    border: 'none',
                                    backgroundColor: 'transparent',
                                    color: colors.error,
                                    fontFamily: FONT_BODY,
                                    fontSize: '13px',
                                    fontWeight: '600',
                                    cursor: 'pointer',
                                    transition: 'all 0.2s ease',
                                }}
                                onMouseOver={(e) => {
                                    e.currentTarget.style.backgroundColor =
                                        '#FFF0F0';
                                }}
                                onMouseOut={(e) => {
                                    e.currentTarget.style.backgroundColor =
                                        'transparent';
                                }}
                            >
                                Сбросить фильтры
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default AdminClients;