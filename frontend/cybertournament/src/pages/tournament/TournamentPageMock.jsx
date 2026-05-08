import { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import Header from "@/shared/ui/Header";
import "@/shared/styles/tournament.css";
import BracketGraph from "@/shared/ui/BracketGraph";
import TeamsSection from "@/shared/ui/TeamsSection";

/**
 * =========================
 * MOCK DATA (FIXED STRUCTURE)
 * =========================
 */
const tournament = {
  id: 102,
  name: "Турик по кске",
  description: "Внутривузовский турнир по CS2",
  minTeams: 2,
  maxTeams: 16,
  type: "PUBLIC",
  status: "RUNNING",
  startAt: "2026-04-22T00:00:00",

  organization: {
    id: 1,
    organizerName: "MIREA_CYBERSPORT",
  },

  game: {
    id: 1,
    name: "Counter-Strike 2",
  },

  teams: [
  {
    "id": 1,
    "gameId": 101,
    "name": "Team Alpha",
    "status": "ACTIVE",
    "type": "PRO",
    "players": [
      { "id": 1, "nickname": "AlphaWolf", "isCaptain": true, "status": "ACTIVE" },
      { "id": 2, "nickname": "Ghost", "isCaptain": false, "status": "ACTIVE" },
      { "id": 3, "nickname": "Reaper", "isCaptain": false, "status": "ACTIVE" },
      { "id": 25, "nickname": "Blitz", "isCaptain": false, "status": "ACTIVE" },
      { "id": 26, "nickname": "Raven", "isCaptain": false, "status": "ACTIVE" }
    ]
  },
  {
    "id": 2,
    "gameId": 101,
    "name": "Team Beta",
    "status": "ACTIVE",
    "type": "PRO",
    "players": [
      { "id": 4, "nickname": "Blaze", "isCaptain": true, "status": "ACTIVE" },
      { "id": 5, "nickname": "SniperX", "isCaptain": false, "status": "ACTIVE" },
      { "id": 6, "nickname": "Vortex", "isCaptain": false, "status": "ACTIVE" },
      { "id": 27, "nickname": "Storm", "isCaptain": false, "status": "ACTIVE" },
      { "id": 28, "nickname": "Falcon", "isCaptain": false, "status": "ACTIVE" }
    ]
  },
  {
    "id": 3,
    "gameId": 101,
    "name": "Team Gamma",
    "status": "ACTIVE",
    "type": "SEMI_PRO",
    "players": [
      { "id": 7, "nickname": "Shadow", "isCaptain": true, "status": "ACTIVE" },
      { "id": 8, "nickname": "NovaX", "isCaptain": false, "status": "ACTIVE" },
      { "id": 9, "nickname": "Drift", "isCaptain": false, "status": "ACTIVE" },
      { "id": 29, "nickname": "Spectre", "isCaptain": false, "status": "ACTIVE" },
      { "id": 30, "nickname": "Pulse", "isCaptain": false, "status": "ACTIVE" }
    ]
  },
  {
    "id": 4,
    "gameId": 101,
    "name": "Team Delta",
    "status": "ACTIVE",
    "type": "SEMI_PRO",
    "players": [
      { "id": 10, "nickname": "Cyclone", "isCaptain": true, "status": "ACTIVE" },
      { "id": 11, "nickname": "Frost", "isCaptain": false, "status": "ACTIVE" },
      { "id": 12, "nickname": "Venom", "isCaptain": false, "status": "ACTIVE" },
      { "id": 31, "nickname": "Blade", "isCaptain": false, "status": "ACTIVE" },
      { "id": 32, "nickname": "Echo", "isCaptain": false, "status": "ACTIVE" }
    ]
  },
  {
    "id": 5,
    "gameId": 101,
    "name": "Team Sigma",
    "status": "ACTIVE",
    "type": "AMATEUR",
    "players": [
      { "id": 13, "nickname": "SigmaCore", "isCaptain": true, "status": "ACTIVE" },
      { "id": 14, "nickname": "Pulse", "isCaptain": false, "status": "ACTIVE" },
      { "id": 15, "nickname": "Echo", "isCaptain": false, "status": "ACTIVE" },
      { "id": 33, "nickname": "Neon", "isCaptain": false, "status": "ACTIVE" },
      { "id": 34, "nickname": "Byte", "isCaptain": false, "status": "ACTIVE" }
    ]
  },
  {
    "id": 6,
    "gameId": 101,
    "name": "Team Omega",
    "status": "ACTIVE",
    "type": "PRO",
    "players": [
      { "id": 16, "nickname": "OmegaKing", "isCaptain": true, "status": "ACTIVE" },
      { "id": 17, "nickname": "Rage", "isCaptain": false, "status": "ACTIVE" },
      { "id": 18, "nickname": "Hunter", "isCaptain": false, "status": "ACTIVE" },
      { "id": 35, "nickname": "Titan", "isCaptain": false, "status": "ACTIVE" },
      { "id": 36, "nickname": "Phantom", "isCaptain": false, "status": "ACTIVE" }
    ]
  },
  {
    "id": 7,
    "gameId": 101,
    "name": "Team Nova",
    "status": "ACTIVE",
    "type": "SEMI_PRO",
    "players": [
      { "id": 19, "nickname": "NovaStar", "isCaptain": true, "status": "ACTIVE" },
      { "id": 20, "nickname": "Flash", "isCaptain": false, "status": "ACTIVE" },
      { "id": 21, "nickname": "Orbit", "isCaptain": false, "status": "ACTIVE" },
      { "id": 37, "nickname": "Comet", "isCaptain": false, "status": "ACTIVE" },
      { "id": 38, "nickname": "Blizzard", "isCaptain": false, "status": "ACTIVE" }
    ]
  },
  {
    "id": 8,
    "gameId": 101,
    "name": "Team Zenith",
    "status": "ACTIVE",
    "type": "PRO",
    "players": [
      { "id": 22, "nickname": "ZenMaster", "isCaptain": true, "status": "ACTIVE" },
      { "id": 23, "nickname": "Skyline", "isCaptain": false, "status": "ACTIVE" },
      { "id": 24, "nickname": "Apex", "isCaptain": false, "status": "ACTIVE" },
      { "id": 39, "nickname": "Vertex", "isCaptain": false, "status": "ACTIVE" },
      { "id": 40, "nickname": "StormEye", "isCaptain": false, "status": "ACTIVE" }
    ]
  }
],

  /**
   * MATCHES (реальные результаты)
   */
  matches: [
    // 1/4 финала
    {
      id: 1,
      status: "FINISHED",
      teams: [
        { id: 1, name: "Team Alpha", result: "WINNER" },
        { id: 2, name: "Team Beta", result: "LOSER" },
      ],
    },
    {
      id: 2,
      status: "FINISHED",
      teams: [
        { id: 3, name: "Team Gamma", result: "LOSER" },
        { id: 4, name: "Team Delta", result: "WINNER" },
      ],
    },
    {
      id: 3,
      status: "FINISHED",
      teams: [
        { id: 5, name: "Team Sigma", result: "WINNER" },
        { id: 6, name: "Team Omega", result: "LOSER" },
      ],
    },
    {
      id: 4,
      status: "RUNNING",
      teams: [
        { id: 7, name: "Team Nova", result: "NOT_PLAYED" },
        { id: 8, name: "Team Zenith", result: "NOT_PLAYED" },
      ],
    },

    // полуфиналы
    {
      id: 5,
      status: "CREATED",
      teams: [
        { id: 1, name: "Team Alpha", result: "NOT_PLAYED" },
        { id: 4, name: "Team Delta", result: "NOT_PLAYED" },
      ],
    },
    {
      id: 6,
      status: "CREATED",
      teams: [
        { id: 5, name: "Team Sigma", result: "NOT_PLAYED" },
        { id: null, name: null, result: "NOT_PLAYED" }, // winner match 4
      ],
    },

    // финал
    {
      id: 7,
      status: "CREATED",
      teams: [
        { id: null, name: null, result: "NOT_PLAYED" }, // winner 5
        { id: null, name: null, result: "NOT_PLAYED" }, // winner 6
      ],
    },
  ],

  /**
   * BRACKET STRUCTURE (ТОЛЬКО ГРАФ, БЕЗ РЕЗУЛЬТАТОВ)
   */
  bracket: {
    version: "1.0.0",
    algorithmType: "SINGLE_ELIMINATION",
    bracketGroups: [
      {
        name: "Плей-офф",
        matches: [
          // ROUND 1
          {
            matchId: 1,
            slots: [
              { teamId: 1, refMatchId: null },
              { teamId: 2, refMatchId: null },
            ],
          },
          {
            matchId: 2,
            slots: [
              { teamId: 3, refMatchId: null },
              { teamId: 4, refMatchId: null },
            ],
          },
          {
            matchId: 3,
            slots: [
              { teamId: 5, refMatchId: null },
              { teamId: 6, refMatchId: null },
            ],
          },
          {
            matchId: 4,
            slots: [
              { teamId: 7, refMatchId: null },
              { teamId: 8, refMatchId: null },
            ],
          },

          // ROUND 2
          {
            matchId: 5,
            slots: [
              { teamId: null, refMatchId: 1, matchTeamResult: "WINNER" },
              { teamId: null, refMatchId: 2, matchTeamResult: "WINNER" },
            ],
          },
          {
            matchId: 6,
            slots: [
              { teamId: null, refMatchId: 3, matchTeamResult: "WINNER" },
              { teamId: null, refMatchId: 4, matchTeamResult: "WINNER" },
            ],
          },

          // FINAL
          {
            matchId: 7,
            slots: [
              { teamId: null, refMatchId: 5, matchTeamResult: "WINNER" },
              { teamId: null, refMatchId: 6, matchTeamResult: "WINNER" },
            ],
          },
        ],
      },
    ],
  },
};

const formatDate = (d) =>
  new Date(d).toLocaleDateString("ru-RU", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });

