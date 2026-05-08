import { api, unwrap, withPlayerId } from "./client";

export const notificationApi = {
  getAll: (playerId) =>
    unwrap(api.get("/api/notification/get-notifications", withPlayerId(playerId))),

  setRead: (notificationIds) =>
    unwrap(api.post("/api/notification/set-notifications-read", notificationIds)),
};
