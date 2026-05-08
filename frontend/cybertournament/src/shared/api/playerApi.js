import { api, unwrap } from "./client";

export const playerApi = {
  create: (data) =>
    unwrap(api.post("/api/participant/players/private/create", data)),

  update: (data, config) =>
    unwrap(api.put("/api/participant/players/private/update", data, config)),

  getById: (playerId) =>
    unwrap(api.get(`/api/participant/players/public/${playerId}`)),

  search: (query) =>
    unwrap(api.get("/api/participant/players/public/search", { params: { query } })),
};
