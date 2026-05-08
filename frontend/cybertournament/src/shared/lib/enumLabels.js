export const tournamentStatusLabels = {
  CREATED: "Скоро",
  REGISTRATION: "Идёт регистрация",
  REGISTRATION_CLOSED: "Регистрация закрыта",
  BRACKET_CREATED: "Сетка подготовлена",
  RUNNING: "Проводится",
  FINISHED: "Завершён",
  CANCEL: "Отменён",
  BANNED: "Заблокирован",
};

export const tournamentTypeLabels = {
  PUBLIC: "Публичный",
  PRIVATE: "Приватный",
};

export const teamStatusLabels = {
  ACTIVE: "Активна",
  INACTIVE: "Неактивна",
  DELETED: "Удалена",
};

export const teamTypeLabels = {
  PUBLIC: "Открытая",
  PRIVATE: "Закрытая",
};

export const teamPlayerStatusLabels = {
  ACTIVE: "Активен",
  INVITED: "Приглашён",
  REQUESTED: "Ожидает одобрения",
  LEAVED: "Покинул команду",
  KICKED: "Исключён",
  CANCELED: "Отклонён",
};

export const tournamentTeamStatusLabels = {
  WAITING_APPROVE: "Ожидает одобрения",
  REGISTERED: "Зарегистрирована",
  KICKED: "Исключена",
  LEAVED: "Покинула турнир",
};

export const matchStatusLabels = {
  COMING: "Скоро",
  RUNNING: "Идёт",
  FINISHED: "Завершён",
  CANCELED: "Отменён",
};

export const matchResultLabels = {
  NOT_PLAYED: "Не сыгран",
  WINNER: "Победа",
  LOSER: "Поражение",
  DRAW: "Ничья",
};

export const translateEnum = (value, map, fallback = "Не указано") => {
  if (!value) return fallback;
  return map[value] || value;
};

export const translateTournamentStatus = (value) =>
  translateEnum(value, tournamentStatusLabels);

export const translateTournamentType = (value) =>
  translateEnum(value, tournamentTypeLabels);

export const translateTeamStatus = (value) =>
  translateEnum(value, teamStatusLabels);

export const translateTeamType = (value) =>
  translateEnum(value, teamTypeLabels);

export const translateMatchStatus = (value) =>
  translateEnum(value, matchStatusLabels);

export const translateMatchResult = (value) =>
  translateEnum(value, matchResultLabels);

export const translateTeamPlayerStatus = (value) =>
  translateEnum(value, teamPlayerStatusLabels);

export const translateTournamentTeamStatus = (value) =>
  translateEnum(value, tournamentTeamStatusLabels);
