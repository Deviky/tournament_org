import { organizationApi } from "./organizationApi";
import { playerApi } from "./playerApi";
import { teamApi } from "./teamApi";

export const participantApi = {
  getAllOrganizators: organizationApi.getAll,
  getOrganizationById: organizationApi.getById,
  createOrganization: organizationApi.create,
  getPlayerById: playerApi.getById,
  createPlayer: playerApi.create,
  searchPlayers: playerApi.search,
  getTeamById: teamApi.getById,
  getTeamsByIds: teamApi.getByIds,
};
