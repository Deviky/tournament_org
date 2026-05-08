import { useEffect, useRef } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { authApi } from "@/shared/api/authApi";
import { useAuthStore } from "@/app/store/authStore";
import { getErrorMessage } from "@/shared/api/client";
import "@/shared/styles/auth.css";

export default function ConfirmPage() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const setSession = useAuthStore((s) => s.setSession);
  const hasRun = useRef(false);

  useEffect(() => {
    if (hasRun.current) return;
    hasRun.current = true;

    const token = params.get("token");

    const confirm = async () => {
      try {
        const authData = await authApi.confirm(token);
        const { accessToken, refreshToken } = authData;
        setSession(accessToken, refreshToken);

        navigate("/", { replace: true });
      } catch (e) {
        console.error("CONFIRM ERROR:", getErrorMessage(e, "Ошибка подтверждения email"), e);
        navigate("/login", { replace: true });
      }
    };

    if (token) {
      confirm();
    } else {
      navigate("/login", { replace: true });
    }
  }, [navigate, params, setSession]);

  return <h2>Подтверждение email...</h2>;
}
