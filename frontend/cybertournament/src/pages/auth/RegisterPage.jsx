import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi } from "@/shared/api/authApi";
import { gameApi } from "@/shared/api/gameApi";
import { getErrorMessage } from "@/shared/api/client";
import "@/shared/styles/auth.css";

const RESEND_COOLDOWN_SECONDS = 60;

const GAME_FIELDS_CONFIG = {
  CS2: [
    { key: "STEAM", label: "Ссылка на Steam", required: true },
    { key: "FACEIT", label: "Ссылка на Faceit", required: false },
  ],
  DOTA2: [
    { key: "STEAM", label: "Ссылка на Steam", required: true },
    { key: "DOTA_ID", label: "ID в Dota", required: true },
    { key: "DOTABUFF", label: "Ссылка на Dotabuff", required: false },
  ],
};

const createGame = () => ({
  id: crypto.randomUUID(),
  gameId: "",
  links: {},
});

const normalizeGameName = (value) =>
  String(value || "")
    .trim()
    .toLowerCase()
    .replace(/\s+/g, " ");

const getGameConfigKey = (gameName) => {
  const normalizedName = normalizeGameName(gameName);

  if (["cs2", "counter-strike 2", "counter strike 2"].includes(normalizedName)) {
    return "CS2";
  }

  if (["dota 2", "dota2"].includes(normalizedName)) {
    return "DOTA2";
  }

  return null;
};

