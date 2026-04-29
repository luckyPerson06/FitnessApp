import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/useAuth';

const FONT_BODY = "'Inter', 'Segoe UI', sans-serif";

const colors = {
    primary: '#979B81',
    primaryDark: '#4D5044',
    textOnPrimary: '#FFFFFF',
    textLight: '#E4E7D6',
    error: '#E4A0A0',
    navBg: '#3A3D34',
    activeBg: 'rgba(255,255,255,0.15)',
};

const linkStyle = {
    color: colors.textOnPrimary,
    textDecoration: 'none',
    fontSize: '16px',
    fontWeight: '600',
    fontFamily: FONT_BODY,
    letterSpacing: '0.4px',
    padding: '12px 18px',
    borderRadius: '10px',
    transition: 'all 0.2s ease',
    minHeight: '48px',
    display: 'flex',
    alignItems: 'center',
    cursor: 'pointer',
};

function Navbar() {
    const { user, isAuthenticated, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const isAdmin = user?.role === 'ADMIN';
    const currentPath = location.pathname;

    const isActive = (path) => {
        if (path === '/') return currentPath === '/';
        return currentPath.startsWith(path);
    };

    const getLinkStyle = (path) => ({
        ...linkStyle,
        backgroundColor: isActive(path) ? colors.activeBg : 'transparent',
    });

    const handleLinkHover = (e) => {
        e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.15)';
        e.currentTarget.style.transform = 'scale(1.03)';
    };

    const handleLinkBlur = (e, path) => {
        e.currentTarget.style.backgroundColor = isActive(path)
            ? colors.activeBg
            : 'transparent';
        e.currentTarget.style.transform = 'scale(1)';
    };

    return (
        <div style={{ padding: '12px 16px 0' }}>
            <nav style={{
                backgroundColor: colors.navBg,
                padding: '12px 20px',
                borderRadius: '16px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: '8px',
                boxShadow: '0 4px 20px rgba(0,0,0,0.2)',
            }}>
                <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    flexWrap: 'wrap',
                }}>
                    <Link to="/" style={{
                        fontSize: '26px',
                        fontWeight: '300',
                        color: colors.primary,
                        letterSpacing: '6px',
                        textDecoration: 'none',
                        fontFamily:
                            "'Cormorant Garamond', 'Times New Roman', serif",
                        padding: '8px 12px',
                    }}>
                        KVETKA
                    </Link>

                    <Link
                        to="/"
                        style={getLinkStyle('/')}
                        onMouseOver={handleLinkHover}
                        onFocus={handleLinkHover}
                        onMouseOut={(e) => handleLinkBlur(e, '/')}
                        onBlur={(e) => handleLinkBlur(e, '/')}
                    >
                        О клубе
                    </Link>
                    <Link
                        to="/directions"
                        style={getLinkStyle('/directions')}
                        onMouseOver={handleLinkHover}
                        onFocus={handleLinkHover}
                        onMouseOut={(e) =>
                            handleLinkBlur(e, '/directions')
                        }
                        onBlur={(e) => handleLinkBlur(e, '/directions')}
                    >
                        Направления
                    </Link>
                    <Link
                        to="/schedule"
                        style={getLinkStyle('/schedule')}
                        onMouseOver={handleLinkHover}
                        onFocus={handleLinkHover}
                        onMouseOut={(e) =>
                            handleLinkBlur(e, '/schedule')
                        }
                        onBlur={(e) => handleLinkBlur(e, '/schedule')}
                    >
                        Расписание
                    </Link>
                    <Link
                        to="/trainers"
                        style={getLinkStyle('/trainers')}
                        onMouseOver={handleLinkHover}
                        onFocus={handleLinkHover}
                        onMouseOut={(e) =>
                            handleLinkBlur(e, '/trainers')
                        }
                        onBlur={(e) => handleLinkBlur(e, '/trainers')}
                    >
                        Тренеры
                    </Link>
                    <Link
                        to="/prices"
                        style={getLinkStyle('/prices')}
                        onMouseOver={handleLinkHover}
                        onFocus={handleLinkHover}
                        onMouseOut={(e) =>
                            handleLinkBlur(e, '/prices')
                        }
                        onBlur={(e) => handleLinkBlur(e, '/prices')}
                    >
                        Абонементы
                    </Link>

                    {isAdmin && (
                        <>
                            <span style={{
                                color: colors.textLight,
                                fontSize: '14px',
                                opacity: 0.3,
                                fontFamily: FONT_BODY,
                                padding: '0 4px',
                            }}>
                                |
                            </span>
                            <Link
                                to="/admin/clients"
                                style={getLinkStyle('/admin/clients')}
                                onMouseOver={handleLinkHover}
                                onFocus={handleLinkHover}
                                onMouseOut={(e) =>
                                    handleLinkBlur(e, '/admin/clients')
                                }
                                onBlur={(e) =>
                                    handleLinkBlur(e, '/admin/clients')
                                }
                            >
                                Клиенты
                            </Link>
                        </>
                    )}
                </div>

                <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '12px',
                }}>
                    {isAuthenticated ? (
                        <>
                            {isAdmin ? (
                                <span style={{
                                    color: colors.textLight,
                                    fontSize: '16px',
                                    fontFamily: FONT_BODY,
                                    fontWeight: '500',
                                    padding: '12px 8px',
                                }}>
                                    Администратор
                                </span>
                            ) : (
                                <span style={{
                                    color: colors.textLight,
                                    fontSize: '16px',
                                    fontFamily: FONT_BODY,
                                    fontWeight: '500',
                                    padding: '12px 8px',
                                }}>
                                    {user?.displayName || user?.email}
                                </span>
                            )}
                            <button
                                onClick={handleLogout}
                                type="button"
                                style={{
                                    backgroundColor: 'transparent',
                                    color: colors.error,
                                    border: `2px solid ${colors.error}`,
                                    padding: '12px 20px',
                                    borderRadius: '10px',
                                    cursor: 'pointer',
                                    fontSize: '16px',
                                    fontFamily: FONT_BODY,
                                    fontWeight: '600',
                                    minHeight: '48px',
                                    transition: 'all 0.2s ease',
                                }}
                                onMouseOver={(e) => {
                                    e.currentTarget.style.backgroundColor =
                                        colors.error;
                                    e.currentTarget.style.color =
                                        colors.textOnPrimary;
                                    e.currentTarget.style.transform =
                                        'scale(1.03)';
                                }}
                                onFocus={(e) => {
                                    e.currentTarget.style.backgroundColor =
                                        colors.error;
                                    e.currentTarget.style.color =
                                        colors.textOnPrimary;
                                }}
                                onMouseOut={(e) => {
                                    e.currentTarget.style.backgroundColor =
                                        'transparent';
                                    e.currentTarget.style.color =
                                        colors.error;
                                    e.currentTarget.style.transform =
                                        'scale(1)';
                                }}
                                onBlur={(e) => {
                                    e.currentTarget.style.backgroundColor =
                                        'transparent';
                                    e.currentTarget.style.color =
                                        colors.error;
                                }}
                            >
                                Выйти
                            </button>
                        </>
                    ) : (
                        <Link
                            to="/login"
                            style={{
                                ...linkStyle,
                                backgroundColor: colors.textOnPrimary,
                                color: colors.navBg,
                                fontWeight: '700',
                                padding: '12px 22px',
                                boxShadow:
                                    '0 2px 8px rgba(0,0,0,0.15)',
                            }}
                        >
                            Войти
                        </Link>
                    )}
                </div>
            </nav>
        </div>
    );
}

export default Navbar;