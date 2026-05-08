import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "@/shared/ui/Header";
import { tournamentApi } from "@/shared/api/tournamentApi";
import { gameApi } from "@/shared/api/gameApi";
import { participantApi } from "@/shared/api/participantApi";
import { getErrorMessage } from "@/shared/api/client";
import { useAuthStore } from "@/app/store/authStore";
import { tournamentStatusLabels, translateTournamentStatus } from "@/shared/lib/enumLabels";
import "@/styles/global.css";

const PAGE_SIZE = 15;

const cutText = (text, max = 200) =>
  text?.length > max ? `${text.slice(0, max)}...` : text;

export default function HomePage() {
  const [tournaments, setTournaments] = useState([]);
  const [games, setGames] = useState([]);
  const [organizations, setOrganizations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [gameFilter, setGameFilter] = useState("");
  const [organizerFilter, setOrganizerFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const navigate = useNavigate();
  const currentRole = useAuthStore((state) => state.currentRole);

  const formatDate = (dateStr) => {
    if (!dateStr) return "";
    return new Date(dateStr).toLocaleDateString("ru-RU", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    });
  };

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        setError("");

        const [tournamentsResult, gamesResult, organizationsResult] = await Promise.allSettled([
          tournamentApi.getAll(),
          gameApi.getAll(),
          participantApi.getAllOrganizators(),
        ]);

        if (tournamentsResult.status === "fulfilled") {
          setTournaments(tournamentsResult.value || []);
        } else {
          console.error("Tournaments load error:", tournamentsResult.reason);
          setTournaments([]);
          setError(getErrorMessage(tournamentsResult.reason, "Не удалось загрузить турниры"));
        }

        if (gamesResult.status === "fulfilled") {
          setGames(gamesResult.value || []);
        } else {
          console.error("Games load error:", gamesResult.reason);
          setGames([]);
        }

        if (organizationsResult.status === "fulfilled") {
          setOrganizations(organizationsResult.value || []);
        } else {
          console.error("Organizations load error:", organizationsResult.reason);
          setOrganizations([]);
        }
      } catch (err) {
        console.error("Unexpected load error:", err);
        setTournaments([]);
        setGames([]);
        setOrganizations([]);
        setError(getErrorMessage(err, "Не удалось загрузить главную страницу"));
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const gameMap = useMemo(() => {
    const map = new Map();
    games.forEach((g) => map.set(g.id, g.name));
    return map;
  }, [games]);

  const orgMap = useMemo(() => {
    const map = new Map();
    organizations.forEach((o) => map.set(o.id, o.organizerName));
    return map;
  }, [organizations]);

  const filtered = useMemo(() => {
    return tournaments.filter((t) => {
      const tStart = new Date(t.startAt);
      const tEnd = t.endAt ? new Date(t.endAt) : null;
      const from = dateFrom ? new Date(dateFrom) : null;
      const to = dateTo ? new Date(dateTo) : null;

      const matchesDate =
        (!from || (tEnd ? tEnd >= from : tStart >= from)) &&
        (!to || tStart <= to);

      const gameName = gameMap.get(t.gameId);
      const orgName = orgMap.get(t.organizerId);

      return (
        (!search || t.name?.toLowerCase().includes(search.toLowerCase())) &&
        (!gameFilter || gameName === gameFilter) &&
        (!organizerFilter || orgName?.toLowerCase().includes(organizerFilter.toLowerCase())) &&
        (!statusFilter || t.status === statusFilter) &&
        matchesDate
      );
    });
  }, [
    tournaments,
    gameMap,
    orgMap,
    search,
    gameFilter,
    organizerFilter,
    statusFilter,
    dateFrom,
    dateTo,
  ]);

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);

  const paginated = useMemo(() => {
    const start = (page - 1) * PAGE_SIZE;
    return filtered.slice(start, start + PAGE_SIZE);
  }, [filtered, page]);

  const updateFilter = (setter) => (value) => {
    setter(value);
    setPage(1);
  };

  return (
    <>
      <Header />

      <div className="container layout">
        <aside className="filters">
          <h3>Фильтры</h3>

          <input
            placeholder="Поиск турнира"
            value={search}
            onChange={(e) => updateFilter(setSearch)(e.target.value)}
          />

          <select
            value={gameFilter}
            onChange={(e) => updateFilter(setGameFilter)(e.target.value)}
          >
            <option value="">Игра</option>
            {games.map((g) => (
              <option key={g.id} value={g.name}>
                {g.name}
              </option>
            ))}
          </select>

          <input
            placeholder="Организатор"
            value={organizerFilter}
            onChange={(e) => updateFilter(setOrganizerFilter)(e.target.value)}
          />

          <select
            value={statusFilter}
            onChange={(e) => updateFilter(setStatusFilter)(e.target.value)}
          >
            <option value="">Статус</option>
            {Object.entries(tournamentStatusLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>

          <label>Дата начала</label>
          <input
            type="date"
            value={dateFrom}
            onChange={(e) => updateFilter(setDateFrom)(e.target.value)}
          />

          <label>Дата окончания</label>
          <input
            type="date"
            value={dateTo}
            onChange={(e) => updateFilter(setDateTo)(e.target.value)}
          />
        </aside>

        <main className="content">
          <div className="page-toolbar">
            <h1>Турниры</h1>
            {currentRole === "ORGANIZER" && (
              <button className="btn btn-card-primary" onClick={() => navigate("/tournaments/create")}>
                Создать турнир
              </button>
            )}
          </div>

          {loading ? (
            <p>Загрузка...</p>
          ) : error ? (
            <p>{error}</p>
          ) : (
            <>
              <div className="grid">
                {paginated.map((t) => {
                  const gameName = gameMap.get(t.gameId);
                  const orgName = orgMap.get(t.organizerId);

                  return (
                    <div key={t.id} className="card tournament-card">
                      <h3>{t.name}</h3>
                      <p>Игра: {gameName ?? "Неизвестная игра"}</p>
                      <p>Организатор: {orgName ?? "Неизвестный организатор"}</p>
                      <p className="tournament-date">
                        {formatDate(t.startAt)}
                        {t.endAt ? ` -> ${formatDate(t.endAt)}` : ""}
                      </p>
                      <p className="tournament-desc">{cutText(t.description, 200)}</p>

                      <div className="tournament-footer">
                        <span className="badge">{translateTournamentStatus(t.status)}</span>

                        <button
                          className="btn btn-card-primary"
                          onClick={() => navigate(`/tournaments/${t.id}`)}
                        >
                          Подробнее
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>

              <div className="pagination">
                <button onClick={() => setPage((p) => Math.max(p - 1, 1))}>
                  Назад
                </button>

                <span>
                  {page} / {totalPages || 1}
                </span>

                <button onClick={() => setPage((p) => Math.min(p + 1, totalPages || 1))}>
                  Вперёд
                </button>
              </div>
            </>
          )}
        </main>
      </div>
    </>
  );
}
