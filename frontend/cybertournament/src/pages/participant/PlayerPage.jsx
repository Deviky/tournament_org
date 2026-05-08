import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "@/shared/ui/Header";
import { gameApi } from "@/shared/api/gameApi";
import { playerApi } from "@/shared/api/playerApi";
import { getErrorMessage } from "@/shared/api/client";
import { useAuthStore } from "@/app/store/authStore";
import { translateTeamStatus, translateTeamType } from "@/shared/lib/enumLabels";
import "@/shared/styles/player-page.css";

export default function PlayerPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const currentRole = useAuthStore((s) => s.currentRole);
  const currentUserId = useAuthStore((s) => s.currentUserId);

  const [player, setPlayer] = useState(null);
  const [games, setGames] = useState([]);
  const [selectedGameId, setSelectedGameId] = useState(null);
  const [openPlatforms, setOpenPlatforms] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        setError("");

        const [gamesData, playerData] = await Promise.all([
          gameApi.getAll(),
          playerApi.getById(id),
        ]);

        setGames(gamesData || []);
        setPlayer(playerData || null);

        const nextGameId =
          playerData?.statistics?.[0]?.gameId ??
          playerData?.games?.[0]?.gameId ??
          playerData?.teams?.[0]?.gameId ??
          gamesData?.[0]?.id ??
          null;

        setSelectedGameId(nextGameId);
      } catch (err) {
        setError(getErrorMessage(err, "Не удалось загрузить профиль игрока"));
        setPlayer(null);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [id]);

  const currentStats = useMemo(
    () => player?.statistics?.find((item) => item.gameId === selectedGameId),
    [player, selectedGameId]
  );

  const currentTeams = useMemo(
    () => player?.teams?.filter((team) => team.gameId === selectedGameId) || [],
    [player, selectedGameId]
  );

  const currentGameLinks = useMemo(
    () => player?.games?.find((game) => game.gameId === selectedGameId)?.links || {},
    [player, selectedGameId]
  );

  const togglePlatform = (idx) => {
    setOpenPlatforms((prev) => ({
      ...prev,
      [idx]: !prev[idx],
    }));
  };

  const avatarLetters = (player?.nickname || "?").slice(0, 2).toUpperCase();
  const isOwnProfile =
    currentRole === "PLAYER" &&
    currentUserId !== null &&
    Number(currentUserId) === Number(player?.id);

  if (loading) {
    return (
      <>
        <Header />
        <div className="page">
          <div className="container">Loading...</div>
        </div>
      </>
    );
  }

  if (!player) {
    return (
      <>
        <Header />
        <div className="page">
          <div className="container">{error || "Player not found"}</div>
        </div>
      </>
    );
  }

  return (
    <>
      <Header />

      <div className="page">
        <div className="container">
          <div className="profileCard">
            <div className="avatar">{avatarLetters}</div>

            <div>
              <h1 className="title">{player.nickname}</h1>
              <div className="subText">Игрок</div>
            </div>

            {isOwnProfile && (
              <button
                className="btn btn-secondary"
                style={{ marginLeft: "auto", width: "auto" }}
                onClick={() => navigate("/profile/edit")}
              >
                Редактировать профиль
              </button>
            )}
          </div>

          <div className="games">
            {(games || []).map((game) => (
              <button
                key={game.id}
                className={`gameBtn ${selectedGameId === game.id ? "active" : ""}`}
                onClick={() => setSelectedGameId(game.id)}
              >
                {game.name}
              </button>
            ))}
          </div>

          <div className="card small">
            <h2>Ссылки</h2>
            <div className="linksCompact">
              {Object.entries(currentGameLinks).length === 0 ? (
                <div>Нет ссылок для этой игры</div>
              ) : (
                Object.entries(currentGameLinks).map(([key, value]) => (
                  <a key={key} href={value} target="_blank" rel="noreferrer">
                    {key}
                  </a>
                ))
              )}
            </div>
          </div>

          <div className="card">
            <h2>Команды</h2>

            {currentTeams.length === 0 ? (
              <div>Нет команд</div>
            ) : (
              currentTeams.map((team) => (
                <div
                  key={team.id}
                  className="teamCard"
                  onClick={() => navigate(`/teams/${team.id}`)}
                >
                  <div>
                    <div className="teamName">{team.name}</div>
                    <div className="teamMeta">
                      {translateTeamType(team.type)} · {translateTeamStatus(team.status)}
                    </div>
                  </div>

                  {team.status && (
                    <span className="teamBadge">{translateTeamStatus(team.status)}</span>
                  )}
                </div>
              ))
            )}
          </div>

          <div className="card">
            <h2>Статистика</h2>

            {!currentStats ? (
              <div>Нет данных</div>
            ) : (
              currentStats.platformStatistics.map((platform, idx) => (
                <div key={idx} className="platform">
                  <div className="platformHeader" onClick={() => togglePlatform(idx)}>
                    <h3>{platform.platform || `Платформа ${idx + 1}`}</h3>
                    <span>{openPlatforms[idx] ? "▲" : "▼"}</span>
                  </div>

                  {openPlatforms[idx] && (
                    <div className="statsGrid">
                      {Object.entries(platform)
                        .filter(([key]) => key !== "platform")
                        .map(([key, value]) => (
                          <div key={key} className="statBox">
                            <div className="statLabel">{key}</div>
                            <div className="statValue">{String(value)}</div>
                          </div>
                        ))}
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </>
  );
}
