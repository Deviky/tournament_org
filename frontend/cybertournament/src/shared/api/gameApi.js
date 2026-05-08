import { api, unwrap } from "./client";

const normalizeGame = (game) => ({
  id: game?.id,
  name: game?.name,
  description: game?.description,
});

export const gameApi = {
  getById: (gameId) => unwrap(api.get(`/api/game/public/get/${gameId}`)),
  getAll: async () => {
    const games = await unwrap(api.get("/api/game/public/get_all"));
    return Array.isArray(games) ? games.map(normalizeGame) : [];
  },
  checkPlayer: (data) => unwrap(api.post("/api/game/private/check_player", data)),
  checkTeam: (data) => unwrap(api.post("/api/game/private/check_team", data)),
  checkTournamentCreate: (data) =>
    unwrap(api.post("/api/game/private/check_tournament_create", data)),
  checkTournamentStart: (data) =>
    unwrap(api.post("/api/game/private/check_tournament_start", data)),
  getBracketAlgorithms: (gameId) =>
    unwrap(api.get(`/api/game/private/get_bracket_algorithms/${gameId}`)),
};
