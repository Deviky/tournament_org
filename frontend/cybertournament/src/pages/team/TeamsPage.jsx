import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import Header from "@/shared/ui/Header";
import { gameApi } from "@/shared/api/gameApi";
import { teamApi } from "@/shared/api/teamApi";
import { getErrorMessage } from "@/shared/api/client";
import { useAuthStore } from "@/app/store/authStore";
import { translateTeamStatus, translateTeamType } from "@/shared/lib/enumLabels";
import { getTeamCaptain } from "@/shared/lib/teamUtils";
import "@/shared/styles/team-pages.css";

export default function TeamsPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const isAuth = useAuthStore((state) => state.isAuth);
  const currentRole = useAuthStore((state) => state.currentRole);
  const currentUserId = useAuthStore((state) => state.currentUserId);

  const initialQuery = searchParams.get("q") || "";
  const initialInviteCode = searchParams.get("token") || "";

  const [query, setQuery] = useState(initialQuery);
  const [inviteCode, setInviteCode] = useState(initialInviteCode);
  const [teams, setTeams] = useState([]);
  const [games, setGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searching, setSearching] = useState(false);
  const [joiningByCode, setJoiningByCode] = useState(false);
  const [error, setError] = useState("");
  const [inviteError, setInviteError] = useState("");

  const normalizedQuery = query.trim().toLowerCase();

  const gameNames = useMemo(
    () => new Map((games || []).map((game) => [Number(game.id), game.name])),
    [games]
  );

  const filteredTeams = useMemo(() => {
    if (!normalizedQuery) {
      return teams;
    }

    return teams.filter((team) => {
      const captain = getTeamCaptain(team);
      const haystack = [
        team.name,
        gameNames.get(Number(team.gameId)),
        captain?.nickname,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return haystack.includes(normalizedQuery);
    });
  }, [gameNames, normalizedQuery, teams]);

  const updateSearchParams = (nextQuery, nextInviteCode) => {
    const params = new URLSearchParams();

    if (nextQuery) {
      params.set("q", nextQuery);
    }

    if (nextInviteCode) {
      params.set("token", nextInviteCode);
    }

    setSearchParams(params, { replace: true });
  };

  useEffect(() => {
    const loadInitialData = async () => {
      try {
        setLoading(true);
        setError("");

        const [gamesData, teamsData] = await Promise.all([
          gameApi.getAll(),
          teamApi.getAll(),
        ]);

        setGames(gamesData || []);
        setTeams(teamsData || []);
      } catch (err) {
        setError(getErrorMessage(err, "Не удалось загрузить список команд"));
      } finally {
        setLoading(false);
      }
    };

    loadInitialData();
  }, []);

  const handleSearchSubmit = (event) => {
    event.preventDefault();
    setSearching(true);
    setError("");
    updateSearchParams(query.trim(), inviteCode);
    setSearching(false);
  };

  const handleJoinByCode = async (event) => {
    event.preventDefault();

    if (!isAuth) {
      navigate(`/login?redirect=${encodeURIComponent(`/teams?token=${inviteCode.trim()}`)}`);
      return;
    }

    if (currentRole !== "PLAYER" || !currentUserId) {
      setInviteError("Вступить по коду приглашения может только игрок");
      return;
    }

    try {
      setJoiningByCode(true);
      setInviteError("");

      const joinedTeam = await teamApi.joinByToken(currentUserId, inviteCode.trim());
      updateSearchParams(query.trim(), "");
      navigate(`/teams/${joinedTeam.id}`);
    } catch (err) {
      setInviteError(getErrorMessage(err, "Не удалось вступить в команду по коду"));
    } finally {
      setJoiningByCode(false);
    }
  };

  return (
    <>
      <Header />

      <div className="container team-shell">
        <div className="team-hero">
          <div>
            <h1>Команды</h1>
            <p>
              Ищи подходящий состав, отправляй заявку в закрытую команду или
              вступай по коду приглашения от капитана.
            </p>
          </div>

          {currentRole === "PLAYER" && (
            <button className="btn btn-primary" onClick={() => navigate("/create-team")}>
              Создать команду
            </button>
          )}
        </div>

        <div className="team-page-grid">
          <div className="team-main-column">
            <form className="team-panel team-search-panel" onSubmit={handleSearchSubmit}>
              <div className="team-panel-header">
                <h2>Поиск команды</h2>
              </div>

              <div className="team-inline-form">
                <input
                  value={query}
                  onChange={(event) => setQuery(event.target.value)}
                  placeholder="Название команды, игра или ник капитана"
                />

                <button className="btn btn-secondary" type="submit" disabled={searching}>
                  {searching ? "Ищем..." : "Найти"}
                </button>
              </div>

              {error && <div className="team-feedback error">{error}</div>}
            </form>

            {loading ? (
              <div className="team-panel">Загрузка...</div>
            ) : filteredTeams.length === 0 ? (
              <div className="team-panel team-empty-state">
                <h2>Команды не найдены</h2>
                <p>
                  Попробуй другой запрос или создай свою команду, если подходящего
                  состава пока нет.
                </p>
              </div>
            ) : (
              <div className="team-results-grid">
                {filteredTeams.map((team) => {
                  const captain = getTeamCaptain(team);

                  return (
                    <div
                      key={team.id}
                      className="team-result-card"
                      onClick={() => navigate(`/teams/${team.id}`)}
                    >
                      <div className="team-result-top">
                        <div>
                          <h3>{team.name}</h3>
                          <div className="team-card-subtitle">
                            {gameNames.get(Number(team.gameId)) || `Игра #${team.gameId}`}
                          </div>
                        </div>

                        <div className="team-badge-row">
                          <span className="team-badge">{translateTeamType(team.type)}</span>
                          <span className="team-badge muted">{translateTeamStatus(team.status)}</span>
                        </div>
                      </div>

                      <div className="team-card-stats">
                        <span>Игроков: {team.players?.length || 0}</span>
                        <span>Капитан: {captain?.nickname || "Не указан"}</span>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          <aside className="team-sidebar-column">
            <form className="team-panel" onSubmit={handleJoinByCode}>
              <div className="team-panel-header">
                <h2>Вступить по коду</h2>
              </div>

              <p className="team-note">
                Если капитан прислал тебе код приглашения, вставь его сюда и
                сразу перейди в нужную команду.
              </p>

              <input
                value={inviteCode}
                onChange={(event) => {
                  const nextCode = event.target.value;
                  setInviteCode(nextCode);
                  updateSearchParams(query.trim(), nextCode.trim());
                }}
                placeholder="Код приглашения"
              />

              {inviteError && <div className="team-feedback error">{inviteError}</div>}

              <button
                className="btn btn-primary"
                type="submit"
                disabled={joiningByCode || !inviteCode.trim()}
              >
                {joiningByCode ? "Проверяем..." : "Вступить"}
              </button>
            </form>

            <div className="team-panel">
              <div className="team-panel-header">
                <h2>Как это работает</h2>
              </div>

              <ul className="team-info-list">
                <li>Открытые команды принимают игроков сразу.</li>
                <li>В закрытую команду сначала отправляется заявка капитану.</li>
                <li>Неактивные и удалённые команды не показываются в общем списке.</li>
                <li>По коду приглашения можно попасть в команду быстрее, если капитан его выдал.</li>
              </ul>
            </div>
          </aside>
        </div>
      </div>
    </>
  );
}
