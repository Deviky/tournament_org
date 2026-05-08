import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "@/shared/ui/Header";
import BracketGraph from "@/shared/ui/BracketGraph";
import TournamentEditorForm from "@/shared/ui/TournamentEditorForm";
import { tournamentApi } from "@/shared/api/tournamentApi";
import { gameApi } from "@/shared/api/gameApi";
import { matchApi } from "@/shared/api/matchApi";
import { getErrorMessage } from "@/shared/api/client";
import { useAuthStore } from "@/app/store/authStore";
import {
  buildAlgorithmParamsPayload,
  buildTournamentPayload,
  canEditTournament,
  canManageTournamentRequests,
  createBracketPreviewMatches,
  getAlgorithmConfigEntries,
  getAlgorithmDefaultValues,
  sortTournamentEntries,
  toDateTimeLocalValue,
} from "@/shared/lib/tournamentUtils";
import {
  translateTournamentStatus,
  translateTournamentTeamStatus,
  translateTournamentType,
} from "@/shared/lib/enumLabels";
import "@/shared/styles/tournament.css";

const createFormFromTournament = (tournament) => ({
  name: tournament?.name || "",
  gameId: tournament?.gameId ? String(tournament.gameId) : "",
  description: tournament?.description || "",
  minTeams: tournament?.minTeams ? String(tournament.minTeams) : "2",
  maxTeams: tournament?.maxTeams ? String(tournament.maxTeams) : "8",
  type: tournament?.type || "PUBLIC",
  startAt: toDateTimeLocalValue(tournament?.startAt),
  endAt: toDateTimeLocalValue(tournament?.endAt),
});

const cloneBracket = (bracket) => JSON.parse(JSON.stringify(bracket));

const getRegisteredTeams = (entries) =>
  (entries || [])
    .filter((entry) => entry?.status === "REGISTERED" && entry?.teamId)
    .map((entry) => ({
      id: Number(entry.teamId),
      name: entry?.team?.name || `Команда #${entry.teamId}`,
    }));

const getSeedSlots = (bracket) => {
  const slots = [];

  (bracket?.bracketGroups || []).forEach((group, groupIndex) => {
    (group?.matches || []).forEach((match, matchIndex) => {
      (match?.slots || []).forEach((slot, slotIndex) => {
        if (slot?.refMatchId) {
          return;
        }

        slots.push({
          key: `${groupIndex}-${matchIndex}-${slotIndex}`,
          groupIndex,
          matchIndex,
          slotIndex,
          matchId: match.matchId,
          currentTeamId: slot?.teamId ? Number(slot.teamId) : null,
        });
      });
    });
  });

  return slots;
};

const updateBracketSeedSlot = (bracket, targetKey, nextTeamId) => {
  const nextBracket = cloneBracket(bracket);
  const [groupIndex, matchIndex, slotIndex] = targetKey.split("-").map(Number);
  const slot =
    nextBracket?.bracketGroups?.[groupIndex]?.matches?.[matchIndex]?.slots?.[slotIndex];

  if (!slot) {
    return nextBracket;
  }

  slot.teamId = nextTeamId ? Number(nextTeamId) : null;
  return nextBracket;
};

const updateBracketGroupName = (bracket, groupIndex, nextName) => {
  const nextBracket = cloneBracket(bracket);
  const group = nextBracket?.bracketGroups?.[groupIndex];

  if (!group) {
    return nextBracket;
  }

  group.name = nextName;
  return nextBracket;
};

const hasDuplicateSeedTeams = (bracket) => {
  const seen = new Set();

  return getSeedSlots(bracket).some((slotInfo) => {
    if (!slotInfo.currentTeamId) {
      return false;
    }

    if (seen.has(slotInfo.currentTeamId)) {
      return true;
    }

    seen.add(slotInfo.currentTeamId);
    return false;
  });
};

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

const GROUP_NAME_LABEL = "Название группы";
const GROUP_NAME_PLACEHOLDER = "Группа";
const GROUP_NAME_HELP =
  "Имя можно задать для каждой группы или этапа сетки, чтобы их было легче различать.";
const ALGORITHM_LABEL = "Алгоритм";
const BOOLEAN_ON_LABEL = "Включено";
const BOOLEAN_OFF_LABEL = "Выключено";

