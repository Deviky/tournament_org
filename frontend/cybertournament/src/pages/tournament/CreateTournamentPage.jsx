import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "@/shared/ui/Header";
import TournamentEditorForm from "@/shared/ui/TournamentEditorForm";
import { gameApi } from "@/shared/api/gameApi";
import { tournamentApi } from "@/shared/api/tournamentApi";
import { getErrorMessage } from "@/shared/api/client";
import { useAuthStore } from "@/app/store/authStore";
import { buildTournamentPayload, toDateTimeLocalValue } from "@/shared/lib/tournamentUtils";
import "@/shared/styles/tournament.css";

const createEmptyForm = (games = []) => ({
  name: "",
  gameId: games[0]?.id ? String(games[0].id) : "",
  description: "",
  minTeams: "2",
  maxTeams: "8",
  type: "PUBLIC",
  startAt: toDateTimeLocalValue(new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()),
  endAt: "",
});

export default function CreateTournamentPage() {
  const navigate = useNavigate();
  const currentRole = useAuthStore((state) => state.currentRole);
  const currentUserId = useAuthStore((state) => state.currentUserId);

  const [games, setGames] = useState([]);
  const [form, setForm] = useState(createEmptyForm());
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
        setForm(createEmptyForm(gamesData || []));
      } catch (err) {
        setError(getErrorMessage(err, "Не удалось загрузить список игр"));
      } finally {
        setLoading(false);
      }
    };

    loadGames();
  }, []);

  const handleChange = (field, value) => {
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

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (currentRole !== "ORGANIZER" || !currentUserId) {
      setError("Создание турнира доступно только организатору");
      return;
    }

    try {
      setSubmitting(true);
      setError("");

      const payload = buildTournamentPayload(form);
      const tournament = await tournamentApi.create(currentUserId, payload);
      navigate(`/tournaments/${tournament.id}/manage`);
    } catch (err) {
      setError(getErrorMessage(err, "Не удалось создать турнир"));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <>
        <Header />
        <div className="container tournament-page">Загрузка...</div>
      </>
    );
  }

  return (
    <>
      <Header />

      <div className="container tournament-page">
        <div className="tournament-header">
          <div>
            <div className="tournament-title">Создание турнира</div>
            <div className="tournament-meta">
              <div>Настрой базовые параметры, тип доступа и расписание.</div>
            </div>
          </div>
        </div>

        <div className="section">
          <TournamentEditorForm
            title="Новый турнир"
            description="После создания турнир появится в статусе черновика. Затем можно открыть регистрацию, управлять заявками и собрать сетку."
            form={form}
            games={games}
            error={error}
            submitting={submitting}
            submitLabel="Создать турнир"
            onChange={handleChange}
            onCancel={() => navigate(-1)}
            onSubmit={handleSubmit}
          />
        </div>
      </div>
    </>
  );
}
