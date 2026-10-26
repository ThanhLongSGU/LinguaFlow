import React from "react";
import { Outlet, Link, useNavigate, useLocation } from "react-router-dom";
import { LayoutDashboard, Users, LogOut, QrCode } from "lucide-react";

export const AdminLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    navigate("/admin/login");
  };

  const isActive = (path: string) =>
    location.pathname === path
      ? "bg-blue-700 text-white"
      : "text-blue-100 hover:bg-blue-700";

  return (
    <div className="flex h-screen bg-gray-100">
      {/* Sidebar bên trái */}
      <aside className="w-64 bg-blue-900 text-white flex flex-col">
        <div className="p-6">
          <h2 className="text-2xl font-bold font-sans tracking-wider">
            LinguaFlow
          </h2>
          <p className="text-blue-300 text-sm mt-1">Admin Panel</p>
        </div>

        <nav className="flex-1 px-4 space-y-2 mt-4">
          <Link
            to="/admin/dashboard"
            className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${isActive("/admin/dashboard")}`}
          >
            <LayoutDashboard size={20} />
            <span className="font-medium">Tổng quan & Lịch sử</span>
          </Link>
          <Link
            to="/admin/users"
            className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${isActive("/admin/users")}`}
          >
            <Users size={20} />
            <span className="font-medium">Quản lý tài khoản</span>
          </Link>
        </nav>

        <div className="p-4">
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 w-full px-4 py-3 text-red-300 hover:bg-red-500/10 hover:text-red-400 rounded-lg transition-colors"
          >
            <LogOut size={20} />
            <span className="font-medium">Đăng xuất</span>
          </button>
        </div>
      </aside>

      <main className="flex-1 flex flex-col overflow-hidden">
        <header className="bg-white shadow-sm border-b h-16 flex items-center px-8">
          <h1 className="text-xl font-semibold text-gray-800">
            {location.pathname.includes("users")
              ? "Quản lý Người Dùng (UC08)"
              : "Tổng quan Lịch sử Quét QR (UC12)"}
          </h1>
        </header>
        <div className="flex-1 overflow-auto p-8">
          <Outlet />
        </div>
      </main>
    </div>
  );
};
