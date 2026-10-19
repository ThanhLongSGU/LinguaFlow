import React from "react";
import { Navigate, Outlet } from "react-router-dom";

export const ProtectedRoute: React.FC = () => {
  const token = localStorage.getItem("accessToken");

  return token ? <Outlet /> : <Navigate to="/admin/login" replace />;
};
