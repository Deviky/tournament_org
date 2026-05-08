import { api, unwrap, withUserId } from "./client";

export const matchApi = {
  createByBatch: (matchesMap) =>
    unwrap(api.post("/api/matches/private/create_by_batch", matchesMap)),

  getById: (matchId) =>
    unwrap(api.get(`/api/matches/public/get/${matchId}`)),

  getByTournament: (tournamentId) =>
    unwrap(api.get(`/api/matches/public/get_by_tournament/${tournamentId}`)),

  update: (userId, data) =>
    unwrap(api.put("/api/matches/organizer/update", data, withUserId(userId))),

  start: (userId, matchId) =>
    unwrap(api.post(`/api/matches/organizer/start/${matchId}`, null, withUserId(userId))),

  finish: (userId, data) =>
    unwrap(api.post("/api/matches/organizer/finish", data, withUserId(userId))),

  cancel: (userId, matchId) =>
    unwrap(api.post(`/api/matches/organizer/cancel/${matchId}`, null, withUserId(userId))),

  cancelByTournament: (tournamentId) =>
    unwrap(api.post(`/api/matches/private/by_tournament/cancel/${tournamentId}`)),
};
