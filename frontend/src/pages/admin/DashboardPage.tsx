import React, { useState } from "react";
import { QrCode, Calendar, MapPin } from "lucide-react";

export const DashboardPage: React.FC = () => {
  // Dữ liệu giả lập lịch sử quét QR
  const [scanLogs] = useState([
    {
      id: 1052,
      qrId: 1,
      siteName: "Landmark 81",
      scannedAt: "2026-10-19 10:30:00",
      device: "iOS",
    },
    {
      id: 1053,
      qrId: 2,
      siteName: "Dinh Độc Lập",
      scannedAt: "2026-10-19 11:15:22",
      device: "Android",
    },
    {
      id: 1054,
      qrId: 2,
      siteName: "Dinh Độc Lập",
      scannedAt: "2026-10-19 14:05:10",
      device: "Chrome",
    },
  ]);

  return (
    <div className="space-y-6">
      {/* Thẻ Thống kê */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center gap-4">
          <div className="w-12 h-12 bg-blue-100 text-blue-600 rounded-full flex justify-center items-center">
            <QrCode size={24} />
          </div>
          <div>
            <p className="text-sm text-gray-500 font-medium">
              Tổng lượt quét hôm nay
            </p>
            <h3 className="text-2xl font-bold text-gray-800">142</h3>
          </div>
        </div>
      </div>

      {/* Bảng chi tiết */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="p-5 border-b border-gray-100 flex justify-between items-center">
          <h3 className="text-lg font-semibold text-gray-800">
            Lịch sử quét mã QR gần đây
          </h3>
        </div>
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gray-50 text-gray-500 text-sm border-b border-gray-200">
              <th className="p-4 font-medium">Scan ID</th>
              <th className="p-4 font-medium">Mã QR</th>
              <th className="p-4 font-medium">Địa điểm</th>
              <th className="p-4 font-medium">Thời gian quét</th>
              <th className="p-4 font-medium">Thiết bị</th>
            </tr>
          </thead>
          <tbody>
            {scanLogs.map((log) => (
              <tr
                key={log.id}
                className="border-b border-gray-50 hover:bg-gray-50"
              >
                <td className="p-4 text-sm font-medium text-gray-900">
                  #{log.id}
                </td>
                <td className="p-4 text-sm text-gray-600">QR-{log.qrId}</td>
                <td className="p-4 text-sm text-gray-600 flex items-center gap-2">
                  <MapPin size={14} className="text-gray-400" /> {log.siteName}
                </td>
                <td className="p-4 text-sm text-gray-600">{log.scannedAt}</td>
                <td className="p-4 text-sm text-gray-600">{log.device}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
