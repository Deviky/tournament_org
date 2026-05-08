import { api, unwrap } from "./client";

export const integrationApi = {
  getPlayerStatistic: (links) =>
    unwrap(api.post("/api/integrator/get_player_statistic", links)),
};
