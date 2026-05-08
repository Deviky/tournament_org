import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "@/shared/ui/Header";
import { gameApi } from "@/shared/api/gameApi";
import { teamApi } from "@/shared/api/teamApi";
import { getErrorMessage } from "@/shared/api/client";
import { useAuthStore } from "@/app/store/authStore";
import {
  translateTeamPlayerStatus,
  translateTeamStatus,
  translateTeamType,
} from "@/shared/lib/enumLabels";
import {
  findTeamPlayer,
  getTeamCaptain,
  isTeamCaptain,
  isTeamMember,
  sortTeamPlayers,
} from "@/shared/lib/teamUtils";
import "@/shared/styles/team-pages.css";

const TEAM_STATUS_OPTIONS = [
  {
    value: "ACTIVE",
    title: "Активная",
    description: "Команда видна в общем списке и принимает новых игроков.",
  },
  {
    value: "INACTIVE",
    title: "Неактивная",
    description: "Команда скрывается из общего списка и временно не набирает состав.",
  },
];

export default function TeamPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const currentRole = useAuthStore((state) => state.currentRole);
  const currentUserId = useAuthStore((state) => state.currentUserId);

  const [team, setTeam] = useState(null);
  const [games, setGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionMessage, setActionMessage] = useState("");
  const [actionError, setActionError] = useState("");
  const [actionKey, setActionKey] = useState("");
  const [inviteToken, setInviteToken] = useState("");
  const [inviteCopied, setInviteCopied] = useState(false);
  const [pendingJoin, setPendingJoin] = useState(false);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [requestsLoading, setRequestsLoading] = useState(false);
  const [statusDraft, setStatusDraft] = useState("ACTIVE");

  useEffect(() => {
    const loadTeam = async () => {
      try {
        setLoading(true);
        setError("");
        setActionError("");
        setActionMessage("");
        setPendingJoin(false);

        const [teamData, gamesData] = await Promise.all([
          teamApi.getById(id),
          gameApi.getAll(),
        ]);

        setTeam(teamData ?? null);
        setGames(gamesData || []);
      } catch (err) {
        setError(getErrorMessage(err, "Не удалось загрузить команду"));
        setTeam(null);
      } finally {
        setLoading(false);
      }
    };

    loadTeam();
  }, [id]);

  useEffect(() => {
    if (team?.status) {
      setStatusDraft(team.status);
    }
  }, [team?.status]);

  const gameName = useMemo(
    () =>
      games.find((game) => Number(game.id) === Number(team?.gameId))?.name ||
      (team?.gameId ? `Игра #${team.gameId}` : "Не указана"),
    [games, team]
  );

  const sortedPlayers = useMemo(
    () => sortTeamPlayers(team?.players || []),
    [team]
  );

  const captain = useMemo(() => getTeamCaptain(team), [team]);
  const playerMode = currentRole === "PLAYER" && currentUserId !== null;
  const ownMember = findTeamPlayer(team, currentUserId);
  const ownCaptain = playerMode && isTeamCaptain(team, currentUserId);
  const ownMemberActive = playerMode && isTeamMember(team, currentUserId);
  const canManageRequests =
    ownCaptain && team?.type === "PRIVATE" && team?.status === "ACTIVE";
  const canGenerateInvite =
    ownCaptain && team?.type === "PRIVATE" && team?.status === "ACTIVE";
  const inviteLink = inviteToken
    ? `${window.location.origin}/teams?token=${encodeURIComponent(inviteToken)}`
    : "";

  useEffect(() => {
    if (!canManageRequests) {
      setPendingRequests([]);
      return;
    }

    const loadPendingRequests = async () => {
      try {
        setRequestsLoading(true);
        const requests = await teamApi.getPendingRequests(currentUserId, id);
        setPendingRequests(requests || []);
      } catch (err) {
        setActionError(getErrorMessage(err, "Не удалось загрузить заявки в команду"));
      } finally {
        setRequestsLoading(false);
      }
    };

    loadPendingRequests();
  }, [canManageRequests, currentUserId, id]);

  const runAction = async (nextActionKey, action, successMessage) => {
    try {
      setActionKey(nextActionKey);
      setActionError("");
      setActionMessage("");
      setInviteCopied(false);

      const nextTeam = await action();

      if (nextTeam) {
        setTeam(nextTeam);
      }

      if (successMessage) {
        setActionMessage(successMessage);
      }
    } catch (err) {
      setActionError(getErrorMessage(err, "Не удалось выполнить действие"));
    } finally {
      setActionKey("");
    }
  };

  const handleJoin = async () => {
    if (!playerMode) {
      setActionError("Вступать в команды могут только игроки");
      return;
    }

    const isPrivateTeam = team?.type === "PRIVATE";

    await runAction(
      "join",
      async () => {
        const nextTeam = await teamApi.join(currentUserId, team.id);

        if (isPrivateTeam && !findTeamPlayer(nextTeam, currentUserId)) {
          setPendingJoin(true);
        } else {
          setPendingJoin(false);
        }

        return nextTeam;
      },
      isPrivateTeam
        ? "Заявка отправлена капитану команды"
        : "Вы вступили в команду"
    );
  };

  const handleLeave = async () => {
    if (!playerMode) {
      return;
    }

    await runAction(
      "leave",
      async () => {
        const nextTeam = await teamApi.leave(currentUserId, team.id);
        setPendingJoin(false);
        setInviteToken("");
        return nextTeam;
      },
      "Состав команды обновлён"
    );
  };

  const handleRemovePlayer = async (player) => {
    if (!ownCaptain) {
      return;
    }

    if (!window.confirm(`Исключить игрока ${player.nickname} из команды?`)) {
      return;
    }

    await runAction(
      `remove-${player.id}`,
      () => teamApi.removePlayer(currentUserId, team.id, player.id),
      "Игрок исключён из команды"
    );
  };

  const handleTransferCaptain = async (player) => {
    if (!ownCaptain) {
      return;
    }

    if (!window.confirm(`Передать капитанство игроку ${player.nickname}?`)) {
      return;
    }

    await runAction(
      `captain-${player.id}`,
      () => teamApi.transferCaptain(currentUserId, team.id, player.id),
      "Капитан команды обновлён"
    );
  };

  const handleGenerateInvite = async () => {
    if (!canGenerateInvite) {
      return;
    }

    await runAction(
      "invite",
      async () => {
        const token = await teamApi.generateInvite(currentUserId, team.id);
        setInviteToken(token);
        return null;
      },
      "Ссылка приглашения готова"
    );
  };

  const handleCopyInvite = async () => {
    if (!inviteLink) {
      return;
    }

    try {
      await navigator.clipboard.writeText(inviteLink);
      setInviteCopied(true);
      setActionMessage("Ссылка приглашения скопирована");
    } catch {
      setActionError("Не удалось скопировать ссылку");
    }
  };

  const handleRequestDecision = async (player, approve) => {
    if (!canManageRequests) {
      return;
    }

    await runAction(
      `${approve ? "approve" : "reject"}-${player.id}`,
      async () => {
        const nextTeam = await teamApi.handleRequest(
          currentUserId,
          team.id,
          player.id,
          approve
        );
        const nextRequests = await teamApi.getPendingRequests(currentUserId, team.id);
        setPendingRequests(nextRequests || []);
        return nextTeam;
      },
      approve ? "Игрок принят в команду" : "Заявка отклонена"
    );
  };

  const handleSaveStatus = async () => {
    if (!ownCaptain || statusDraft === team?.status) {
      return;
    }

    const confirmText =
      statusDraft === "INACTIVE"
        ? "Сделать команду неактивной и скрыть её из общего списка?"
        : "Вернуть команду в активные?";

    if (!window.confirm(confirmText)) {
      return;
    }

    await runAction(
      "status",
      () => teamApi.updateStatus(currentUserId, team.id, statusDraft),
      statusDraft === "INACTIVE"
        ? "Команда скрыта из общего списка"
        : "Команда снова отображается в списке активных"
    );
  };

  const handleDeleteTeam = async () => {
    if (!ownCaptain) {
      return;
    }

    if (
      !window.confirm(
        "Пометить команду как удалённую? Она исчезнет из списка активных команд."
      )
    ) {
      return;
    }

    await runAction(
      "delete-team",
      () => teamApi.updateStatus(currentUserId, team.id, "DELETED"),
      "Команда помечена как удалённая"
    );
  };

  const handleRestoreTeam = async () => {
    if (!ownCaptain) {
      return;
    }

    await runAction(
      "restore-team",
      () => teamApi.updateStatus(currentUserId, team.id, "ACTIVE"),
      "Команда снова активна"
    );
  };

  if (loading) {
    return (
      <>
        <Header />
        <div className="container team-shell">
          <div className="team-panel">Загрузка...</div>
        </div>
      </>
    );
  }

  if (!team) {
    return (
      <>
        <Header />
        <div className="container team-shell">
          <div className="team-panel">{error || "Команда не найдена"}</div>
        </div>
      </>
    );
  }

  return (
    <>
      <Header />

      <div className="container team-shell">
        <div className="team-hero">
          <div>
            <div className="team-badge-row hero">
              <span className="team-badge">{translateTeamType(team.type)}</span>
              <span className="team-badge muted">{translateTeamStatus(team.status)}</span>
            </div>

            <h1>{team.name}</h1>
            <p>{gameName}</p>
          </div>

          <div className="team-actions-row">
            <button className="btn btn-secondary" onClick={() => navigate("/teams")}>
              К списку команд
            </button>

            {canGenerateInvite && (
              <button
                className="btn btn-primary"
                onClick={handleGenerateInvite}
                disabled={actionKey === "invite"}
              >
                {actionKey === "invite" ? "Готовим..." : "Ссылка приглашения"}
              </button>
            )}

            {canGenerateInvite && inviteLink && (
              <button className="btn btn-secondary" onClick={handleCopyInvite}>
                {inviteCopied ? "Скопировано" : "Скопировать ссылку"}
              </button>
            )}

            {playerMode && !ownMemberActive && !pendingJoin && team.status === "ACTIVE" && (
              <button className="btn btn-primary" onClick={handleJoin} disabled={actionKey === "join"}>
                {actionKey === "join"
                  ? "Отправляем..."
                  : team.type === "PRIVATE"
                    ? "Подать заявку"
                    : "Вступить"}
              </button>
            )}

            {playerMode && (ownMemberActive || pendingJoin) && (
              <button className="btn btn-secondary" onClick={handleLeave} disabled={actionKey === "leave"}>
                {actionKey === "leave"
                  ? "Обновляем..."
                  : pendingJoin
                    ? "Отменить ожидание"
                    : "Покинуть команду"}
              </button>
            )}
          </div>
        </div>

        {(actionError || actionMessage || pendingJoin) && (
          <div className="team-panel">
            {actionError && <div className="team-feedback error">{actionError}</div>}
            {!actionError && actionMessage && (
              <div className="team-feedback success">{actionMessage}</div>
            )}
            {!actionError && pendingJoin && (
              <div className="team-feedback info">
                Заявка уже отправлена. После одобрения капитаном ты появишься в составе
                команды.
              </div>
            )}
          </div>
        )}

        {ownCaptain && inviteLink && (
          <div className="team-panel">
            <div className="team-panel-header">
              <h2>Ссылка приглашения</h2>
            </div>

            <div className="team-token-box">
              <label>
                Ссылка
                <input value={inviteLink} readOnly />
              </label>

              <label>
                Код
                <input value={inviteToken} readOnly />
              </label>
            </div>
          </div>
        )}

        <div className="team-page-grid">
          <div className="team-main-column">
            {canManageRequests && (
              <div className="team-panel">
                <div className="team-panel-header">
                  <h2>Заявки в команду</h2>
                  <span>{pendingRequests.length}</span>
                </div>

                {requestsLoading ? (
                  <div>Загрузка...</div>
                ) : pendingRequests.length === 0 ? (
                  <div className="team-note">Новых заявок пока нет.</div>
                ) : (
                  <div className="team-members-list">
                    {pendingRequests.map((player) => {
                      const approveKey = `approve-${player.id}`;
                      const rejectKey = `reject-${player.id}`;
                      const isBusy = actionKey === approveKey || actionKey === rejectKey;

                      return (
                        <div key={player.id} className="team-member-card pending-request-card">
                          <div
                            className="team-member-main"
                            onClick={() => navigate(`/players/${player.id}`)}
                          >
                            <div className="team-member-avatar">
                              {(player.nickname || "?").slice(0, 2).toUpperCase()}
                            </div>

                            <div>
                              <div className="team-member-name">{player.nickname}</div>
                              <div className="team-member-meta">
                                {translateTeamPlayerStatus(player.status)}
                              </div>
                            </div>
                          </div>

                          <div className="team-member-actions">
                            <button
                              className="btn btn-primary"
                              onClick={() => handleRequestDecision(player, true)}
                              disabled={isBusy}
                            >
                              {actionKey === approveKey ? "Принимаем..." : "Принять"}
                            </button>

                            <button
                              className="btn btn-secondary"
                              onClick={() => handleRequestDecision(player, false)}
                              disabled={isBusy}
                            >
                              {actionKey === rejectKey ? "Отклоняем..." : "Отклонить"}
                            </button>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            )}

            <div className="team-panel">
              <div className="team-panel-header">
                <h2>Состав</h2>
                <span>{sortedPlayers.length} игроков</span>
              </div>

              <div className="team-members-list">
                {sortedPlayers.map((player) => {
                  const isSelf = Number(player.id) === Number(currentUserId);
                  const isBusy =
                    actionKey === `remove-${player.id}` || actionKey === `captain-${player.id}`;

                  return (
                    <div key={player.id} className="team-member-card">
                      <div
                        className="team-member-main"
                        onClick={() => navigate(`/players/${player.id}`)}
                      >
                        <div className="team-member-avatar">
                          {(player.nickname || "?").slice(0, 2).toUpperCase()}
                        </div>

                        <div>
                          <div className="team-member-name">
                            {player.nickname}
                            {player.isCaptain && <span className="team-member-crown">Капитан</span>}
                            {isSelf && <span className="team-member-self">Это вы</span>}
                          </div>
                          <div className="team-member-meta">
                            {translateTeamPlayerStatus(player.status)}
                          </div>
                        </div>
                      </div>

                      {ownCaptain && !player.isCaptain && (
                        <div className="team-member-actions">
                          <button
                            className="btn btn-secondary"
                            onClick={() => handleTransferCaptain(player)}
                            disabled={isBusy}
                          >
                            {actionKey === `captain-${player.id}`
                              ? "Передаём..."
                              : "Сделать капитаном"}
                          </button>

                          <button
                            className="btn btn-secondary"
                            onClick={() => handleRemovePlayer(player)}
                            disabled={isBusy}
                          >
                            {actionKey === `remove-${player.id}` ? "Исключаем..." : "Исключить"}
                          </button>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          <aside className="team-sidebar-column">
            <div className="team-panel">
              <div className="team-panel-header">
                <h2>Информация</h2>
              </div>

              <div className="team-info-grid">
                <div>
                  <span>Игра</span>
                  <strong>{gameName}</strong>
                </div>
                <div>
                  <span>Тип команды</span>
                  <strong>{translateTeamType(team.type)}</strong>
                </div>
                <div>
                  <span>Статус</span>
                  <strong>{translateTeamStatus(team.status)}</strong>
                </div>
                <div>
                  <span>Капитан</span>
                  <strong>{captain?.nickname || "Не указан"}</strong>
                </div>
              </div>
            </div>

            <div className="team-panel">
              <div className="team-panel-header">
                <h2>Управление</h2>
              </div>

              {!playerMode ? (
                <p className="team-note">
                  Управлять командой может только авторизованный игрок.
                </p>
              ) : ownCaptain ? (
                <div className="team-status-manager">
                  <p className="team-note">
                    Капитан может временно скрыть команду из общего списка, снова открыть
                    набор игроков или пометить команду как удалённую.
                  </p>

                  {team.status !== "DELETED" ? (
                    <>
                      <div className="team-status-options">
                        {TEAM_STATUS_OPTIONS.map((option) => (
                          <label
                            key={option.value}
                            className={`team-status-option ${
                              statusDraft === option.value ? "active" : ""
                            }`}
                          >
                            <input
                              type="radio"
                              name="team-status"
                              value={option.value}
                              checked={statusDraft === option.value}
                              onChange={(event) => setStatusDraft(event.target.value)}
                            />
                            <strong>{option.title}</strong>
                            <span>{option.description}</span>
                          </label>
                        ))}
                      </div>

                      <div className="team-member-actions">
                        <button
                          className="btn btn-primary"
                          onClick={handleSaveStatus}
                          disabled={actionKey === "status" || statusDraft === team.status}
                        >
                          {actionKey === "status" ? "Сохраняем..." : "Сохранить статус"}
                        </button>

                        <button
                          className="btn btn-secondary"
                          onClick={handleDeleteTeam}
                          disabled={actionKey === "delete-team"}
                        >
                          {actionKey === "delete-team"
                            ? "Обновляем..."
                            : "Пометить как удалённую"}
                        </button>
                      </div>
                    </>
                  ) : (
                    <div className="team-feedback info">
                      Команда уже помечена как удалённая и скрыта из общего списка.
                      <div className="team-member-actions top-gap">
                        <button
                          className="btn btn-primary"
                          onClick={handleRestoreTeam}
                          disabled={actionKey === "restore-team"}
                        >
                          {actionKey === "restore-team" ? "Возвращаем..." : "Вернуть в активные"}
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              ) : ownMember ? (
                <p className="team-note">
                  Ты состоишь в команде и можешь покинуть её в любой момент.
                </p>
              ) : team.status !== "ACTIVE" ? (
                <p className="team-note">
                  Сейчас команда скрыта из общего набора и не принимает новых игроков.
                </p>
              ) : (
                <p className="team-note">
                  {team.type === "PRIVATE"
                    ? "Для закрытой команды сначала отправляется заявка капитану."
                    : "В открытую команду можно вступить сразу, если состав подходит под правила игры."}
                </p>
              )}
            </div>
          </aside>
        </div>
      </div>
    </>
  );
}
