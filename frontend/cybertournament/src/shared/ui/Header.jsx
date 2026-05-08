import { useNavigate } from "react-router-dom";
import { useAuthStore } from "@/app/store/authStore";
import { getProfilePathByIdentity } from "@/shared/lib/authIdentity";
import "@/styles/global.css";

export default function Header() {
  const navigate = useNavigate();

  const isAuth = useAuthStore((s) => s.isAuth);
  const logout = useAuthStore((s) => s.logout);
  const currentRole = useAuthStore((s) => s.currentRole);
  const currentUserId = useAuthStore((s) => s.currentUserId);
  const canCreateTeam = currentRole === "PLAYER";

  const handleProfileNavigate = () => {
    navigate(getProfilePathByIdentity(currentRole, currentUserId));
  };

  return (
    <header className="header">
      <div className="logo" onClick={() => navigate("/")}>
        CyberTournamentZ
      </div>

      <nav className="nav">
        {isAuth ? (
          <>
            {canCreateTeam && (
              <button className="btn btn-secondary" onClick={() => navigate("/create-team")}>
                Создать команду
              </button>
            )}

            <button className="btn btn-secondary" onClick={() => navigate("/teams")}>
              Найти команду
            </button>

            <button className="btn btn-secondary" onClick={() => navigate("/")}>
              Турниры
            </button>

            <div className="profile" title="Мой профиль" onClick={handleProfileNavigate}>
              👤
            </div>

            <button className="btn btn-primary" onClick={logout}>
              Выйти
            </button>
          </>
        ) : (
          <>
            <button className="btn btn-secondary" onClick={() => navigate("/login")}>
              Войти
            </button>

            <button className="btn btn-primary" onClick={() => navigate("/register")}>
              Регистрация
            </button>
          </>
        )}
      </nav>
    </header>
  );
}
