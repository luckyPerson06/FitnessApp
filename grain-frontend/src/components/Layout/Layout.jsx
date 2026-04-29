import PropTypes from 'prop-types';
import Navbar from './Navbar';

function Layout({ children }) {
    return (
        <div style={{
            minHeight: '100vh',
            position: 'relative',
            margin: 0,
            padding: 0,
        }}>
            <div style={{
                position: 'fixed',
                inset: 0,
                backgroundImage: 'url("/images/flowers/GeneralBack.png")',
                backgroundSize: 'cover',
                backgroundRepeat: 'no-repeat',
                backgroundPosition: 'center',
                zIndex: -1,
                pointerEvents: 'none',
            }} />
            <div style={{ position: 'relative', zIndex: 0 }}>
                <Navbar />
                <main>
                    {children}
                </main>
            </div>
        </div>
    );
}

Layout.propTypes = {
    children: PropTypes.node.isRequired,
};

export default Layout;