export default function TournamentManagePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const currentRole = useAuthStore((state) => state.currentRole);
  const currentUserId = useAuthStore((state) => state.currentUserId);

  const [tournament, setTournament] = useState(null);
  const [matches, setMatches] = useState([]);
  const [games, setGames] = useState([]);
  const [entries, setEntries] = useState([]);
  const [form, setForm] = useState(createFormFromTournament(null));
  const [algorithms, setAlgorithms] = useState({});
  const [selectedAlgorithm, setSelectedAlgorithm] = useState("");
  const [algorithmValues, setAlgorithmValues] = useState({});
  const [draftBracket, setDraftBracket] = useState(null);
  const [inviteLink, setInviteLink] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [bracketLoading, setBracketLoading] = useState(false);
  const [error, setError] = useState("");
  const [actionError, setActionError] = useState("");
  const [actionSuccess, setActionSuccess] = useState("");
  const [actionKey, setActionKey] = useState("");

  const isTournamentOrganizer =
    currentRole === "ORGANIZER" &&
    currentUserId !== null &&
    tournament &&
    (Number(currentUserId) === Number(tournament.organizerId) ||
      Number(currentUserId) === Number(tournament.organization?.id));

  const refreshTournament = async () => {
    const [tournamentData, gamesData] = await Promise.all([
      tournamentApi.getById(id),
      gameApi.getAll(),
    ]);

    let matchesData = tournamentData?.matches || [];
    try {
      matchesData = await matchApi.getByTournament(id);
    } catch (matchError) {
      console.warn("Falling back to tournament.matches due to match API error", matchError);
    }

    const entriesData =
      currentUserId && currentRole === "ORGANIZER"
        ? await tournamentApi.getTeamEntries(currentUserId, id)
        : [];

    setTournament(tournamentData ?? null);
    setMatches(matchesData ?? []);
    setGames(gamesData || []);
    setEntries(entriesData || []);
    setForm(createFormFromTournament(tournamentData));
    setDraftBracket(null);
  };

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        setError("");
        await refreshTournament();
      } catch (err) {
        setError(getErrorMessage(err, "Не удалось загрузить управление турниром"));
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [currentRole, currentUserId, id]);

  useEffect(() => {
    const loadAlgorithms = async () => {
      if (!isTournamentOrganizer || !tournament?.gameId || tournament?.bracket) {
        setAlgorithms({});
        setSelectedAlgorithm("");
        setAlgorithmValues({});
        return;
      }

      if (tournament.status !== "REGISTRATION_CLOSED") {
        return;
      }

      try {
        const data = await tournamentApi.getAlgorithms(currentUserId, tournament.gameId);
        const nextAlgorithms = data || {};
        const firstAlgorithm = Object.keys(nextAlgorithms)[0] || "";

        setAlgorithms(nextAlgorithms);
        setSelectedAlgorithm((current) =>
          current && nextAlgorithms[current] ? current : firstAlgorithm
        );
        setAlgorithmValues(getAlgorithmDefaultValues(nextAlgorithms[firstAlgorithm]));
      } catch (err) {
        setActionError(getErrorMessage(err, "Не удалось загрузить алгоритмы сетки"));
      }
    };

    loadAlgorithms();
  }, [currentUserId, isTournamentOrganizer, tournament]);

  useEffect(() => {
    if (!selectedAlgorithm || !algorithms[selectedAlgorithm]) {
      return;
    }

    setAlgorithmValues(getAlgorithmDefaultValues(algorithms[selectedAlgorithm]));
  }, [algorithms, selectedAlgorithm]);

  const sortedEntries = useMemo(() => sortTournamentEntries(entries), [entries]);
  const registeredTeams = useMemo(() => getRegisteredTeams(entries), [entries]);
  const editableSeedSlots = useMemo(() => getSeedSlots(draftBracket), [draftBracket]);
  const canEditSettings = canEditTournament(tournament?.status);
  const editorReadOnlyFields = canEditSettings
    ? ["name", "gameId"]
    : ["name", "gameId", "minTeams", "maxTeams", "startAt", "endAt", "description", "type"];
  const draftMatches = useMemo(
    () => createBracketPreviewMatches(draftBracket, entries),
    [draftBracket, entries]
  );
  const algorithmConfigEntries = useMemo(
    () => getAlgorithmConfigEntries(algorithms[selectedAlgorithm]),
    [algorithms, selectedAlgorithm]
  );
  const duplicateSeeds = useMemo(() => hasDuplicateSeedTeams(draftBracket), [draftBracket]);

  const handleFormChange = (field, value) => {
    setForm((current) => {
      const next = {
        ...current,
        [field]: value,
      };

      if (field === "minTeams" && Number(next.maxTeams) < Number(value || 0)) {
        next.maxTeams = value;
      }

      return next;
    });
  };

  const runAction = async (key, executor, successMessage) => {
    try {
      setActionKey(key);
      setActionError("");
      setActionSuccess("");
      await executor();
      await refreshTournament();
      if (successMessage) {
        setActionSuccess(successMessage);
      }
    } catch (err) {
      setActionError(getErrorMessage(err, "Не удалось выполнить действие"));
    } finally {
      setActionKey("");
    }
  };

  const handleSaveTournament = async (event) => {
    event.preventDefault();

    if (!canEditSettings) {
      setActionError("Редактирование доступно только до закрытия регистрации");
      return;
    }

    try {
      setSaving(true);
      setActionError("");
      setActionSuccess("");

      const payload = buildTournamentPayload(form, { includeImmutable: false });
      await tournamentApi.update(currentUserId, id, payload);
      await refreshTournament();
      setActionSuccess("Турнир обновлен");
    } catch (err) {
      setActionError(getErrorMessage(err, "Не удалось обновить турнир"));
    } finally {
      setSaving(false);
    }
  };

  const handleGenerateBracket = async () => {
    try {
      setBracketLoading(true);
      setActionError("");
      setActionSuccess("");

      const payload = {
        algorithmName: selectedAlgorithm,
        algorithmParams: buildAlgorithmParamsPayload(
          algorithms[selectedAlgorithm],
          algorithmValues
        ),
      };

      const bracket = await tournamentApi.generateBracket(currentUserId, id, payload);
      setDraftBracket(bracket);
      setActionSuccess(
        "Черновик сетки сгенерирован. При желании скорректируй посев и затем сохрани сетку."
      );
    } catch (err) {
      setActionError(getErrorMessage(err, "Не удалось сгенерировать сетку"));
    } finally {
      setBracketLoading(false);
    }
  };

  const handleSubmitBracket = async () => {
    if (!draftBracket) {
      return;
    }

    try {
      setBracketLoading(true);
      setActionError("");
      setActionSuccess("");
      await tournamentApi.submitFinalBracket(currentUserId, id, draftBracket);
      await refreshTournament();
      setActionSuccess("Сетка сохранена");
    } catch (err) {
      setActionError(getErrorMessage(err, "Не удалось сохранить сетку"));
    } finally {
      setBracketLoading(false);
    }
  };

  const handleGenerateInvite = async () => {
    try {
      setActionKey("invite");
      setActionError("");
      setActionSuccess("");
      const link = await tournamentApi.generateInvite(currentUserId, id);
      setInviteLink(link || "");
      setActionSuccess("Приглашение сгенерировано");
    } catch (err) {
      setActionError(getErrorMessage(err, "Не удалось сгенерировать приглашение"));
    } finally {
      setActionKey("");
    }
  };

  const actionButtons = [
    tournament?.status === "CREATED"
      ? {
          key: "start-registration",
          label: "Открыть регистрацию",
          action: () => tournamentApi.startRegistration(currentUserId, id),
          success: "Регистрация открыта",
        }
      : null,
    tournament?.status === "REGISTRATION"
      ? {
          key: "close-registration",
          label: "Закрыть регистрацию",
          action: () => tournamentApi.closeRegistration(currentUserId, id),
          success: "Регистрация закрыта",
        }
      : null,
    tournament?.status === "BRACKET_CREATED"
      ? {
          key: "start-tournament",
          label: "Запустить турнир",
          action: () => tournamentApi.start(currentUserId, id),
          success: "Турнир запущен",
        }
      : null,
    tournament?.status === "RUNNING"
      ? {
          key: "end-tournament",
          label: "Завершить турнир",
          action: () => tournamentApi.end(currentUserId, id),
          success: "Турнир завершен",
        }
      : null,
    tournament &&
    !["FINISHED", "CANCEL", "BANNED"].includes(tournament.status)
      ? {
          key: "cancel-tournament",
          label: "Отменить турнир",
          action: () => tournamentApi.cancel(currentUserId, id),
          success: "Турнир отменен",
          danger: true,
        }
      : null,
  ].filter(Boolean);

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

  if (!isTournamentOrganizer) {
    return (
      <>
        <Header />
        <div className="container tournament-page">
          <div className="section">
            <h3>Доступ ограничен</h3>
            <p>Эта страница доступна только организатору турнира.</p>
            <button className="btn btn-primary" onClick={() => navigate(`/tournaments/${id}`)}>
              Вернуться к турниру
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
            <div className="tournament-title">Управление: {tournament.name}</div>
            <div className="tournament-meta">
              <div>Статус: {translateTournamentStatus(tournament.status)}</div>
              <div>Тип: {translateTournamentType(tournament.type)}</div>
              <div>Старт: {formatDateTime(tournament.startAt)}</div>
            </div>
          </div>

          <button className="btn btn-secondary" onClick={() => navigate(`/tournaments/${id}`)}>
            К странице турнира
          </button>
        </div>

        {(actionError || actionSuccess) && (
          <div className="section">
            {actionError && <div className="team-feedback error">{actionError}</div>}
            {actionSuccess && <div className="team-feedback success">{actionSuccess}</div>}
          </div>
        )}

        <div className="tournament-layout manage-layout">
          <div className="manage-main-column">
            <div className="section">
              <h3>Основные настройки</h3>
              <TournamentEditorForm
                title="Редактирование турнира"
                description="Название и игра остаются фиксированными после создания, остальные параметры можно менять, пока турнир еще не вышел за этап регистрации."
                form={form}
                games={games}
                readOnlyFields={editorReadOnlyFields}
                error={canEditSettings ? "" : "Редактирование доступно только до закрытия регистрации"}
                submitting={saving}
                submitLabel="Сохранить изменения"
                onChange={handleFormChange}
                onCancel={() => setForm(createFormFromTournament(tournament))}
                onSubmit={handleSaveTournament}
              />
            </div>

            <div className="section">
              <h3>Участники</h3>

              {!sortedEntries.length ? (
                <p className="muted-text">Заявок пока нет.</p>
              ) : (
                <div className="tournament-entry-list">
                  {sortedEntries.map((entry) => (
                    <div key={`${entry.tournamentId}-${entry.teamId}`} className="tournament-entry-card">
                      <div>
                        <strong>{entry.team?.name || `Команда #${entry.teamId}`}</strong>
                        <div className="entry-meta">
                          <span>{translateTournamentTeamStatus(entry.status)}</span>
                          <span>{formatDateTime(entry.registeredAt)}</span>
                        </div>
                      </div>

                      <div className="entry-actions">
                        {entry.status === "WAITING_APPROVE" &&
                          canManageTournamentRequests(tournament.status) && (
                            <>
                              <button
                                className="btn btn-primary"
                                disabled={Boolean(actionKey)}
                                onClick={() =>
                                  runAction(
                                    `approve-${entry.teamId}`,
                                    () =>
                                      tournamentApi.handleRequest(
                                        currentUserId,
                                        id,
                                        entry.teamId,
                                        true
                                      ),
                                    "Заявка одобрена"
                                  )
                                }
                              >
                                Одобрить
                              </button>
                              <button
                                className="btn btn-secondary"
                                disabled={Boolean(actionKey)}
                                onClick={() =>
                                  runAction(
                                    `reject-${entry.teamId}`,
                                    () =>
                                      tournamentApi.handleRequest(
                                        currentUserId,
                                        id,
                                        entry.teamId,
                                        false
                                      ),
                                    "Заявка отклонена"
                                  )
                                }
                              >
                                Отклонить
                              </button>
                            </>
                          )}

                        {entry.status === "REGISTERED" &&
                          !["RUNNING", "FINISHED", "CANCEL", "BANNED"].includes(
                            tournament.status
                          ) && (
                            <button
                              className="btn btn-danger"
                              disabled={Boolean(actionKey)}
                              onClick={() =>
                                runAction(
                                  `kick-${entry.teamId}`,
                                  () => tournamentApi.kickTeam(currentUserId, id, entry.teamId),
                                  "Команда исключена"
                                )
                              }
                            >
                              Исключить
                            </button>
                          )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {tournament.status === "REGISTRATION_CLOSED" && !tournament.bracket && (
              <div className="section">
                <h3>Сборка сетки</h3>

                {!Object.keys(algorithms).length ? (
                  <p className="muted-text">Алгоритмы для этой игры не найдены.</p>
                ) : (
                  <>
                    <div className="tournament-generator-grid">
                      <div className="tournament-generator-card">
                        <div className="tournament-generator-card-header">
                          <span className="tournament-generator-label">{ALGORITHM_LABEL}</span>
                        </div>
                        <select
                          className="tournament-generator-control"
                          value={selectedAlgorithm}
                          onChange={(event) => setSelectedAlgorithm(event.target.value)}
                        >
                          {Object.keys(algorithms).map((algorithmName) => (
                            <option key={algorithmName} value={algorithmName}>
                              {algorithmName}
                            </option>
                          ))}
                        </select>
                      </div>

                      {algorithmConfigEntries.map(([fieldName, descriptor]) => {
                        const fieldType = String(descriptor?.type || "").toLowerCase();
                        const value = algorithmValues[fieldName];
                        const fieldLabel = descriptor?.label || fieldName;

                        return (
                          <div
                            key={fieldName}
                            className={`tournament-generator-card${
                              fieldType === "boolean" ? " tournament-generator-card--boolean" : ""
                            }`}
                          >
                            <div className="tournament-generator-card-header">
                              <span className="tournament-generator-label">{fieldLabel}</span>
                              {descriptor?.description && (
                                <span className="field-hint">{descriptor.description}</span>
                              )}
                            </div>

                            {descriptor?.allowedValues?.length ? (
                              <select
                                className="tournament-generator-control"
                                value={value ?? ""}
                                onChange={(event) =>
                                  setAlgorithmValues((current) => ({
                                    ...current,
                                    [fieldName]: event.target.value,
                                  }))
                                }
                              >
                                {descriptor.allowedValues.map((option) => (
                                  <option key={option} value={option}>
                                    {option}
                                  </option>
                                ))}
                              </select>
                            ) : fieldType === "boolean" ? (
                              <label
                                className={`algorithm-toggle${
                                  Boolean(value) ? " is-checked" : ""
                                }`}
                              >
                                <input
                                  className="algorithm-toggle-input"
                                  type="checkbox"
                                  checked={Boolean(value)}
                                  onChange={(event) =>
                                    setAlgorithmValues((current) => ({
                                      ...current,
                                      [fieldName]: event.target.checked,
                                    }))
                                  }
                                />
                                <span className="algorithm-toggle-track" aria-hidden="true">
                                  <span className="algorithm-toggle-thumb" />
                                </span>
                                <span className="algorithm-toggle-text">
                                  {Boolean(value) ? BOOLEAN_ON_LABEL : BOOLEAN_OFF_LABEL}
                                </span>
                              </label>
                            ) : (
                              <input
                                className="tournament-generator-control"
                                type={
                                  ["int", "integer", "long", "double", "float", "number"].includes(
                                    fieldType
                                  )
                                    ? "number"
                                    : "text"
                                }
                                min={Number.isFinite(Number(descriptor?.min)) ? descriptor.min : undefined}
                                max={Number.isFinite(Number(descriptor?.max)) ? descriptor.max : undefined}
                                value={value ?? ""}
                                onChange={(event) =>
                                  setAlgorithmValues((current) => ({
                                    ...current,
                                    [fieldName]: event.target.value,
                                  }))
                                }
                              />
                            )}
                          </div>
                        );
                      })}
                    </div>

                    <div className="team-actions-row">
                      <button
                        className="btn btn-primary"
                        disabled={bracketLoading || !selectedAlgorithm}
                        onClick={handleGenerateBracket}
                      >
                        {bracketLoading ? "Генерируем..." : "Сгенерировать сетку"}
                      </button>
                    </div>
                  </>
                )}

                {draftBracket && (
                  <div className="tournament-bracket-draft">
                    <div className="subsection-header">
                      <h4>Черновик сетки</h4>
                      <p>Ниже можно скорректировать стартовый посев до финального сохранения.</p>
                    </div>

                    <p className="field-hint">{GROUP_NAME_HELP}</p>

                    <div className="group-name-editor-grid">
                      {(draftBracket.bracketGroups || []).map((group, groupIndex) => (
                        <label key={`group-name-${groupIndex}`} className="seed-editor-card">
                          <span>{`${GROUP_NAME_LABEL} ${groupIndex + 1}`}</span>
                          <input
                            type="text"
                            value={group?.name || ""}
                            onChange={(event) =>
                              setDraftBracket((current) =>
                                updateBracketGroupName(current, groupIndex, event.target.value)
                              )
                            }
                            placeholder={`${GROUP_NAME_PLACEHOLDER} ${groupIndex + 1}`}
                          />
                        </label>
                      ))}
                    </div>

                    <BracketGraph
                      bracketGroups={draftBracket.bracketGroups || []}
                      matches={draftMatches}
                      allowMatchNavigation={false}
                    />

                    <div className="seed-editor-grid">
                      {editableSeedSlots.map((slotInfo) => {
                        const selectedTeamIds = editableSeedSlots
                          .filter((candidate) => candidate.key !== slotInfo.key)
                          .map((candidate) => candidate.currentTeamId)
                          .filter(Boolean);

                        return (
                          <label key={slotInfo.key} className="seed-editor-card">
                            <span>
                              Матч #{slotInfo.matchId}, слот {slotInfo.slotIndex + 1}
                            </span>
                            <select
                              value={slotInfo.currentTeamId ?? ""}
                              onChange={(event) =>
                                setDraftBracket((current) =>
                                  updateBracketSeedSlot(
                                    current,
                                    slotInfo.key,
                                    event.target.value || null
                                  )
                                )
                              }
                            >
                              <option value="">TBD</option>
                              {registeredTeams.map((team) => (
                                <option
                                  key={team.id}
                                  value={team.id}
                                  disabled={selectedTeamIds.includes(team.id)}
                                >
                                  {team.name}
                                </option>
                              ))}
                            </select>
                          </label>
                        );
                      })}
                    </div>

                    {duplicateSeeds && (
                      <div className="team-feedback error">
                        В сетке есть повторяющиеся команды. Исправь посев перед сохранением.
                      </div>
                    )}

                    <div className="team-actions-row">
                      <button
                        className="btn btn-primary"
                        disabled={bracketLoading || duplicateSeeds}
                        onClick={handleSubmitBracket}
                      >
                        {bracketLoading ? "Сохраняем..." : "Сохранить сетку"}
                      </button>
                    </div>
                  </div>
                )}
              </div>
            )}

            {tournament.bracket && (
              <div className="section">
                <h3>Текущая сетка</h3>
                <p className="muted-text">
                  Нажми на матч в сетке, чтобы открыть его отдельную страницу. Управление матчем
                  доступно на отдельной странице матча.
                </p>
                <BracketGraph
                  bracketGroups={tournament.bracket?.bracketGroups || []}
                  matches={matches}
                />
              </div>
            )}
          </div>

          <div className="sidebar">
            <div className="section">
              <h3>Статус турнира</h3>
              <div className="manage-status-list">
                {actionButtons.map((button) => (
                  <button
                    key={button.key}
                    className={`btn ${button.danger ? "btn-danger" : "btn-primary"}`}
                    disabled={Boolean(actionKey)}
                    onClick={() => runAction(button.key, button.action, button.success)}
                  >
                    {actionKey === button.key ? "Выполняем..." : button.label}
                  </button>
                ))}
              </div>
            </div>

            {tournament.type === "PRIVATE" && (
              <div className="section">
                <h3>Приватный доступ</h3>
                <p className="muted-text">
                  Для приватного турнира можно раздать командам токен-приглашение.
                </p>
                <button
                  className="btn btn-secondary"
                  disabled={actionKey === "invite"}
                  onClick={handleGenerateInvite}
                >
                  {actionKey === "invite" ? "Генерируем..." : "Сгенерировать invite"}
                </button>

                {inviteLink && (
                  <div className="invite-box">
                    <input value={inviteLink} readOnly />
                    <button
                      className="btn btn-secondary"
                      onClick={() => navigator.clipboard?.writeText(inviteLink)}
                    >
                      Копировать
                    </button>
                  </div>
                )}
              </div>
            )}

            <div className="section">
              <h3>Сводка</h3>
              <p>Команд в турнире: {entries.length}</p>
              <p>
                Подтверждено: {entries.filter((entry) => entry.status === "REGISTERED").length}
              </p>
              <p>
                Ожидают решения:{" "}
                {entries.filter((entry) => entry.status === "WAITING_APPROVE").length}
              </p>
              <p>
                Лимит: {tournament.minTeams} - {tournament.maxTeams}
              </p>
              <p>Матчей: {matches.length}</p>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
