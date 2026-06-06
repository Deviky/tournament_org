import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi } from "@/shared/api/authApi";
import { tournamentApi } from "@/shared/api/tournamentApi";
import { getErrorMessage } from "@/shared/api/client";
import { getRoleLabel, isStaffRole } from "@/shared/lib/authIdentity";
import {
  translateTournamentStatus,
  translateTournamentType,
} from "@/shared/lib/enumLabels";
import "@/shared/styles/auth.css";
import "@/shared/styles/tournament.css";
import "@/shared/styles/staff.css";

const STATUS_FILTERS = [
  { value: "ALL", label: "Все статусы" },
  { value: "ACTIVE", label: "Только активные" },
  { value: "BANNED", label: "Только заблокированные" },
];

const isActiveTournamentStatus = (status) =>
  !["FINISHED", "BANNED", "CANCEL"].includes(String(status || ""));

export default function StaffDashboardPage({ currentRole, currentSubject }) {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [busyTournamentId, setBusyTournamentId] = useState(null);
  const [creatingModerator, setCreatingModerator] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [tournaments, setTournaments] = useState([]);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ACTIVE");
  const [moderatorEmail, setModeratorEmail] = useState("");
  const [moderatorPassword, setModeratorPassword] = useState("");
  const [moderatorConfirmPassword, setModeratorConfirmPassword] = useState("");
  const [moderatorNickname, setModeratorNickname] = useState("");

  const isAdmin = currentRole === "ADMIN";

  const loadTournaments = async () => {
    const data = await tournamentApi.getAll();
    setTournaments(Array.isArray(data) ? data : []);
  };

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        setError("");
        await loadTournaments();
      } catch (e) {
        setError(getErrorMessage(e, "Не удалось загрузить панель модерации"));
      } finally {
        setLoading(false);
      }
    };

    load();
  }, []);

  const filteredTournaments = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase();

    return tournaments
      .filter((tournament) => {
        if (statusFilter === "ACTIVE") {
          return isActiveTournamentStatus(tournament.status);
        }

        if (statusFilter === "BANNED") {
          return tournament.status === "BANNED";
        }

        return true;
      })
      .filter((tournament) => {
        if (!normalizedSearch) {
          return true;
        }

        const haystack = [
          tournament.name,
          tournament.description,
          tournament.game?.name,
          tournament.organization?.organizerName,
        ]
          .filter(Boolean)
          .join(" ")
          .toLowerCase();

        return haystack.includes(normalizedSearch);
      })
      .sort((left, right) => Number(right.id) - Number(left.id));
  }, [search, statusFilter, tournaments]);

  const stats = useMemo(() => {
    const total = tournaments.length;
    const banned = tournaments.filter((item) => item.status === "BANNED").length;
    const active = tournaments.filter((item) => isActiveTournamentStatus(item.status)).length;

    return { total, banned, active };
  }, [tournaments]);

  const handleCreateModerator = async () => {
    if (!moderatorEmail.trim()) {
      setError("Укажите электронную почту модератора");
      return;
    }

    if (!moderatorPassword.trim()) {
      setError("Укажите пароль модератора");
      return;
    }

    if (moderatorPassword !== moderatorConfirmPassword) {
      setError("Пароли модератора не совпадают");
      return;
    }

    try {
      setCreatingModerator(true);
      setError("");
      setSuccess("");

      await authApi.createModerator({
        email: moderatorEmail.trim(),
        password: moderatorPassword,
        nickname: moderatorNickname.trim() || null,
      });

      setSuccess(
        "Модератор создан. На его почту отправлено письмо для подтверждения аккаунта."
      );
      setModeratorEmail("");
      setModeratorPassword("");
      setModeratorConfirmPassword("");
      setModeratorNickname("");
    } catch (e) {
      setError(getErrorMessage(e, "Не удалось создать модератора"));
    } finally {
      setCreatingModerator(false);
    }
  };

  const handleBanTournament = async (tournament) => {
    if (!tournament || tournament.status === "BANNED") {
      return;
    }

    const confirmed = window.confirm(
      `Заблокировать турнир «${tournament.name}»? Это действие остановит активность турнира и отменит проблемные матчи на стороне сервиса.`
    );

    if (!confirmed) {
      return;
    }

    try {
      setBusyTournamentId(tournament.id);
      setError("");
      setSuccess("");
      await tournamentApi.ban(tournament.id);
      await loadTournaments();
      setSuccess(`Турнир «${tournament.name}» был заблокирован.`);
    } catch (e) {
      setError(getErrorMessage(e, "Не удалось заблокировать турнир"));
    } finally {
      setBusyTournamentId(null);
    }
  };

  if (!isStaffRole(currentRole)) {
    return null;
  }

  return (
    <div className="staff-main">
      <section className="staff-hero">
        <h3 className="staff-hero-title">
          {isAdmin ? "Панель администратора" : "Панель модератора"}
        </h3>
        <p className="staff-hero-text">
          Здесь можно следить за турнирами, быстро открывать нужные карточки и блокировать
          проблемные события. Администратор дополнительно может регистрировать новых
          модераторов прямо из интерфейса.
        </p>
        <div className="staff-badges">
          <span className="staff-badge">Роль: {getRoleLabel(currentRole)}</span>
          <span className="staff-badge">
            Электронная почта: {currentSubject || "Не указана"}
          </span>
        </div>
      </section>

      {error && <div className="team-feedback error">{error}</div>}
      {success && <div className="team-feedback success">{success}</div>}

      <section className="section">
        <h3>Сводка</h3>
        <div className="staff-grid">
          <div className="staff-stat">
            <div className="staff-stat-label">Всего турниров</div>
            <div className="staff-stat-value">{stats.total}</div>
          </div>
          <div className="staff-stat">
            <div className="staff-stat-label">Активных</div>
            <div className="staff-stat-value">{stats.active}</div>
          </div>
          <div className="staff-stat">
            <div className="staff-stat-label">Заблокированных</div>
            <div className="staff-stat-value">{stats.banned}</div>
          </div>
        </div>
      </section>

      <div className="staff-layout">
        <div className="staff-main">
          <section className="section">
            <h3>Модерация турниров</h3>
            <div className="staff-toolbar">
              <input
                className="auth-input"
                placeholder="Поиск по турниру, игре или организатору"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
              />
              <select
                className="auth-input staff-filter"
                value={statusFilter}
                onChange={(event) => setStatusFilter(event.target.value)}
              >
                {STATUS_FILTERS.map((filter) => (
                  <option key={filter.value} value={filter.value}>
                    {filter.label}
                  </option>
                ))}
              </select>
            </div>

            <div className="staff-list">
              {loading ? (
                <div className="staff-empty">Загрузка турниров...</div>
              ) : filteredTournaments.length ? (
                filteredTournaments.map((tournament) => {
                  const isBanned = tournament.status === "BANNED";
                  const isFinished = tournament.status === "FINISHED";
                  const isBusy = busyTournamentId === tournament.id;

                  return (
                    <article key={tournament.id} className="staff-card">
                      <div className="staff-card-main">
                        <h4 className="staff-card-title">{tournament.name}</h4>
                        <div className="staff-card-meta">
                          <span>Игра: {tournament.game?.name || "Не указана"}</span>
                          <span>Тип: {translateTournamentType(tournament.type)}</span>
                          <span>Статус: {translateTournamentStatus(tournament.status)}</span>
                          <span>
                            Организатор:{" "}
                            {tournament.organization?.organizerName || "Не указан"}
                          </span>
                        </div>
                        <p className="staff-card-text">
                          {tournament.description || "Описание турнира пока не заполнено."}
                        </p>
                      </div>

                      <div className="staff-card-actions">
                        <button
                          className="btn btn-secondary"
                          onClick={() => navigate(`/tournaments/${tournament.id}`)}
                        >
                          Открыть турнир
                        </button>
                        <button
                          className="btn btn-danger"
                          onClick={() => handleBanTournament(tournament)}
                          disabled={isBusy || isBanned || isFinished}
                        >
                          {isBusy
                            ? "Блокируем..."
                            : isBanned
                              ? "Уже заблокирован"
                              : isFinished
                                ? "Завершенный турнир не блокируется"
                                : "Заблокировать турнир"}
                        </button>
                      </div>
                    </article>
                  );
                })
              ) : (
                <div className="staff-empty">По текущим фильтрам турниров не найдено.</div>
              )}
            </div>
          </section>
        </div>

        <aside className="staff-side">
          {isAdmin && (
            <section className="section">
              <h3>Зарегистрировать модератора</h3>
              <div className="staff-form">
                <input
                  className="auth-input"
                  placeholder="Электронная почта модератора"
                  value={moderatorEmail}
                  onChange={(event) => setModeratorEmail(event.target.value)}
                />
                <input
                  className="auth-input"
                  placeholder="Никнейм модератора"
                  value={moderatorNickname}
                  onChange={(event) => setModeratorNickname(event.target.value)}
                />
                <input
                  className="auth-input"
                  type="password"
                  placeholder="Пароль"
                  value={moderatorPassword}
                  onChange={(event) => setModeratorPassword(event.target.value)}
                />
                <input
                  className="auth-input"
                  type="password"
                  placeholder="Подтвердите пароль"
                  value={moderatorConfirmPassword}
                  onChange={(event) => setModeratorConfirmPassword(event.target.value)}
                />
                <button
                  className="auth-btn"
                  onClick={handleCreateModerator}
                  disabled={creatingModerator}
                >
                  {creatingModerator ? "Создаем..." : "Создать модератора"}
                </button>
              </div>
            </section>
          )}

          <section className="section">
            <h3>Что умеет эта панель</h3>
            <div className="manage-status-list">
              <div className="muted-text">
                {isAdmin
                  ? "Администратор может создавать модераторов и модерировать турниры."
                  : "Модератор может быстро открывать турниры и блокировать проблемные события."}
              </div>
              <div className="muted-text">
                При блокировке турнир получает статус «Заблокирован», а связанные действия
                обрабатываются на стороне бэкенда.
              </div>
              <div className="muted-text">
                Для детального просмотра можно открыть карточку турнира и перейти к его
                участникам, матчам и сетке.
              </div>
            </div>
          </section>
        </aside>
      </div>
    </div>
  );
}
