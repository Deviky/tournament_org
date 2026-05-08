export const GAME_FIELDS_CONFIG = {
  CS2: [
    { key: "STEAM", label: "Steam URL", required: true },
    { key: "FACEIT", label: "Faceit URL", required: false },
  ],
  DOTA2: [
    { key: "STEAM", label: "Steam URL", required: true },
    { key: "DOTA_ID", label: "Dota ID", required: true },
    { key: "DOTABUFF", label: "Dotabuff URL", required: false },
  ],
};

export const normalizeGameName = (value) =>
  String(value || "")
    .trim()
    .toLowerCase()
    .replace(/\s+/g, " ");

export const getGameConfigKey = (gameName) => {
  const normalizedName = normalizeGameName(gameName);

  if (["cs2", "counter-strike 2", "counter strike 2"].includes(normalizedName)) {
    return "CS2";
  }

  if (["dota 2", "dota2"].includes(normalizedName)) {
    return "DOTA2";
  }

  return null;
};

export const getGameConfigById = (gameId, availableGames) => {
  const game = (availableGames || []).find((item) => String(item.id) === String(gameId));
  if (!game) {
    return null;
  }

  return GAME_FIELDS_CONFIG[getGameConfigKey(game.name)] || null;
};

export const createEditablePlayerGame = (game = {}) => ({
  id: crypto.randomUUID(),
  gameId: game?.gameId ? String(game.gameId) : "",
  links: { ...(game?.links || {}) },
});
