export const isCaptainFlag = (player) =>
  Boolean(player?.isCaptain ?? player?.captain);

export const findTeamPlayer = (team, playerId) =>
  team?.players?.find((player) => Number(player.id) === Number(playerId)) || null;

export const getTeamCaptain = (team) =>
  team?.players?.find((player) => isCaptainFlag(player)) || null;

export const isTeamMember = (team, playerId) => !!findTeamPlayer(team, playerId);

export const isTeamCaptain = (team, playerId) => isCaptainFlag(findTeamPlayer(team, playerId));

export const sortTeamPlayers = (players = []) =>
  [...players].sort((left, right) => {
    const leftIsCaptain = isCaptainFlag(left);
    const rightIsCaptain = isCaptainFlag(right);

    if (leftIsCaptain !== rightIsCaptain) {
      return Number(rightIsCaptain) - Number(leftIsCaptain);
    }

    return (left.nickname || "").localeCompare(right.nickname || "", "ru");
  });
