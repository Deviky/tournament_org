import { api, unwrap } from "./client";

export const organizationApi = {
  create: (data) =>
    unwrap(api.post("/api/participant/organizations/private/create", data)),

  update: (data, config) =>
    unwrap(api.put("/api/participant/organizations/private/update", data, config)),

  getById: (organizationId) =>
    unwrap(api.get(`/api/participant/organizations/public/get/${organizationId}`)),

  getAll: () =>
    unwrap(api.get("/api/participant/organizations/public/get_all")),
};
