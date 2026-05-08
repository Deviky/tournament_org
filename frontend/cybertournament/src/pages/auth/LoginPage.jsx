import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuthStore } from "@/app/store/authStore";
import { getErrorMessage } from "@/shared/api/client";
import "@/shared/styles/auth.css";

export default function LoginPage() {
  const login = useAuthStore((s) => s.login);
  const isAuth = useAuthStore((s) => s.isAuth);
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const redirectPath = searchParams.get("redirect") || "/";

  useEffect(() => {
    if (isAuth) navigate(redirectPath, { replace: true });
  }, [isAuth, navigate, redirectPath]);

  const handleLogin = async () => {
    try {
      setError("");
      await login({ email, password });
    } catch (e) {
      setError(getErrorMessage(e, "Неверный логин или пароль"));
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-title">Вход</div>

        {error && <div className="auth-error">{error}</div>}

        <input
          className="auth-input"
          placeholder="Электронная почта"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <input
          className="auth-input"
          type="password"
          placeholder="Пароль"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <button className="auth-btn" onClick={handleLogin}>
          Войти
        </button>

        <div className="auth-link" onClick={() => navigate("/forgot")}>
          Забыли пароль?
        </div>

        <div className="auth-link" onClick={() => navigate("/register")}>
          Нет аккаунта? Регистрация
        </div>
      </div>
    </div>
  );
}
