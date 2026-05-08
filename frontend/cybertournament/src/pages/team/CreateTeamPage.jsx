import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "@/shared/ui/Header";
import { gameApi } from "@/shared/api/gameApi";
import { teamApi } from "@/shared/api/teamApi";
import { getErrorMessage } from "@/shared/api/client";
import { useAuthStore } from "@/app/store/authStore";
import { translateTeamType } from "@/shared/lib/enumLabels";
import "@/shared/styles/team-pages.css";

const initialForm = {
  name: "",
  gameId: "",
  type: "PUBLIC",
};

export default function CreateTeamPage() {
  const navigate = useNavigate();
  const currentRole = useAuthStore((state) => state.currentRole);
  const currentUserId = useAuthStore((state) => state.currentUserId);

  const [games, setGames] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadGames = async () => {
      try {
        setLoading(true);
        setError("");

        const gamesData = await gameApi.getAll();
        setGames(gamesData || []);

        setForm((prev) => ({
          ...prev,
          gameId: prev.gameId || String(gamesData?.[0]?.id || ""),
        }));
      } catch (err) {
        setError(getErrorMessage(err, "Не удалось загрузить список игр"));
      } finally {
        setLoading(false);
      }
    };

    loadGames();
  }, []);

  const handleChange = (field) => (event) => {
    setForm((prev) => ({
      ...prev,
      [field]: event.target.value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!currentUserId) {
      setError("Не удалось определить текущего игрока");
      return;
    }

    try {
      setSubmitting(true);
      setError("");

      const createdTeam = await teamApi.create(currentUserId, {
        name: form.name.trim(),
        gameId: Number(form.gameId),
        type: form.type,
      });

      navigate(`/teams/${createdTeam.id}`);
    } catch (err) {
      setError(getErrorMessage(err, "Не удалось создать команду"));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <Header />

      <div className="container team-shell">
        <div className="team-hero">
          <div>
            <h1>Создание команды</h1>
            <p>Выбери игру, название команды и формат набора участников.</p>
          </div>
        </div>

        {currentRole !== "PLAYER" ? (
          <div className="team-panel team-empty-state">
            <h2>Создание команды доступно только игрокам</h2>
            <p>Организаторы не участвуют в командах, поэтому этот раздел предназначен только для игроков.</p>
            <button className="btn btn-secondary" onClick={() => navigate("/teams")}>
              Перейти к списку команд
            </button>
          </div>
        ) : loading ? (
          <div className="team-panel">Загрузка...</div>
        ) : (
          <form className="team-panel team-form" onSubmit={handleSubmit}>
            <div className="team-form-grid">
              <label>
                Название команды
                <input
                  value={form.name}
                  onChange={handleChange("name")}
                  placeholder="Например, Neon Wolves"
                  minLength={2}
                  maxLength={64}
                  required
                />
              </label>

              <label>
                Игра
                <select value={form.gameId} onChange={handleChange("gameId")} required>
                  {games.map((game) => (
                    <option key={game.id} value={game.id}>
                      {game.name}
                    </option>
                  ))}
                </select>
              </label>
            </div>

            <div className="team-type-grid">
              {["PUBLIC", "PRIVATE"].map((teamType) => {
                const isActive = form.type === teamType;

                return (
                  <button
                    key={teamType}
                    type="button"
                    className={`team-type-card ${isActive ? "active" : ""}`}
                    onClick={() =>
                      setForm((prev) => ({
                        ...prev,
                        type: teamType,
                      }))
                    }
                  >
                    <strong>{translateTeamType(teamType)}</strong>
                    <span>
                      {teamType === "PUBLIC"
                        ? "Игроки смогут вступать сразу, если подходят по условиям команды."
                        : "Игроки будут отправлять заявку, а капитан сам решит, кого принять."}
                    </span>
                  </button>
                );
              })}
            </div>

            {error && <div className="team-feedback error">{error}</div>}

            <div className="team-actions-row">
              <button className="btn btn-primary" type="submit" disabled={submitting}>
                {submitting ? "Создаём..." : "Создать команду"}
              </button>

              <button
                className="btn btn-secondary"
                type="button"
                onClick={() => navigate("/teams")}
              >
                К списку команд
              </button>
            </div>
          </form>
        )}
      </div>
    </>
  );
}
