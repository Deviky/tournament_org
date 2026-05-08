import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "@/shared/ui/Header";
import BracketGraph from "@/shared/ui/BracketGraph";
import TeamsSection from "@/shared/ui/TeamsSection";
import TournamentMatchesSection from "@/shared/ui/TournamentMatchesSection";
import { tournamentApi } from "@/shared/api/tournamentApi";
import { matchApi } from "@/shared/api/matchApi";
import { teamApi } from "@/shared/api/teamApi";
import { getErrorMessage } from "@/shared/api/client";
import { useAuthStore } from "@/app/store/authStore";
import { getTeamCaptain } from "@/shared/lib/teamUtils";
import {
  translateTournamentStatus,
  translateTournamentType,
} from "@/shared/lib/enumLabels";
import "@/shared/styles/tournament.css";

const formatDate = (dateValue) =>
  dateValue
    ? new Date(dateValue).toLocaleDateString("ru-RU", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
      })
    : "Не указано";

export default function TournamentPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const currentRole = useAuthStore((s) => s.currentRole);
  const currentUserId = useAuthStore((s) => s.currentUserId);
  const isAuth = useAuthStore((s) => s.isAuth);

  const [tournament, setTournament] = useState(null);
  const [matches, setMatches] = useState([]);
  const [ownedTeams, setOwnedTeams] = useState([]);
  const [selectedTeamId, setSelectedTeamId] = useState("");
  const [inviteCode, setInviteCode] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [leavingTeamId, setLeavingTeamId] = useState(null);
  const [registrationError, setRegistrationError] = useState("");
  const [registrationSuccess, setRegistrationSuccess] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadTournament = async () => {
    const tournamentData = await tournamentApi.getById(id);

    let matchesData = tournamentData?.matches || [];
    try {
      matchesData = await matchApi.getByTournament(id);
    } catch (matchError) {
      console.warn("Falling back to tournament.matches due to match API error", matchError);
    }

    setTournament(tournamentData ?? null);
    setMatches(matchesData ?? []);

    if (currentRole === "PLAYER" && currentUserId) {
      const allTeams = await teamApi.getAll();
      const captainTeams = (allTeams || []).filter((team) => {
        const captain = getTeamCaptain(team);
        return captain && Number(captain.id) === Number(currentUserId);
      });

      setOwnedTeams(captainTeams);
      setSelectedTeamId((current) => current || String(captainTeams[0]?.id || ""));
    } else {
      setOwnedTeams([]);
      setSelectedTeamId("");
    }
  };

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        setError("");
        await loadTournament();
      } catch (err) {
        setError(getErrorMessage(err, "Не удалось загрузить турнир"));
        setTournament(null);
        setMatches([]);
        setOwnedTeams([]);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [currentRole, currentUserId, id]);

  const isTournamentOrganizer =
    currentRole === "ORGANIZER" &&
    currentUserId !== null &&
    tournament &&
    (Number(currentUserId) === Number(tournament.organizerId) ||
      Number(currentUserId) === Number(tournament.organization?.id));

  const tournamentTeamIds = useMemo(
    () => new Set((tournament?.teams || []).map((team) => Number(team.id))),
    [tournament]
  );

  const eligibleTeams = useMemo(
    () =>
      (ownedTeams || []).filter(
        (team) =>
          Number(team.gameId) === Number(tournament?.gameId) &&
          !tournamentTeamIds.has(Number(team.id))
      ),
    [ownedTeams, tournament?.gameId, tournamentTeamIds]
  );

  const joinedOwnedTeams = useMemo(
    () => (ownedTeams || []).filter((team) => tournamentTeamIds.has(Number(team.id))),
    [ownedTeams, tournamentTeamIds]
  );

  const selectedTeam = useMemo(
    () => eligibleTeams.find((team) => Number(team.id) === Number(selectedTeamId)) || null,
    [eligibleTeams, selectedTeamId]
  );

  useEffect(() => {
    if (!eligibleTeams.length) {
      setSelectedTeamId("");
      return;
    }

    if (!eligibleTeams.some((team) => Number(team.id) === Number(selectedTeamId))) {
      setSelectedTeamId(String(eligibleTeams[0].id));
    }
  }, [eligibleTeams, selectedTeamId]);

  const alreadyJoinedTeamNames = useMemo(
    () => joinedOwnedTeams.map((team) => team.name),
    [joinedOwnedTeams]
  );

  const canRegister =
    currentRole === "PLAYER" &&
    currentUserId &&
    tournament?.status === "REGISTRATION" &&
    eligibleTeams.length > 0;

  const runningMatches = useMemo(
    () => (matches || []).filter((match) => match?.status === "RUNNING"),
    [matches]
  );

  const handleRegisterTeam = async () => {
    if (!isAuth) {
      navigate(`/login?redirect=${encodeURIComponent(`/tournaments/${id}`)}`);
      return;
    }

    if (!selectedTeamId) {
      setRegistrationError("Выбери команду для участия");
      return;
    }

    try {
      setSubmitting(true);
      setRegistrationError("");
      setRegistrationSuccess("");

      await tournamentApi.registerTeam(
        currentUserId,
        id,
        selectedTeamId,
        inviteCode.trim() || undefined
      );

      await loadTournament();
      setInviteCode("");
      setRegistrationSuccess("Заявка отправлена");
    } catch (err) {
      setRegistrationError(getErrorMessage(err, "Не удалось отправить заявку"));
    } finally {
      setSubmitting(false);
    }
  };

  const handleLeaveTournament = async (team) => {
    if (!team || !currentUserId) {
      return;
    }

    try {
      setLeavingTeamId(team.id);
      setRegistrationError("");
      setRegistrationSuccess("");

      await tournamentApi.leaveTeam(currentUserId, id, team.id);
      await loadTournament();
      setRegistrationSuccess(`Команда «${team.name}» покинула турнир`);
    } catch (err) {
      setRegistrationError(getErrorMessage(err, "Не удалось вывести команду из турнира"));
    } finally {
      setLeavingTeamId(null);
    }
  };

  const renderLeaveTeamCards = () => {
    if (!joinedOwnedTeams.length) {
      return null;
    }

    return (
      <div style={{ marginTop: 16 }}>
        <p className="muted-text">
          Если планы изменились, капитан может вывести свою команду из турнира, пока регистрация ещё открыта.
        </p>

        <div
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 10,
            marginTop: 10,
          }}
        >
          {joinedOwnedTeams.map((team) => (
            <div
              key={team.id}
              className="card"
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                gap: 12,
                padding: 12,
              }}
            >
              <div>
                <strong>{team.name}</strong>
                <div className="muted-text">Этой командой управляешь ты как капитан</div>
              </div>

              <button
                className="btn btn-secondary"
                disabled={leavingTeamId === team.id}
                onClick={() => handleLeaveTournament(team)}
              >
                {leavingTeamId === team.id ? "Выходим..." : "Выйти из турнира"}
              </button>
            </div>
          ))}
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <>
        <Header />
        <div className="container tournament-page">Загрузка...</div>
      </>
    );
  }

  if (!tournament) {
    return (
      <>
        <Header />
        <div className="container tournament-page">{error || "Турнир не найден"}</div>
      </>
    );
  }

  return (
    <>
      <Header />

      <div className="container tournament-page">
        <div className="tournament-header">
          <div>
            <div className="tournament-title">{tournament.name}</div>

            <div className="tournament-meta">
              <div>Игра: {tournament.game?.name || "Не указана"}</div>
              <div>
                Организатор:{" "}
                <span
                  className="link"
                  onClick={() => navigate(`/organizations/${tournament.organization?.id}`)}
                >
                  {tournament.organization?.organizerName || "Не указан"}
                </span>
              </div>
              <div>Дата начала: {formatDate(tournament.startAt)}</div>
            </div>
          </div>
        </div>

        <div className="tournament-layout">
          <div style={{ display: "flex", flexDirection: "column", gap: 15 }}>
            <div className="section">
              <h3>Описание</h3>
              <p>{tournament.description || "Организатор пока не добавил описание турнира."}</p>
            </div>

            <div className="section">
              <h3>Турнирная сетка</h3>

              {!tournament.bracket ? (
                <p className="muted-text">Сетка появится после того, как организатор подготовит её для турнира.</p>
              ) : (
                <BracketGraph
                  bracketGroups={tournament.bracket.bracketGroups || []}
                  matches={matches}
                />
              )}
            </div>

            {runningMatches.length > 0 && (
              <TournamentMatchesSection
                title="Матчи, которые идут сейчас"
                matches={runningMatches}
                emptyText="Сейчас активных матчей нет."
              />
            )}

            <TeamsSection
              teams={tournament.teams || []}
              onTeamClick={(teamId) => navigate(`/teams/${teamId}`)}
            />
          </div>

          <div className="sidebar">
            <div className="section">
              <h3>О турнире</h3>
              <p>Тип: {translateTournamentType(tournament.type)}</p>
              <p>Статус: {translateTournamentStatus(tournament.status)}</p>
              <p>
                Команд: {tournament.minTeams} - {tournament.maxTeams}
              </p>
            </div>

            <div className="section">
              {isTournamentOrganizer ? (
                <button
                  className="btn btn-secondary"
                  onClick={() => navigate(`/tournaments/${tournament.id}/manage`)}
                >
                  Управление турниром
                </button>
              ) : (
                <>
                  <h3>Участие</h3>

                  {registrationError && <div className="team-feedback error">{registrationError}</div>}
                  {registrationSuccess && (
                    <div className="team-feedback success">{registrationSuccess}</div>
                  )}

                  {canRegister ? (
                    <div className="tournament-registration-box">
                      <label>
                        Команда
                        <select
                          value={selectedTeamId}
                          onChange={(event) => setSelectedTeamId(event.target.value)}
                        >
                          {eligibleTeams.map((team) => (
                            <option key={team.id} value={team.id}>
                              {team.name}
                            </option>
                          ))}
                        </select>
                      </label>

                      {tournament.type === "PRIVATE" && (
                        <label>
                          Код приглашения
                          <input
                            value={inviteCode}
                            onChange={(event) => setInviteCode(event.target.value)}
                            placeholder="Введи код, который прислал организатор"
                          />
                        </label>
                      )}

                      {selectedTeam && (
                        <p className="muted-text">
                          Заявка будет отправлена от команды «{selectedTeam.name}».
                        </p>
                      )}

                      <button
                        className="btn btn-primary"
                        disabled={submitting}
                        onClick={handleRegisterTeam}
                      >
                        {submitting ? "Отправляем..." : "Подать заявку"}
                      </button>

                      {renderLeaveTeamCards()}
                    </div>
                  ) : (
                    <div className="tournament-registration-box">
                      {!isAuth ? (
                        <button
                          className="btn btn-primary"
                          onClick={() =>
                            navigate(`/login?redirect=${encodeURIComponent(`/tournaments/${id}`)}`)
                          }
                        >
                          Войти для участия
                        </button>
                      ) : currentRole !== "PLAYER" ? (
                        <p className="muted-text">
                          Подавать заявку на турнир может только игрок, который является капитаном команды.
                        </p>
                      ) : tournament.status !== "REGISTRATION" ? (
                        <p className="muted-text">
                          Сейчас регистрация закрыта, поэтому новые заявки не принимаются.
                        </p>
                      ) : alreadyJoinedTeamNames.length ? (
                        <div>
                          <p className="muted-text">
                            Твои команды уже участвуют в турнире: {alreadyJoinedTeamNames.join(", ")}.
                          </p>
                          {renderLeaveTeamCards()}
                        </div>
                      ) : (
                        <p className="muted-text">
                          У тебя пока нет подходящей команды, где ты являешься капитаном.
                        </p>
                      )}
                    </div>
                  )}
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
