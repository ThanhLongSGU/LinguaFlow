import React, { useState } from "react";
import { UserPlus, Shield, User } from "lucide-react";

export const UserManagementPage: React.FC = () => {
  const [users] = useState([
    {
      id: 1,
      username: "admin",
      fullName: "Quản trị viên Hệ thống",
      role: "ADMIN",
      isActive: true,
    },
    {
      id: 2,
      username: "content_manager",
      fullName: "Quản lý Nội dung",
      role: "CONTENT_MANAGER",
      isActive: true,
    },
    {
      id: 3,
      username: "tester",
      fullName: "Nhân viên Test",
      role: "CONTENT_MANAGER",
      isActive: false,
    },
  ]);

  return (
    <div className="space-y-6">
      <div className="flex justify-end">
        <button className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center gap-2 font-medium transition-colors">
          <UserPlus size={18} /> Thêm tài khoản
        </button>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gray-50 text-gray-500 text-sm border-b border-gray-200">
              <th className="p-4 font-medium">ID</th>
              <th className="p-4 font-medium">Tài khoản</th>
              <th className="p-4 font-medium">Vai trò</th>
              <th className="p-4 font-medium">Trạng thái</th>
              <th className="p-4 font-medium">Hành động</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr
                key={user.id}
                className="border-b border-gray-50 hover:bg-gray-50"
              >
                <td className="p-4 text-sm text-gray-600">{user.id}</td>
                <td className="p-4">
                  <div className="font-medium text-gray-900">
                    {user.fullName}
                  </div>
                  <div className="text-sm text-gray-500">@{user.username}</div>
                </td>
                <td className="p-4">
                  <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                    <Shield size={12} /> {user.role}
                  </span>
                </td>
                <td className="p-4">
                  <span
                    className={`inline-block px-2.5 py-1 rounded-full text-xs font-medium ${user.isActive ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800"}`}
                  >
                    {user.isActive ? "Hoạt động" : "Đã khóa"}
                  </span>
                </td>
                <td className="p-4">
                  <button className="text-blue-600 hover:text-blue-800 text-sm font-medium">
                    Sửa
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
