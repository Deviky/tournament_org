export const toDateTimeLocalValue = (value) => {
  if (!value) return "";

  const normalized = String(value).replace(" ", "T");
  return normalized.length >= 16 ? normalized.slice(0, 16) : normalized;
};

export const toApiDateTime = (value) => (value ? value : null);

export const buildTournamentPayload = (form, options = {}) => {
  const includeImmutable = options.includeImmutable ?? true;

  const basePayload = {
    description: form.description.trim(),
    minTeams: Number(form.minTeams),
    maxTeams: Number(form.maxTeams),
    type: form.type,
    startAt: toApiDateTime(form.startAt),
    endAt: toApiDateTime(form.endAt),
  };

  if (!includeImmutable) {
    return basePayload;
  }

  return {
    ...basePayload,
    gameId: Number(form.gameId),
    name: form.name.trim(),
  };
};

export const getAlgorithmConfigEntries = (config) =>
  Object.entries(config || {}).map(([fieldName, descriptor]) => [
    fieldName,
    {
      ...descriptor,
      name: descriptor?.name || fieldName,
    },
  ]);

export const getAlgorithmDefaultValues = (config) =>
  getAlgorithmConfigEntries(config).reduce((acc, [fieldName, descriptor]) => {
    const rawDefault = descriptor?.defaultValue;
    const type = String(descriptor?.type || "").toLowerCase();

    if (type === "boolean") {
      acc[fieldName] = rawDefault === true || rawDefault === "true";
      return acc;
    }

    acc[fieldName] = rawDefault ?? "";
    return acc;
  }, {});

export const coerceAlgorithmValue = (descriptor, value) => {
  const type = String(descriptor?.type || "").toLowerCase();

  if (type === "boolean") {
    return value === true || value === "true";
  }

  if (["int", "integer", "long", "double", "float", "number"].includes(type)) {
    if (value === "" || value === null || value === undefined) {
      return null;
    }

    return Number(value);
  }

  if (value === "" || value === undefined) {
    return null;
  }

  return value;
};

export const buildAlgorithmParamsPayload = (config, values) =>
  getAlgorithmConfigEntries(config).reduce((acc, [fieldName, descriptor]) => {
    const coercedValue = coerceAlgorithmValue(descriptor, values[fieldName]);

    if (coercedValue !== null) {
      acc[fieldName] = coercedValue;
    }

    return acc;
  }, {});

export const createBracketPreviewMatches = (bracket, entries) => {
  const teamNameById = new Map(
    (entries || [])
      .filter((entry) => entry?.teamId)
      .map((entry) => [Number(entry.teamId), entry.team?.name || `Команда #${entry.teamId}`])
  );

  return (bracket?.bracketGroups || []).flatMap((group) =>
    (group.matches || []).map((match) => ({
      id: match.matchId,
      status: "COMING",
      teams: (match.slots || []).map((slot) => ({
        id: slot.teamId ?? null,
        name: slot.teamId ? teamNameById.get(Number(slot.teamId)) || `Команда #${slot.teamId}` : null,
        result: "NOT_PLAYED",
      })),
    }))
  );
};

export const sortTournamentEntries = (entries) => {
  const statusWeight = {
    WAITING_APPROVE: 0,
    REGISTERED: 1,
    LEAVED: 2,
    KICKED: 3,
  };

  return [...(entries || [])].sort((left, right) => {
    const statusDiff =
      (statusWeight[left?.status] ?? 99) - (statusWeight[right?.status] ?? 99);

    if (statusDiff !== 0) {
      return statusDiff;
    }

    return String(left?.team?.name || "").localeCompare(String(right?.team?.name || ""), "ru");
  });
};

export const canEditTournament = (status) =>
  status === "CREATED" || status === "REGISTRATION";

export const canManageTournamentRequests = (status) =>
  status === "REGISTRATION" || status === "REGISTRATION_CLOSED";
