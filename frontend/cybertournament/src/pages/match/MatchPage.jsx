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

export default function MatchPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const currentRole = useAuthStore((state) => state.currentRole);
  const currentUserId = useAuthStore((state) => state.currentUserId);

  const [match, setMatch] = useState(null);
  const [tournament, setTournament] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        setError("");

        const matchData = await matchApi.getById(id);
        const tournamentData = await tournamentApi.getById(matchData.tournamentId);

        setMatch(matchData ?? null);
        setTournament(tournamentData ?? null);
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

  return (
    <>
      <Header />

      <div className="container tournament-page">
        <div className="tournament-header">
          <div>
            <div className="tournament-title">Матч #{match.id}</div>
            <div className="tournament-meta">
              <div>Турнир: {tournament.name}</div>
              <div>Статус: {translateMatchStatus(match.status)}</div>
              <div>Начало: {formatDateTime(match.startAt)}</div>
            </div>
          </div>

          <div className="tournament-header-actions">
            {isTournamentOrganizer && (
              <button
                className="btn btn-secondary"
                onClick={() => navigate(`/matches/${match.id}/manage`)}
              >
                Управление матчем
              </button>
            )}

            <button
              className="btn btn-secondary"
              onClick={() => navigate(`/tournaments/${tournament.id}`)}
            >
              К турниру
            </button>
          </div>
        </div>

        <div className="tournament-layout">
          <div style={{ display: "flex", flexDirection: "column", gap: 15 }}>
            <TournamentMatchesSection
              title="Информация о матче"
              matches={[match]}
              emptyText="Матч не найден."
            />
          </div>

          <div className="sidebar">
            <div className="section">
              <h3>Контекст</h3>
              <p>Турнир: {tournament.name}</p>
              <p>Организация: {tournament.organization?.organizerName || "Не указана"}</p>
              <p>Игра: {tournament.game?.name || "Не указана"}</p>
            </div>

            <div className="section">
              <h3>Навигация</h3>
              <button
                className="btn btn-secondary"
                onClick={() => navigate(`/tournaments/${tournament.id}`)}
              >
                Открыть турнир
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
