import { useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { authApi } from "@/shared/api/authApi";
import { getErrorMessage } from "@/shared/api/client";
import "@/shared/styles/auth.css";

export default function ResetPage() {
  const [params] = useSearchParams();
  const navigate = useNavigate();

  const token = params.get("token");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [done, setDone] = useState(false);
  const [error, setError] = useState("");

  const validate = () => {
    if (!password.trim()) return "Введите пароль";
    if (!confirmPassword.trim()) return "Подтвердите пароль";
    if (password !== confirmPassword) return "Пароли не совпадают";
    if (!token) return "Неверная ссылка сброса";
    return null;
  };

  const handleReset = async () => {
    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      setError("");
      setLoading(true);

      await authApi.reset(token, {
        newPassword: password,
      });

      setDone(true);

      setTimeout(() => {
        navigate("/login", { replace: true });
      }, 2000);
    } catch (e) {
      setError(getErrorMessage(e, "Не удалось сбросить пароль"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-title">Сброс пароля</div>

        {!done ? (
          <>
            <p style={{ marginBottom: 12, color: "#8b949e", fontSize: 13 }}>
              Придумайте новый пароль и подтвердите его
            </p>

            {error && <div className="auth-error">{error}</div>}

            <input
              className="auth-input"
              type="password"
              placeholder="Новый пароль"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />

            <input
              className="auth-input"
              type="password"
              placeholder="Подтвердите пароль"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />

            <button
              className="auth-btn"
              onClick={handleReset}
              disabled={loading}
              style={{
                opacity: loading ? 0.6 : 1,
                cursor: loading ? "not-allowed" : "pointer",
              }}
            >
              {loading ? "Сохранение..." : "Сменить пароль"}
            </button>
          </>
        ) : (
          <div
            className="auth-error"
            style={{
              color: "#58a6ff",
              background: "rgba(88, 166, 255, 0.12)",
              border: "1px solid rgba(88, 166, 255, 0.35)",
            }}
          >
            Пароль успешно изменён. Перенаправляем на вход...
          </div>
        )}
      </div>
    </div>
  );
}
