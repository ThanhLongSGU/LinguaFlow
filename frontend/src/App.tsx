import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import VisitorPage from "./pages/visitor/VisitorPage";
import { LoginPage } from "./pages/admin/LoginPage";
import { DashboardPage } from "./pages/admin/DashboardPage";
import { UserManagementPage } from "./pages/admin/UserManagementPage";
import { ProtectedRoute } from "./components/common/ProtectedRoute";
import { AdminLayout } from "./components/admin/AdminLayout";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Luồng Visitor */}
        <Route path="/visitor/:qrId" element={<VisitorPage />} />

        {/* Luồng Auth */}
        <Route path="/admin/login" element={<LoginPage />} />

        {/* Luồng Admin (Yêu cầu đăng nhập) */}
        <Route element={<ProtectedRoute />}>
          <Route element={<AdminLayout />}>
            <Route path="/admin/dashboard" element={<DashboardPage />} />
            <Route path="/admin/users" element={<UserManagementPage />} />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/visitor/1" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