export default function RegisterPage() {
  const navigate = useNavigate();
  const [type, setType] = useState("player");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [nickname, setNickname] = useState("");
  const [organizerName, setOrganizerName] = useState("");
  const [description, setDescription] = useState("");
  const [games, setGames] = useState([createGame()]);
  const [availableGames, setAvailableGames] = useState([]);
  const [gamesLoading, setGamesLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [resendLoading, setResendLoading] = useState(false);
  const [resendCooldown, setResendCooldown] = useState(0);
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");
  const [successEmail, setSuccessEmail] = useState("");

  useEffect(() => {
    const loadGames = async () => {
      try {
        setGamesLoading(true);
        const data = await gameApi.getAll();
        setAvailableGames(data || []);
      } catch (e) {
        setError(getErrorMessage(e, "Не удалось загрузить список игр"));
      } finally {
        setGamesLoading(false);
      }
    };

    loadGames();
  }, []);

  useEffect(() => {
    if (resendCooldown <= 0) {
      return undefined;
    }

    const timer = window.setInterval(() => {
      setResendCooldown((prev) => (prev > 1 ? prev - 1 : 0));
    }, 1000);

    return () => window.clearInterval(timer);
  }, [resendCooldown]);

  const selectedGameIds = useMemo(
    () => games.map((game) => String(game.gameId || "")).filter(Boolean),
    [games]
  );

  const getConfigByGameId = (gameId) => {
    const game = availableGames.find((item) => String(item.id) === String(gameId));
    if (!game) {
      return null;
    }

    return GAME_FIELDS_CONFIG[getGameConfigKey(game.name)] || null;
  };

  const resetRegistrationForm = () => {
    setEmail("");
    setPassword("");
    setConfirmPassword("");
    setNickname("");
    setOrganizerName("");
    setDescription("");
    setGames([createGame()]);
  };

  const addGame = () => {
    if (games.length >= availableGames.length) {
      return;
    }

    setGames((prev) => [...prev, createGame()]);
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

        const config = getConfigByGameId(nextGameId);

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

  const validate = () => {
    if (!email.trim()) {
      return "Электронная почта обязательна";
    }

    if (!password.trim()) {
      return "Пароль обязателен";
    }

    if (!confirmPassword.trim()) {
      return "Подтвердите пароль";
    }

    if (password !== confirmPassword) {
      return "Пароли не совпадают";
    }

    if (type === "player") {
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

        const config = getConfigByGameId(game.gameId);
        if (!config) {
          continue;
        }

        for (const field of config) {
          if (field.required && !String(game.links[field.key] || "").trim()) {
            return `${field.label} обязательна`;
          }
        }
      }
    }

    if (type === "org" && !organizerName.trim()) {
      return "Название обязательно";
    }

    return null;
  };

  const handleResendConfirmation = async (targetEmail) => {
    if (!targetEmail || resendCooldown > 0) {
      return;
    }

    try {
      setResendLoading(true);
      setError("");
      setInfo("");
      await authApi.resend(targetEmail);
      setInfo("Письмо с подтверждением отправлено повторно. Проверьте вашу почту.");
      setResendCooldown(RESEND_COOLDOWN_SECONDS);
    } catch (e) {
      setError(getErrorMessage(e, "Не удалось отправить письмо повторно"));
    } finally {
      setResendLoading(false);
    }
  };

  const register = async () => {
    const validationError = validate();
    if (validationError) {
      setSuccessEmail("");
      setError(validationError);
      return;
    }

    setError("");
    setInfo("");
    setSubmitting(true);

    try {
      const registeredEmail = email.trim();
      const payload =
        type === "player"
          ? {
              email,
              password,
              nickname,
              games: games.map((game) => ({
                gameId: Number(game.gameId),
                links: game.links,
              })),
            }
          : { email, password, organizerName, description };

      if (type === "player") {
        await authApi.registerPlayer(payload);
      } else {
        await authApi.registerOrg(payload);
      }

      setSuccessEmail(registeredEmail);
      setResendCooldown(RESEND_COOLDOWN_SECONDS);
      resetRegistrationForm();
    } catch (e) {
      setSuccessEmail("");
      setError(getErrorMessage(e, "Ошибка регистрации"));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h2 className="auth-title">Регистрация</h2>

        {error && <div className="auth-error">{error}</div>}
        {info && <div className="auth-info">{info}</div>}

        {successEmail ? (
          <div className="auth-success">
            <div className="auth-success-badge">✓</div>
            <h3 className="auth-success-title">Проверьте вашу почту</h3>
            <p className="auth-success-text">
              Мы отправили письмо для подтверждения регистрации на адрес:
            </p>
            <p className="auth-success-email">{successEmail}</p>
            <p className="auth-success-text">
              Если письмо не пришло сразу, попробуйте отправить его повторно.
            </p>

            <div className="auth-actions">
              <button className="auth-btn" onClick={() => navigate("/login")}>
                Перейти ко входу
              </button>
              <button
                className="btn btn-secondary"
                onClick={() => handleResendConfirmation(successEmail)}
                disabled={resendLoading || resendCooldown > 0}
              >
                {resendLoading
                  ? "Отправляем..."
                  : resendCooldown > 0
                    ? `Отправить повторно через ${resendCooldown} c`
                    : "Отправить письмо повторно"}
              </button>
              <button
                className="btn btn-secondary"
                onClick={() => {
                  setSuccessEmail("");
                  setInfo("");
                  setError("");
                  setResendCooldown(0);
                }}
              >
                Зарегистрировать еще аккаунт
              </button>
            </div>
          </div>
        ) : (
          <>
            <div className="row" style={{ marginBottom: 15 }}>
              <button
                className={`btn ${type === "player" ? "btn-primary" : "btn-secondary"}`}
                onClick={() => setType("player")}
                disabled={submitting}
              >
                Игрок
              </button>

              <button
                className={`btn ${type === "org" ? "btn-primary" : "btn-secondary"}`}
                onClick={() => setType("org")}
                disabled={submitting}
              >
                Организатор
              </button>
            </div>

            <input
              className="auth-input"
              placeholder="Электронная почта *"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
            />

            <input
              className="auth-input"
              type="password"
              placeholder="Пароль *"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />

            <input
              className="auth-input"
              type="password"
              placeholder="Подтвердите пароль *"
              value={confirmPassword}
              onChange={(event) => setConfirmPassword(event.target.value)}
            />

            {type === "player" && (
              <>
                <input
                  className="auth-input"
                  placeholder="Никнейм *"
                  value={nickname}
                  onChange={(event) => setNickname(event.target.value)}
                />

                <h3 style={{ margin: "10px 0" }}>Игры</h3>

                {games.map((game) => {
                  const config = getConfigByGameId(game.gameId);

                  return (
                    <div
                      key={game.id}
                      className="card"
                      style={{ marginBottom: 15, position: "relative" }}
                    >
                      <button
                        className="remove-btn"
                        onClick={() => removeGame(game.id)}
                        disabled={games.length === 1 || submitting}
                      >
                        x
                      </button>

                      <select
                        className="auth-input"
                        value={game.gameId}
                        onChange={(event) => selectGame(game.id, event.target.value)}
                        disabled={gamesLoading || submitting}
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
                  disabled={
                    submitting ||
                    gamesLoading ||
                    games.length >= availableGames.length
                  }
                >
                  + Добавить игру
                </button>
              </>
            )}

            {type === "org" && (
              <>
                <input
                  className="auth-input"
                  placeholder="Название *"
                  value={organizerName}
                  onChange={(event) => setOrganizerName(event.target.value)}
                />

                <textarea
                  className="auth-input"
                  placeholder="Описание"
                  value={description}
                  onChange={(event) => setDescription(event.target.value)}
                />
              </>
            )}

            <button className="auth-btn" onClick={register} disabled={submitting}>
              {submitting ? "Отправляем..." : "Зарегистрироваться"}
            </button>
          </>
        )}
      </div>
    </div>
  );
}
