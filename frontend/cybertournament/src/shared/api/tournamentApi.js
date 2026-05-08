import { api, unwrap, withUserId } from "./client";

export const tournamentApi = {
  getAlgorithms: (userId, gameId) =>
    unwrap(api.get(`/api/tournaments/organizer/games/${gameId}/algorithms`, withUserId(userId))),

  create: (userId, data) =>
    unwrap(api.post("/api/tournaments/organizer/create", data, withUserId(userId))),

  update: (userId, tournamentId, data) =>
    unwrap(api.put(`/api/tournaments/organizer/update/${tournamentId}`, data, withUserId(userId))),

  getAll: () => unwrap(api.get("/api/tournaments/public/get_all")),

  getById: (id) => unwrap(api.get(`/api/tournaments/public/get/${id}`)),

  getTeamEntries: (userId, tournamentId) =>
    unwrap(api.get(`/api/tournaments/organizer/teams/${tournamentId}`, withUserId(userId))),

  getByGame: (gameId) =>
    unwrap(api.get(`/api/tournaments/public/get_by_game/${gameId}`)),

  getByGames: (gameIds) =>
    unwrap(api.post("/api/tournaments/public/get_by_games", gameIds)),

  generateBracket: (userId, tournamentId, data) =>
    unwrap(
      api.post(
        `/api/tournaments/organizer/bracket/generate/${tournamentId}`,
        data,
        withUserId(userId)
      )
    ),

  submitFinalBracket: (userId, tournamentId, bracket) =>
    unwrap(
      api.post(
        `/api/tournaments/organizer/bracket/final/${tournamentId}`,
        bracket,
        withUserId(userId)
      )
    ),

  updateBracket: (tournamentId, matchResult) =>
    unwrap(api.post(`/api/tournaments/organizer/bracket/update/${tournamentId}`, matchResult)),

  cancelMatchUpdateBracket: (tournamentId, matchId) =>
    unwrap(
      api.post(`/api/tournaments/private/bracket/match_cancel/${tournamentId}`, null, {
        params: { matchId },
      })
    ),

  registerTeam: (userId, tournamentId, teamId, inviteToken) =>
    unwrap(
      api.post(
        `/api/tournaments/player/register/${tournamentId}`,
        null,
        withUserId(userId, { params: { teamId, inviteToken } })
      )
    ),

  generateInvite: (userId, tournamentId) =>
    unwrap(api.post(`/api/tournaments/organizer/invite/${tournamentId}`, null, withUserId(userId))),

  leaveTeam: (userId, tournamentId, teamId) =>
    unwrap(
      api.delete(
        `/api/tournaments/player/leave/${tournamentId}/teams/${teamId}`,
        withUserId(userId)
      )
    ),

  kickTeam: (userId, tournamentId, teamId) =>
    unwrap(
      api.delete(
        `/api/tournaments/organizer/kick/${tournamentId}/teams/${teamId}`,
        withUserId(userId)
      )
    ),

  handleRequest: (userId, tournamentId, teamId, approve) =>
    unwrap(
      api.post(
        `/api/tournaments/organizer/handle/${tournamentId}/teams/${teamId}`,
        null,
        withUserId(userId, { params: { approve } })
      )
    ),

  startRegistration: (userId, tournamentId) =>
    unwrap(
      api.post(
        `/api/tournaments/organizer/start_registration/${tournamentId}`,
        null,
        withUserId(userId)
      )
    ),

  closeRegistration: (userId, tournamentId) =>
    unwrap(
      api.post(
        `/api/tournaments/organizer/close_registration/${tournamentId}`,
        null,
        withUserId(userId)
      )
    ),

  start: (userId, tournamentId) =>
    unwrap(api.post(`/api/tournaments/organizer/start/${tournamentId}`, null, withUserId(userId))),

  end: (userId, tournamentId) =>
    unwrap(api.post(`/api/tournaments/organizer/end/${tournamentId}`, null, withUserId(userId))),

  ban: (tournamentId) =>
    unwrap(api.post(`/api/tournaments/moderator/ban/${tournamentId}`)),

  cancel: (userId, tournamentId) =>
    unwrap(api.post(`/api/tournaments/organizer/cancel/${tournamentId}`, null, withUserId(userId))),

  checkCreateMatch: (tournamentId, organizerId) =>
    unwrap(api.get(`/api/tournaments/private/create_match_check/${tournamentId}`, {
      params: { organizerId },
    })),
};
