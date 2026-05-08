import { useEffect, useMemo, useState } from "react";
import { toDateTimeLocalValue } from "@/shared/lib/tournamentUtils";
import {
  translateMatchResult,
  translateMatchStatus,
  translateTeamStatus,
} from "@/shared/lib/enumLabels";

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

const buildMatchTitle = (match) => {
  const names = (match?.teams || []).map((team) => team?.name).filter(Boolean);

  if (!names.length) {
    return `Матч #${match?.id}`;
  }

  if (names.length === 1) {
    return `${names[0]} vs TBD`;
  }

  return `${names[0]} vs ${names[1]}`;
};

const buildFinishPayload = (match, winnerTeamId) => {
  const teamToMatchResult = Object.fromEntries(
    (match?.teams || []).map((team) => [
      team.id,
      Number(team.id) === Number(winnerTeamId) ? "WINNER" : "LOSER",
    ])
  );

  return {
    matchId: match.id,
    teamToMatchResult,
  };
};

export default function TournamentMatchesSection({
  title = "Матчи",
  matches = [],
  emptyText = "Матчей пока нет.",
  isOrganizer = false,
  busy = false,
  onUpdateMatch,
  onStartMatch,
  onFinishMatch,
  onCancelMatch,
}) {
  const [forms, setForms] = useState({});
  const [winnerSelections, setWinnerSelections] = useState({});

  useEffect(() => {
    const nextForms = {};
    const nextWinners = {};

    (matches || []).forEach((match) => {
      nextForms[match.id] = {
        links: match?.links || "",
        startAt: toDateTimeLocalValue(match?.startAt),
      };

      const currentWinner = (match?.teams || []).find((team) => team?.result === "WINNER");
      nextWinners[match.id] = currentWinner ? String(currentWinner.id) : "";
    });

    setForms(nextForms);
    setWinnerSelections(nextWinners);
  }, [matches]);

  const sortedMatches = useMemo(
    () =>
      [...(matches || [])].sort((left, right) => {
        const leftDate = left?.startAt ? new Date(left.startAt).getTime() : Number.MAX_SAFE_INTEGER;
        const rightDate = right?.startAt
          ? new Date(right.startAt).getTime()
          : Number.MAX_SAFE_INTEGER;

        if (leftDate !== rightDate) {
          return leftDate - rightDate;
        }

        return Number(left?.id || 0) - Number(right?.id || 0);
      }),
    [matches]
  );

  const handleFormChange = (matchId, field, value) => {
    setForms((current) => ({
      ...current,
      [matchId]: {
        ...(current[matchId] || {}),
        [field]: value,
      },
    }));
  };

  const handleUpdate = async (match) => {
    if (!onUpdateMatch) {
      return;
    }

    const form = forms[match.id] || {};

    await onUpdateMatch({
      matchId: match.id,
      links: form.links || "",
      startAt: form.startAt || null,
      endAt: null,
    });
  };

  const handleFinish = async (match) => {
    if (!onFinishMatch) {
      return;
    }

    const winnerTeamId = winnerSelections[match.id];
    if (!winnerTeamId) {
      return;
    }

    await onFinishMatch(buildFinishPayload(match, winnerTeamId));
  };

  return (
    <div className="section">
      <h3>{title}</h3>

      {!sortedMatches.length ? (
        <p className="muted-text">{emptyText}</p>
      ) : (
        <div className="match-list">
          {sortedMatches.map((match) => {
            const canEdit = isOrganizer && match.status === "COMING";
            const canStart =
              isOrganizer && match.status === "COMING" && (match.teams || []).length > 0;
            const canFinish =
              isOrganizer && match.status === "RUNNING" && (match.teams || []).length > 0;
            const canCancel =
              isOrganizer && !["FINISHED", "CANCELED"].includes(String(match.status));
            const selectedWinner = winnerSelections[match.id] || "";

            return (
              <div key={match.id} className="match-card">
                <div className="match-card-header">
                  <div>
                    <div className="match-title">{buildMatchTitle(match)}</div>
                    <div className="match-subtitle">Матч #{match.id}</div>
                  </div>

                  <span className={`match-status-badge is-${String(match.status || "").toLowerCase()}`}>
                    {translateMatchStatus(match.status)}
                  </span>
                </div>

                <div className="match-meta-grid">
                  <div>
                    <span className="match-meta-label">Начало</span>
                    <span>{formatDateTime(match.startAt)}</span>
                  </div>
                  <div>
                    <span className="match-meta-label">Окончание</span>
                    <span>{formatDateTime(match.endAt)}</span>
                  </div>
                  <div>
                    <span className="match-meta-label">Ссылки</span>
                    <span>{match.links || "Не указаны"}</span>
                  </div>
                </div>

                <div className="match-team-list">
                  {(match.teams || []).length ? (
                    match.teams.map((team) => (
                      <div key={team.id} className="match-team-card">
                        <div className="match-team-main">
                          <strong>{team.name}</strong>
                          <span className="muted-text">{translateTeamStatus(team.status)}</span>
                        </div>
                        <span
                          className={`match-result-badge is-${String(team.result || "").toLowerCase()}`}
                        >
                          {translateMatchResult(team.result)}
                        </span>
                      </div>
                    ))
                  ) : (
                    <p className="muted-text">Участники определятся позже.</p>
                  )}
                </div>

                {isOrganizer && (
                  <div className="match-manage-panel">
                    {canEdit && (
                      <div className="match-edit-grid">
                        <label>
                          <span>Плановое время начала</span>
                          <input
                            type="datetime-local"
                            value={forms[match.id]?.startAt || ""}
                            onChange={(event) =>
                              handleFormChange(match.id, "startAt", event.target.value)
                            }
                          />
                        </label>

                        <label>
                          <span>Ссылки на матч</span>
                          <input
                            type="text"
                            placeholder="Например, lobby / room / stream"
                            value={forms[match.id]?.links || ""}
                            onChange={(event) =>
                              handleFormChange(match.id, "links", event.target.value)
                            }
                          />
                        </label>
                      </div>
                    )}

                    {canFinish && (
                      <div className="match-edit-grid">
                        <label>
                          <span>Победитель</span>
                          <select
                            value={selectedWinner}
                            onChange={(event) =>
                              setWinnerSelections((current) => ({
                                ...current,
                                [match.id]: event.target.value,
                              }))
                            }
                          >
                            <option value="">Выберите победителя</option>
                            {(match.teams || []).map((team) => (
                              <option key={team.id} value={team.id}>
                                {team.name}
                              </option>
                            ))}
                          </select>
                        </label>
                      </div>
                    )}

                    <div className="match-action-row">
                      {canEdit && (
                        <button className="btn btn-secondary" disabled={busy} onClick={() => handleUpdate(match)}>
                          Сохранить матч
                        </button>
                      )}

                      {canStart && (
                        <button
                          className="btn btn-primary"
                          disabled={busy}
                          onClick={() => onStartMatch?.(match.id)}
                        >
                          Запустить матч
                        </button>
                      )}

                      {canFinish && (
                        <button
                          className="btn btn-primary"
                          disabled={busy || !selectedWinner}
                          onClick={() => handleFinish(match)}
                        >
                          Завершить матч
                        </button>
                      )}

                      {canCancel && (
                        <button
                          className="btn btn-danger"
                          disabled={busy}
                          onClick={() => onCancelMatch?.(match.id)}
                        >
                          Отменить матч
                        </button>
                      )}
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
