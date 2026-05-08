import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "@/shared/styles/teams-section.css";

export default function TeamsSection({ teams, onTeamClick }) {
  const navigate = useNavigate();

  const [isOpen, setIsOpen] = useState(false);
  const [openTeamId, setOpenTeamId] = useState(null);

  const toggleTeamList = () => {
    setIsOpen(prev => !prev);
  };

  const toggleTeam = (id) => {
    setOpenTeamId(prev => (prev === id ? null : id));
  };

  return (
    <div className="section">

      {/* GLOBAL HEADER */}
      <div className="teams-section-header" onClick={toggleTeamList}>
        <h3 className="teams-title">Команды</h3>
        <span className="teams-section-arrow">
          {isOpen ? "▲" : "▼"}
        </span>
      </div>

      {/* BODY */}
      {isOpen && (
        <div className="teams-accordion">

          {teams.map(team => {
            const isTeamOpen = openTeamId === team.id;

            return (
              <div key={team.id} className="team-card">

                {/* TEAM HEADER */}
                <div
                  className="team-header"
                  onClick={() => toggleTeam(team.id)}
                >
                  <span
                    className="team-name"
                    onClick={(e) => {
                      e.stopPropagation();
                      onTeamClick(team.id);
                    }}
                  >
                    {team.name}
                  </span>

                  <span className="team-arrow">
                    {isTeamOpen ? "▲" : "▼"}
                  </span>
                </div>

                {/* PLAYERS */}
                {isTeamOpen && (
                  <div className="team-players">
                    {team.players.map(p => (
                      <div
                        key={p.id}
                        className="player-card"
                        onClick={() => navigate(`/players/${p.id}`)}  // 🔥 переход на профиль
                      >

                        <div className="player-avatar-wrapper">
                          {p.isCaptain && (
                            <div className="captain-crown">👑</div>
                          )}

                          <img
                            className="player-avatar"
                            src={`https://api.dicebear.com/7.x/identicon/svg?seed=${p.nickname}`}
                            alt={p.nickname}
                          />
                        </div>

                        <div className="player-name">
                          {p.nickname}
                        </div>

                      </div>
                    ))}
                  </div>
                )}

              </div>
            );
          })}

        </div>
      )}

    </div>
  );
}