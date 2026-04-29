import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthProvider';
import Layout from './components/Layout/Layout';
import Login from './pages/Login';
import Register from './pages/Register';
import Home from './pages/Home';
import Directions from './pages/Directions';
import Trainers from './pages/Trainers';
import Prices from './pages/Prices';
import Schedule from './pages/Schedule';
import AdminClients from './pages/AdminClients';

function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />

                    <Route path="/*" element={
                        <Layout>
                            <Routes>
                                <Route path="/" element={<Home />} />
                                <Route path="/directions" element={<Directions />} />
                                <Route path="/trainers" element={<Trainers />} />
                                <Route path="/prices" element={<Prices />} />
                                <Route path="/schedule" element={<Schedule />} />
                                <Route path="/admin/clients" element={<AdminClients />} />
                            </Routes>
                        </Layout>
                    } />
                </Routes>
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;