import { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import Header from "@/shared/ui/Header";
import { useAuthStore } from "@/app/store/authStore";
import {
  getProfilePathByIdentity,
  getRoleLabel,
  hasDirectProfileByIdentity,
} from "@/shared/lib/authIdentity";
import "@/shared/styles/tournament.css";

export default function SelfProfilePage() {
  const navigate = useNavigate();
  const currentRole = useAuthStore((s) => s.currentRole);
  const currentUserId = useAuthStore((s) => s.currentUserId);
  const currentSubject = useAuthStore((s) => s.currentSubject);

  const directProfilePath = useMemo(
    () => getProfilePathByIdentity(currentRole, currentUserId),
    [currentRole, currentUserId]
  );

  const canOpenDirectProfile = hasDirectProfileByIdentity(currentRole, currentUserId);
  const roleLabel = getRoleLabel(currentRole);

  return (
    <>
      <Header />

      <div className="container tournament-page">
        <div className="section">
          <h3>Мой профиль</h3>

          {canOpenDirectProfile ? (
            <>
              <p>Профиль определён автоматически, можно сразу перейти к просмотру или редактированию.</p>
              <div className="team-actions-row" style={{ marginTop: 16 }}>
                <button className="btn btn-primary" onClick={() => navigate(directProfilePath)}>
                  Открыть профиль
                </button>
                <button className="btn btn-secondary" onClick={() => navigate("/profile/edit")}>
                  Редактировать профиль
                </button>
              </div>
            </>
          ) : (
            <>
              <p>
                Для роли <strong>{roleLabel}</strong> отдельный профиль участника не предусмотрен.
              </p>
              <p>
                Такой аккаунт может входить в систему и пользоваться служебными возможностями, но не связан со страницей игрока или организатора.
              </p>
              <div className="team-actions-row" style={{ marginTop: 16 }}>
                <button className="btn btn-secondary" onClick={() => navigate("/")}>
                  На главную
                </button>
              </div>
              <div className="tournament-meta" style={{ marginTop: 16 }}>
                <div>Роль: {roleLabel}</div>
                <div>Email: {currentSubject || "Не указан"}</div>
              </div>
            </>
          )}
        </div>
      </div>
    </>
  );
}
