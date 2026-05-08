import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "@/shared/ui/Header";
import { organizationApi } from "@/shared/api/organizationApi";
import { tournamentApi } from "@/shared/api/tournamentApi";
import { getErrorMessage } from "@/shared/api/client";
import { useAuthStore } from "@/app/store/authStore";
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
    : "Не указана";

const cutText = (text, max = 180) =>
  text?.length > max ? `${text.slice(0, max)}...` : text;

export default function OrganizationPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const currentRole = useAuthStore((s) => s.currentRole);
  const currentUserId = useAuthStore((s) => s.currentUserId);

  const [organization, setOrganization] = useState(null);
  const [tournaments, setTournaments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        setError("");

        const [organizationData, tournamentsData] = await Promise.all([
          organizationApi.getById(id),
          tournamentApi.getAll(),
        ]);

        setOrganization(organizationData ?? null);
        setTournaments(Array.isArray(tournamentsData) ? tournamentsData : []);
      } catch (err) {
        setOrganization(null);
        setTournaments([]);
        setError(getErrorMessage(err, "Не удалось загрузить профиль организатора"));
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [id]);

  const isOwnOrganization =
    currentRole === "ORGANIZER" &&
    currentUserId !== null &&
    Number(currentUserId) === Number(organization?.id);

  const organizationTournaments = useMemo(
    () =>
      (tournaments || [])
        .filter((tournament) => Number(tournament.organizerId) === Number(id))
        .sort((left, right) => {
          const leftDate = left?.startAt ? new Date(left.startAt).getTime() : 0;
          const rightDate = right?.startAt ? new Date(right.startAt).getTime() : 0;
          return rightDate - leftDate;
        }),
    [id, tournaments]
  );

  if (loading) {
    return (
      <>
        <Header />
        <div className="container tournament-page">Loading...</div>
      </>
    );
  }

  if (!organization) {
    return (
      <>
        <Header />
        <div className="container tournament-page">{error || "Not found"}</div>
      </>
    );
  }

  return (
    <>
      <Header />

      <div className="container tournament-page">
        <div className="tournament-header">
          <div>
            <div className="tournament-title">{organization.organizerName}</div>
            <div className="tournament-meta">
              <div>Организатор турниров</div>
              <div>ID: {organization.id}</div>
            </div>
          </div>

          {isOwnOrganization && (
            <div className="tournament-header-actions">
              <button
                className="btn btn-primary"
                onClick={() => navigate("/tournaments/create")}
              >
                Создать турнир
              </button>
              <button
                className="btn btn-secondary"
                onClick={() => navigate("/profile/edit")}
              >
                Редактировать профиль
              </button>
            </div>
          )}
        </div>

        <div className="tournament-layout">
          <div style={{ display: "flex", flexDirection: "column", gap: 15 }}>
            <div className="section">
              <h3>Описание</h3>
              <p>{organization.description || "Описание пока не добавлено"}</p>
            </div>

            <div className="section">
              <div className="subsection-header">
                <h3>Турниры организатора</h3>
                <p>Всего турниров: {organizationTournaments.length}</p>
              </div>

              {!organizationTournaments.length ? (
                <p className="muted-text">Пока что турниров у этого организатора нет.</p>
              ) : (
                <div className="grid">
                  {organizationTournaments.map((tournament) => (
                    <div key={tournament.id} className="card tournament-card">
                      <h3>{tournament.name}</h3>
                      <p>Тип: {translateTournamentType(tournament.type)}</p>
                      <p>Статус: {translateTournamentStatus(tournament.status)}</p>
                      <p className="tournament-date">
                        {formatDate(tournament.startAt)}
                        {tournament.endAt ? ` -> ${formatDate(tournament.endAt)}` : ""}
                      </p>
                      <p className="tournament-desc">
                        {cutText(tournament.description || "Описание пока не добавлено")}
                      </p>

                      <div className="tournament-footer">
                        <button
                          className="btn btn-card-primary"
                          onClick={() => navigate(`/tournaments/${tournament.id}`)}
                        >
                          Подробнее
                        </button>

                        {isOwnOrganization && (
                          <button
                            className="btn btn-secondary"
                            onClick={() => navigate(`/tournaments/${tournament.id}/manage`)}
                          >
                            Управление
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div className="sidebar">
            <div className="section">
              <h3>Инфо</h3>
              <p>Профиль организатора</p>
              <p>Турниров создано: {organizationTournaments.length}</p>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
