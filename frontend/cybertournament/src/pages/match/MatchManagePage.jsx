import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "@/shared/ui/Header";
import TournamentMatchesSection from "@/shared/ui/TournamentMatchesSection";
import { matchApi } from "@/shared/api/matchApi";
import { tournamentApi } from "@/shared/api/tournamentApi";
import { getErrorMessage } from "@/shared/api/client";
import { useAuthStore } from "@/app/store/authStore";
import { translateMatchStatus } from "@/shared/lib/enumLabels";
import "@/shared/styles/tournament.css";

const formatDateTime = (value) => {
  if (!value) {
    return "Не указано";
  }

  return new Date(value).toLocaleString("ru-RU", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};

export default function MatchManagePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const currentRole = useAuthStore((state) => state.currentRole);
  const currentUserId = useAuthStore((state) => state.currentUserId);

  const [match, setMatch] = useState(null);
  const [tournament, setTournament] = useState(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [actionError, setActionError] = useState("");
  const [actionSuccess, setActionSuccess] = useState("");

  const loadData = async () => {
    const matchData = await matchApi.getById(id);
    const tournamentData = await tournamentApi.getById(matchData.tournamentId);

    setMatch(matchData ?? null);
    setTournament(tournamentData ?? null);
  };

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        setError("");
        await loadData();
      } catch (err) {
        setError(getErrorMessage(err, "Не удалось загрузить матч"));
        setMatch(null);
        setTournament(null);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [id]);

  const isTournamentOrganizer = useMemo(() => {
    if (!match || !tournament || currentRole !== "ORGANIZER" || currentUserId === null) {
      return false;
    }

    return (
      Number(currentUserId) === Number(tournament.organizerId) ||
      Number(currentUserId) === Number(tournament.organization?.id)
    );
  }, [currentRole, currentUserId, match, tournament]);

  const runAction = async (executor, successMessage) => {
    try {
      setBusy(true);
      setActionError("");
      setActionSuccess("");
      await executor();
      await loadData();
      if (successMessage) {
        setActionSuccess(successMessage);
      }
    } catch (err) {
      setActionError(getErrorMessage(err, "Не удалось выполнить действие"));
    } finally {
      setBusy(false);
    }
  };

  const handleUpdateMatch = (payload) =>
    runAction(() => matchApi.update(currentUserId, payload), "Изменения сохранены");

  const handleStartMatch = (matchId) =>
    runAction(() => matchApi.start(currentUserId, matchId), "Матч начался");

  const handleFinishMatch = (payload) =>
    runAction(() => matchApi.finish(currentUserId, payload), "Матч завершён");

  const handleCancelMatch = (matchId) =>
    runAction(() => matchApi.cancel(currentUserId, matchId), "Матч отменён");

  if (loading) {
    return (
      <>
        <Header />
        <div className="container tournament-page">Загрузка...</div>
      </>
    );
  }

  if (!match || !tournament) {
    return (
      <>
        <Header />
        <div className="container tournament-page">{error || "Матч не найден"}</div>
      </>
    );
  }

  if (!isTournamentOrganizer) {
    return (
      <>
        <Header />
        <div className="container tournament-page">
          <div className="section">
            <h3>Доступ ограничен</h3>
            <p>Управлять матчем может только организатор турнира.</p>
            <button className="btn btn-primary" onClick={() => navigate(`/matches/${id}`)}>
              Вернуться к матчу
            </button>
          </div>
        </div>
      </>
    );
  }

  return (
    <>
      <Header />

      <div className="container tournament-page">
        <div className="tournament-header">
          <div>
            <div className="tournament-title">Управление матчем #{match.id}</div>
            <div className="tournament-meta">
              <div>Турнир: {tournament.name}</div>
              <div>Статус: {translateMatchStatus(match.status)}</div>
              <div>Начало: {formatDateTime(match.startAt)}</div>
            </div>
          </div>

          <div className="tournament-header-actions">
            <button className="btn btn-secondary" onClick={() => navigate(`/matches/${match.id}`)}>
              К странице матча
            </button>
            <button
              className="btn btn-secondary"
              onClick={() => navigate(`/tournaments/${tournament.id}/manage`)}
            >
              К управлению турниром
            </button>
          </div>
        </div>

        {(actionError || actionSuccess) && (
          <div className="section">
            {actionError && <div className="team-feedback error">{actionError}</div>}
            {actionSuccess && <div className="team-feedback success">{actionSuccess}</div>}
          </div>
        )}

        <div className="tournament-layout">
          <div style={{ display: "flex", flexDirection: "column", gap: 15 }}>
            <TournamentMatchesSection
              title="Настройки матча"
              matches={[match]}
              emptyText="Матч не найден."
              isOrganizer
              busy={busy}
              onUpdateMatch={handleUpdateMatch}
              onStartMatch={handleStartMatch}
              onFinishMatch={handleFinishMatch}
              onCancelMatch={handleCancelMatch}
            />
          </div>

          <div className="sidebar">
            <div className="section">
              <h3>Подсказка</h3>
              <p>Время окончания матча появляется автоматически после его завершения.</p>
              <p>Чтобы завершить матч, нужно выбрать одного победителя.</p>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
