import { api, unwrap, withUserId } from "./client";

const normalizeTeamPlayer = (player) => ({
  ...player,
  isCaptain: Boolean(player?.isCaptain ?? player?.captain),
});

const normalizeTeam = (team) => ({
  ...team,
  players: Array.isArray(team?.players) ? team.players.map(normalizeTeamPlayer) : [],
});

const normalizeTeamPlayers = (players) =>
  Array.isArray(players) ? players.map(normalizeTeamPlayer) : [];

export const teamApi = {
  create: (userId, data) =>
    unwrap(api.post("/api/participant/teams/player/create", data, withUserId(userId))).then(normalizeTeam),

  getAll: () =>
    unwrap(api.get("/api/participant/teams/public/get_all")).then((teams) =>
      Array.isArray(teams) ? teams.map(normalizeTeam) : []
    ),

  search: async (query = "") => {
    if (!query?.trim()) {
      return teamApi.getAll();
    }

    const searchResult = await unwrap(
      api.get("/api/participant/players/public/search", { params: { query: query.trim() } })
    );

    return Array.isArray(searchResult?.teams) ? searchResult.teams.map(normalizeTeam) : [];
  },

  join: (userId, teamId) =>
    unwrap(api.post(`/api/participant/teams/player/join/${teamId}`, null, withUserId(userId))).then(normalizeTeam),

  handleRequest: (userId, teamId, playerId, approve) =>
    unwrap(
      api.post(
        `/api/participant/teams/player/request/${teamId}`,
        null,
        withUserId(userId, { params: { playerId, approve } })
      )
    ).then(normalizeTeam),

  getPendingRequests: (userId, teamId) =>
    unwrap(
      api.get(`/api/participant/teams/player/pending_requests/${teamId}`, withUserId(userId))
    ).then(normalizeTeamPlayers),

  generateInvite: (userId, teamId) =>
    api.post(`/api/participant/teams/player/invite_generate/${teamId}`, null, withUserId(userId)).then((response) => {
      if (typeof response.data === "string") {
        return response.data;
      }

      if (typeof response.data?.data === "string") {
        return response.data.data;
      }

      return "";
    }),

  joinByToken: (userId, token) =>
    unwrap(
      api.post(
        "/api/participant/teams/player/join_by_token",
        null,
        withUserId(userId, { params: { token } })
      )
    ).then(normalizeTeam),

  leave: (userId, teamId) =>
    unwrap(api.delete(`/api/participant/teams/player/leave/${teamId}`, withUserId(userId))).then(normalizeTeam),

  removePlayer: (userId, teamId, playerId) =>
    unwrap(
      api.delete(
        `/api/participant/teams/player/remove/${teamId}`,
        withUserId(userId, { params: { playerId } })
      )
    ).then(normalizeTeam),

  transferCaptain: (userId, teamId, newCaptainId) =>
    unwrap(
      api.post(
        `/api/participant/teams/player/transfer_captain/${teamId}`,
        null,
        withUserId(userId, { params: { newCaptainId } })
      )
    ).then(normalizeTeam),

  updateStatus: (userId, teamId, status) =>
    unwrap(
      api.patch(
        `/api/participant/teams/player/status/${teamId}`,
        null,
        withUserId(userId, { params: { status } })
      )
    ).then(normalizeTeam),

  getById: (teamId) =>
    unwrap(api.get(`/api/participant/teams/public/get/${teamId}`)).then(normalizeTeam),

  getByIds: (teamIds) =>
    unwrap(api.get("/api/participant/teams/public/get_by_ids", { params: { teamIds } })).then((teams) =>
      Array.isArray(teams) ? teams.map(normalizeTeam) : []
    ),
};
