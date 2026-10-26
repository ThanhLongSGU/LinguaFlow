import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

// Import các Trang
import VisitorPage from './pages/visitor/VisitorPage';
import MapPage from './pages/visitor/MapPage'; // Component Bản đồ GPS

import { LoginPage } from './pages/admin/LoginPage';
import { ProtectedRoute } from './components/common/ProtectedRoute';
import { AdminLayout } from './components/admin/AdminLayout';
import { DashboardPage } from './pages/admin/DashboardPage';
import { UserManagementPage } from './pages/admin/UserManagementPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Route Khách tham quan chi tiết */}
        <Route path="/visitor/:qrId" element={<VisitorPage />} />

        {/* Route Bản đồ GPS (Trang bản đồ tương tác) */}
        <Route path="/map" element={<MapPage />} />

        {/* Route Admin */}
        <Route path="/admin/login" element={<LoginPage />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<AdminLayout />}>
            <Route path="/admin/dashboard" element={<DashboardPage />} />
            <Route path="/admin/users" element={<UserManagementPage />} />
          </Route>
        </Route>

        {/* Đổi tuyến đường mặc định sang Trang Bản đồ /map */}
        <Route path="*" element={<Navigate to="/map" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;