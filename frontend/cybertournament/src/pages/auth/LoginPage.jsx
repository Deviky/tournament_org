import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuthStore } from "@/app/store/authStore";
import { authApi } from "@/shared/api/authApi";
import { getErrorMessage } from "@/shared/api/client";
import "@/shared/styles/auth.css";

const RESEND_COOLDOWN_SECONDS = 60;

const isUnconfirmedEmailError = (message) =>
  String(message || "").toLowerCase().includes("подтверж");

export default function LoginPage() {
  const login = useAuthStore((s) => s.login);
  const isAuth = useAuthStore((s) => s.isAuth);
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");
  const [loginLoading, setLoginLoading] = useState(false);
  const [resendLoading, setResendLoading] = useState(false);
  const [resendCooldown, setResendCooldown] = useState(0);
  const [canResendConfirmation, setCanResendConfirmation] = useState(false);
  const redirectPath = searchParams.get("redirect") || "/";

  useEffect(() => {
    if (isAuth) {
      navigate(redirectPath, { replace: true });
    }
  }, [isAuth, navigate, redirectPath]);

  useEffect(() => {
    if (resendCooldown <= 0) {
      return undefined;
    }

    const timer = window.setInterval(() => {
      setResendCooldown((prev) => (prev > 1 ? prev - 1 : 0));
    }, 1000);

    return () => window.clearInterval(timer);
  }, [resendCooldown]);

  const handleLogin = async () => {
    try {
      setLoginLoading(true);
      setError("");
      setInfo("");
      setCanResendConfirmation(false);
      await login({ email, password });
    } catch (e) {
      const message = getErrorMessage(e, "Неверный логин или пароль");
      setError(message);
      setCanResendConfirmation(isUnconfirmedEmailError(message) && !!email.trim());
    } finally {
      setLoginLoading(false);
    }
  };

  const handleResendConfirmation = async () => {
    if (!email.trim() || resendCooldown > 0) {
      return;
    }

    try {
      setResendLoading(true);
      setError("");
      setInfo("");
      await authApi.resend(email.trim());
      setInfo("Письмо с подтверждением отправлено повторно. Проверьте вашу почту.");
      setResendCooldown(RESEND_COOLDOWN_SECONDS);
    } catch (e) {
      setError(getErrorMessage(e, "Не удалось отправить письмо повторно"));
    } finally {
      setResendLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-title">Вход</div>

        {error && <div className="auth-error">{error}</div>}
        {info && <div className="auth-info">{info}</div>}

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

        <button className="auth-btn" onClick={handleLogin} disabled={loginLoading}>
          {loginLoading ? "Входим..." : "Войти"}
        </button>

        {canResendConfirmation && (
          <div className="auth-resend-panel">
            <div className="auth-hint">
              Почта еще не подтверждена. Можно отправить письмо с подтверждением повторно.
            </div>
            <button
              className="btn btn-secondary"
              onClick={handleResendConfirmation}
              disabled={resendLoading || resendCooldown > 0}
            >
              {resendLoading
                ? "Отправляем..."
                : resendCooldown > 0
                  ? `Отправить повторно через ${resendCooldown} c`
                  : "Отправить подтверждение"}
            </button>
          </div>
        )}

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
