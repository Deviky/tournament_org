import { translateTournamentType } from "@/shared/lib/enumLabels";

const tournamentTypes = ["PUBLIC", "PRIVATE"];

export default function TournamentEditorForm({
  title,
  description,
  form,
  games,
  readOnlyFields = [],
  error = "",
  submitting = false,
  submitLabel,
  onChange,
  onCancel,
  onSubmit,
}) {
  const isReadOnly = (field) => readOnlyFields.includes(field);

  return (
    <form className="team-panel team-form" onSubmit={onSubmit}>
      {title && <h3>{title}</h3>}

      <div className="team-form-grid">
        <label>
          Название турнира
          <input
            value={form.name}
            onChange={(event) => onChange("name", event.target.value)}
            placeholder="Например, Весенний кубок"
            minLength={2}
            maxLength={120}
            disabled={isReadOnly("name")}
            required
          />
        </label>

        <label>
          Игра
          <select
            value={form.gameId}
            onChange={(event) => onChange("gameId", event.target.value)}
            disabled={isReadOnly("gameId")}
            required
          >
            {games.map((game) => (
              <option key={game.id} value={game.id}>
                {game.name}
              </option>
            ))}
          </select>
        </label>

        <label>
          Минимум команд
          <input
            type="number"
            min="2"
            max="1024"
            value={form.minTeams}
            onChange={(event) => onChange("minTeams", event.target.value)}
            disabled={isReadOnly("minTeams")}
            required
          />
        </label>

        <label>
          Максимум команд
          <input
            type="number"
            min={form.minTeams || 2}
            max="1024"
            value={form.maxTeams}
            onChange={(event) => onChange("maxTeams", event.target.value)}
            disabled={isReadOnly("maxTeams")}
            required
          />
        </label>

        <label>
          Начало турнира
          <input
            type="datetime-local"
            value={form.startAt}
            onChange={(event) => onChange("startAt", event.target.value)}
            disabled={isReadOnly("startAt")}
            required
          />
        </label>

        <label>
          Окончание турнира
          <input
            type="datetime-local"
            value={form.endAt}
            onChange={(event) => onChange("endAt", event.target.value)}
            disabled={isReadOnly("endAt")}
          />
        </label>
      </div>

      <label>
        Описание
        <textarea
          value={form.description}
          onChange={(event) => onChange("description", event.target.value)}
          placeholder="Расскажи о формате турнира, требованиях к участникам и важных правилах."
          rows={5}
          disabled={isReadOnly("description")}
          required
        />
      </label>

      <div className="team-type-grid">
        {tournamentTypes.map((type) => {
          const active = form.type === type;

          return (
            <button
              key={type}
              type="button"
              className={`team-type-card ${active ? "active" : ""}`}
              onClick={() => onChange("type", type)}
              disabled={isReadOnly("type")}
            >
              <strong>{translateTournamentType(type)}</strong>
              <span>
                {type === "PUBLIC"
                  ? "Команды смогут сами подать заявку на участие, а ты будешь принимать участников."
                  : "Подать заявку смогут только команды с кодом приглашения от организатора."}
              </span>
            </button>
          );
        })}
      </div>

      {description && <div className="team-note">{description}</div>}
      {error && <div className="team-feedback error">{error}</div>}

      <div className="team-actions-row">
        <button className="btn btn-primary" type="submit" disabled={submitting}>
          {submitting ? "Сохраняем..." : submitLabel}
        </button>

        <button className="btn btn-secondary" type="button" onClick={onCancel}>
          Отмена
        </button>
      </div>
    </form>
  );
}
