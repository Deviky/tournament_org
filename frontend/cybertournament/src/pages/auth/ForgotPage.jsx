import { useState } from "react";
import { authApi } from "@/shared/api/authApi";
import { getErrorMessage } from "@/shared/api/client";
import "@/shared/styles/auth.css";

export default function ForgotPage() {
  const [email, setEmail] = useState("");
  const [sent, setSent] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSend = async () => {
    try {
      setError("");
      setLoading(true);
      await authApi.forgot(email);
      setSent(true);
    } catch (e) {
      setError(getErrorMessage(e, "Ошибка отправки письма"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-title">Восстановление пароля</div>

        {error && <div className="auth-error">{error}</div>}

        {!sent ? (
          <>
            <p style={{ marginBottom: 12, color: "#8b949e", fontSize: 13 }}>
              Введите email, мы отправим инструкцию для сброса пароля
            </p>

            <input
              className="auth-input"
              placeholder="Email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />

            <button
              className="auth-btn"
              onClick={handleSend}
              disabled={loading || !email}
              style={{
                opacity: loading || !email ? 0.6 : 1,
                cursor: loading || !email ? "not-allowed" : "pointer",
              }}
            >
              {loading ? "Отправка..." : "Отправить инструкцию"}
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
            Инструкция отправлена на вашу почту
          </div>
        )}
      </div>
    </div>
  );
}