export default function TournamentPageMock() {
  const navigate = useNavigate();

  const teamMap = useMemo(() => {
    const map = new Map();
    tournament.teams.forEach(t => map.set(t.id, t.name));
    return map;
  }, []);

  return (
    <>
      <Header />

      <div className="container tournament-page">

        {/* HEADER */}
        <div className="tournament-header">
          <div>
            <div className="tournament-title">{tournament.name}</div>

            <div className="tournament-meta">
              <div>🎮 {tournament.game.name}</div>
              <div>🏢 {tournament.organization.organizerName}</div>
              <div>📅 {formatDate(tournament.startAt)}</div>
            </div>
          </div>
        </div>

        {/* LAYOUT */}
        <div className="tournament-layout">

          {/* LEFT */}
            <div style={{ display: "flex", flexDirection: "column", gap: 15 }}>

            <div className="section">
                <h3>Описание</h3>
                <p>{tournament.description}</p>
            </div>

            {/* =========================
                BRACKET SECTION
            ========================= */}
            <div className="section">
                <h3>Турнирная сетка</h3>

                <BracketGraph
                    bracketGroups={tournament.bracket.bracketGroups}
                    matches={tournament.matches}
                />
            </div>

            {/* ✅ НОВЫЙ БЛОК КОМАНД */}
                <TeamsSection
                teams={tournament.teams}
                onTeamClick={(id) => navigate(`/teams/${id}`)}
                />

            </div>

          {/* RIGHT */}
          <div className="sidebar">

            <div className="section">
              <h3>Инфо</h3>
              <p>Тип: {tournament.type}</p>
              <p>Статус: {tournament.status}</p>
              <p>
                Команд: {tournament.minTeams} - {tournament.maxTeams}
              </p>
            </div>

            <div className="section">
              <button
                className="btn btn-primary"
                onClick={() => console.log("JOIN MOCK")}
              >
                Подать заявку
              </button>
            </div>

          </div>

        </div>
      </div>
    </>
  );
}