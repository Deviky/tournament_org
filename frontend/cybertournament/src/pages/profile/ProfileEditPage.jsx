import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "@/shared/ui/Header";
import { useAuthStore } from "@/app/store/authStore";
import { withUserId, getErrorMessage } from "@/shared/api/client";
import {
  getProfilePathByIdentity,
  getRoleLabel,
  hasDirectProfileByIdentity,
} from "@/shared/lib/authIdentity";
import { playerApi } from "@/shared/api/playerApi";
import { organizationApi } from "@/shared/api/organizationApi";
import { gameApi } from "@/shared/api/gameApi";
import {
  createEditablePlayerGame,
  getGameConfigById,
} from "@/shared/lib/playerGameFields";
import "@/shared/styles/tournament.css";
import "@/shared/styles/auth.css";

const createEmptyGame = () => createEditablePlayerGame();

export default function ProfileEditPage() {
  const navigate = useNavigate();
  const currentRole = useAuthStore((s) => s.currentRole);
  const currentUserId = useAuthStore((s) => s.currentUserId);
  const currentSubject = useAuthStore((s) => s.currentSubject);

  const profilePath = getProfilePathByIdentity(currentRole, currentUserId);
  const hasEditableProfile = hasDirectProfileByIdentity(currentRole, currentUserId);
  const roleLabel = getRoleLabel(currentRole);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [availableGames, setAvailableGames] = useState([]);
  const [nickname, setNickname] = useState("");
  const [games, setGames] = useState([createEmptyGame()]);
  const [organizerName, setOrganizerName] = useState("");
  const [description, setDescription] = useState("");

  useEffect(() => {
    const load = async () => {
      if (!currentRole || !currentUserId) {
        setLoading(false);
        return;
      }

      if (!hasDirectProfileByIdentity(currentRole, currentUserId)) {
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError("");

        if (currentRole === "PLAYER") {
          const [gamesData, playerData] = await Promise.all([
            gameApi.getAll(),
            playerApi.getById(currentUserId),
          ]);

          setAvailableGames(gamesData || []);
          setNickname(playerData?.nickname || "");
          setGames(
            playerData?.games?.length
              ? playerData.games.map((game) => createEditablePlayerGame(game))
              : [createEmptyGame()]
          );
        } else if (currentRole === "ORGANIZER") {
          const organization = await organizationApi.getById(currentUserId);
          setOrganizerName(organization?.organizerName || "");
          setDescription(organization?.description || "");
        }
      } catch (err) {
        setError(getErrorMessage(err, "Не удалось загрузить профиль для редактирования"));
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [currentRole, currentUserId]);

  const selectedGameIds = useMemo(
    () => games.map((game) => String(game.gameId || "")).filter(Boolean),
    [games]
  );

  const addGame = () => {
    if (games.length >= availableGames.length) {
      return;
    }

    setGames((prev) => [...prev, createEmptyGame()]);
  };

  const removeGame = (id) => {
    if (games.length === 1) {
      return;
    }

    setGames((prev) => prev.filter((game) => game.id !== id));
  };

  const selectGame = (id, nextGameId) => {
    setGames((prev) =>
      prev.map((game) => {
        if (game.id !== id) {
          return game;
        }

        const config = getGameConfigById(nextGameId, availableGames);

        return {
          ...game,
          gameId: nextGameId,
          links: config
            ? Object.fromEntries(
                config.map((field) => [field.key, game.links[field.key] || ""])
              )
            : {},
        };
      })
    );
  };

  const updateField = (id, fieldKey, value) => {
    setGames((prev) =>
      prev.map((game) =>
        game.id === id
          ? { ...game, links: { ...game.links, [fieldKey]: value } }
          : game
      )
    );
  };

  const validatePlayer = () => {
    if (!nickname.trim()) {
      return "Никнейм обязателен";
    }

    const uniqueGameIds = new Set();

    for (const game of games) {
      if (!game.gameId) {
        return "Выберите игру";
      }

      if (uniqueGameIds.has(String(game.gameId))) {
        return "Нельзя выбирать одну и ту же игру несколько раз";
      }

      uniqueGameIds.add(String(game.gameId));

      const config = getGameConfigById(game.gameId, availableGames);
      if (!config) {
        continue;
      }

      for (const field of config) {
        if (field.required && !String(game.links[field.key] || "").trim()) {
          return `${field.label} обязателен`;
        }
      }
    }

    return null;
  };

  const handleSave = async () => {
    try {
      setSaving(true);
      setError("");
      setSuccess("");

      if (currentRole === "PLAYER") {
        const validationError = validatePlayer();
        if (validationError) {
          setError(validationError);
          return;
        }

        await playerApi.update(
          {
            nickname: nickname.trim(),
            games: games.map((game) => ({
              gameId: Number(game.gameId),
              links: game.links,
            })),
          },
          withUserId(currentUserId)
        );
      } else if (currentRole === "ORGANIZER") {
        if (!organizerName.trim()) {
          setError("Название организатора обязательно");
          return;
        }

        await organizationApi.update(
          {
            organizerName: organizerName.trim(),
            description,
          },
          withUserId(currentUserId)
        );
      }

      setSuccess("Профиль обновлён");
    } catch (err) {
      setError(getErrorMessage(err, "Не удалось сохранить профиль"));
    } finally {
      setSaving(false);
    }
  };

  if (!currentRole || !currentUserId) {
    return (
      <>
        <Header />
        <div className="container tournament-page">
          <div className="section">
            <h3>Редактирование профиля</h3>
            <p>Сначала нужно войти в аккаунт.</p>
          </div>
        </div>
      </>
    );
  }

  if (loading) {
    return (
      <>
        <Header />
        <div className="container tournament-page">Загрузка...</div>
      </>
    );
  }

  if (!hasEditableProfile) {
    return (
      <>
        <Header />
        <div className="container tournament-page">
          <div className="section">
            <h3>Редактирование профиля</h3>
            <p>
              Для роли <strong>{roleLabel}</strong> отдельная форма редактирования пока не предусмотрена.
            </p>
            <p>
              Этот аккаунт используется как служебный и не привязан к профилю игрока или организатора.
            </p>
            <div className="tournament-meta" style={{ marginTop: 16 }}>
              <div>Роль: {roleLabel}</div>
              <div>Email: {currentSubject || "Не указан"}</div>
            </div>
            <div className="team-actions-row" style={{ marginTop: 20 }}>
              <button className="btn btn-secondary" onClick={() => navigate("/profile")}>
                К профилю
              </button>
              <button className="btn btn-primary" onClick={() => navigate("/")}>
                На главную
              </button>
            </div>
          </div>
        </div>
      </>
    );
  }

  return (
    <>
      <Header />

      <div className="container tournament-page">
        <div className="section">
          <div className="tournament-header">
            <div>
              <div className="tournament-title">Редактирование профиля</div>
              <div className="tournament-meta">
                <div>{currentRole === "PLAYER" ? "Профиль игрока" : "Профиль организатора"}</div>
              </div>
            </div>

            <button className="btn btn-secondary" onClick={() => navigate(profilePath)}>
              К профилю
            </button>
          </div>

          {error && <div className="team-feedback error" style={{ marginTop: 16 }}>{error}</div>}
          {success && (
            <div className="team-feedback success" style={{ marginTop: 16 }}>
              {success}
            </div>
          )}

          {currentRole === "PLAYER" ? (
            <div style={{ marginTop: 20 }}>
              <input
                className="auth-input"
                placeholder="Никнейм *"
                value={nickname}
                onChange={(event) => setNickname(event.target.value)}
              />

              <h3 style={{ margin: "10px 0" }}>Игры и ссылки</h3>

              {games.map((game) => {
                const config = getGameConfigById(game.gameId, availableGames);

                return (
                  <div
                    key={game.id}
                    className="card"
                    style={{ marginBottom: 15, position: "relative" }}
                  >
                    <button
                      className="remove-btn"
                      onClick={() => removeGame(game.id)}
                      disabled={games.length === 1}
                    >
                      x
                    </button>

                    <select
                      className="auth-input"
                      value={game.gameId}
                      onChange={(event) => selectGame(game.id, event.target.value)}
                    >
                      <option value="">Выберите игру *</option>
                      {availableGames.map((availableGame) => {
                        const isSelectedElsewhere =
                          selectedGameIds.includes(String(availableGame.id)) &&
                          String(game.gameId) !== String(availableGame.id);

                        return (
                          <option
                            key={availableGame.id}
                            value={availableGame.id}
                            disabled={isSelectedElsewhere}
                          >
                            {availableGame.name}
                          </option>
                        );
                      })}
                    </select>

                    {config?.map((field) => (
                      <input
                        key={field.key}
                        className="auth-input"
                        placeholder={field.label + (field.required ? " *" : "")}
                        value={game.links[field.key] || ""}
                        onChange={(event) =>
                          updateField(game.id, field.key, event.target.value)
                        }
                      />
                    ))}
                  </div>
                );
              })}

              <button
                className="btn btn-secondary"
                onClick={addGame}
                disabled={games.length >= availableGames.length}
                style={{ width: "auto" }}
              >
                + Добавить игру
              </button>
            </div>
          ) : (
            <div style={{ marginTop: 20 }}>
              <input
                className="auth-input"
                placeholder="Название организатора *"
                value={organizerName}
                onChange={(event) => setOrganizerName(event.target.value)}
              />

              <textarea
                className="auth-input"
                placeholder="Описание"
                value={description}
                onChange={(event) => setDescription(event.target.value)}
              />
            </div>
          )}

          <div className="team-actions-row" style={{ marginTop: 20 }}>
            <button className="btn btn-primary" disabled={saving} onClick={handleSave}>
              {saving ? "Сохраняем..." : "Сохранить"}
            </button>
            <button className="btn btn-secondary" onClick={() => navigate(profilePath)}>
              Отмена
            </button>
          </div>
        </div>
      </div>
    </>
  );
}
