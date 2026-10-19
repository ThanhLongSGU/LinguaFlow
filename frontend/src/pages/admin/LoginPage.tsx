import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../../services/authService";
import { User, Lock, ArrowRight, ShieldCheck } from "lucide-react";

export const LoginPage: React.FC = () => {
  const [username, setUsername] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [error, setError] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(false);
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      await login(username, password);
      // Điều hướng về trang Admin Dashboard sau khi đăng nhập thành công
      navigate("/admin/dashboard");
    } catch (err: any) {
      setError(
        err.response?.data?.message ||
          "Tài khoản hoặc mật khẩu không chính xác!",
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F7F4EE] flex items-center justify-center p-4">
      {/* Khối Card màu kem sáng */}
      <div className="bg-[#FFFDF9] border border-[#E8E2D5] rounded-3xl shadow-xl shadow-stone-200/60 p-8 sm:p-12 w-full max-w-md flex flex-col items-center">
        {/* Biểu tượng Logo trên Header */}
        <div className="w-16 h-16 bg-[#F0ECE1] text-[#655343] rounded-2xl flex items-center justify-center mb-4 shadow-inner border border-[#E2DBD0]">
          <ShieldCheck className="w-8 h-8" />
        </div>

        <h2 className="text-2xl font-bold text-[#3B322C] tracking-tight mb-1">
          Member Login
        </h2>
        <p className="text-xs text-[#8C7E72] mb-8 text-center font-medium">
          Hệ thống Quản trị LinguaFlow
        </p>

        {/* Thông báo lỗi nếu đăng nhập thất bại */}
        {error && (
          <div className="w-full bg-[#FDF2F2] border border-[#F5C6C6] text-[#A93226] text-xs px-4 py-2.5 rounded-xl mb-4 text-center font-medium">
            {error}
          </div>
        )}

        {/* Form Đăng nhập */}
        <form onSubmit={handleLogin} className="w-full space-y-4">
          {/* Input Email / Username */}
          <div className="space-y-1">
            <label className="text-xs font-semibold text-[#655343] ml-1">
              Tài khoản / Email
            </label>
            <div className="relative flex items-center bg-[#F3EFE6] rounded-xl border border-[#E2DBD0] focus-within:border-[#7A6855] focus-within:ring-2 focus-within:ring-[#7A6855]/20 transition">
              <User className="w-4 h-4 text-[#8C7E72] ml-3.5 mr-2 shrink-0" />
              <input
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Nhập tên đăng nhập..."
                className="w-full bg-transparent text-sm text-[#3B322C] placeholder-[#A09386] py-3 pr-4 outline-none font-medium"
              />
            </div>
          </div>

          {/* Input Password */}
          <div className="space-y-1">
            <label className="text-xs font-semibold text-[#655343] ml-1">
              Mật khẩu
            </label>
            <div className="relative flex items-center bg-[#F3EFE6] rounded-xl border border-[#E2DBD0] focus-within:border-[#7A6855] focus-within:ring-2 focus-within:ring-[#7A6855]/20 transition">
              <Lock className="w-4 h-4 text-[#8C7E72] ml-3.5 mr-2 shrink-0" />
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full bg-transparent text-sm text-[#3B322C] placeholder-[#A09386] py-3 pr-4 outline-none font-medium"
              />
            </div>
          </div>

          {/* Nút bấm LOGIN */}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-[#5C4B3C] hover:bg-[#4A3B2F] active:bg-[#382C22] text-[#F7F4EE] font-semibold py-3.5 rounded-xl shadow-md transition-all duration-200 text-sm mt-2 flex items-center justify-center gap-2 disabled:opacity-70"
          >
            {loading ? "Đang xác thực..." : "LOGIN"}
          </button>
        </form>

        {/* Liên kết điều hướng */}
        <div className="mt-8 flex flex-col items-center gap-2 text-xs">
          <a
            href="#forgot"
            onClick={(e) => e.preventDefault()}
            className="text-[#8C7E72] hover:text-[#5C4B3C] transition font-medium"
          >
            Forgot{" "}
            <span className="underline font-semibold text-[#655343]">
              Username / Password?
            </span>
          </a>

          <a
            href="#signup"
            onClick={(e) => e.preventDefault()}
            className="inline-flex items-center gap-1.5 text-[#5C4B3C] hover:text-[#3B322C] font-semibold transition mt-3"
          >
            Create your Account <ArrowRight className="w-3.5 h-3.5" />
          </a>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
