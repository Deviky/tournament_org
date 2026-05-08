import { useLayoutEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  translateMatchResult,
  translateMatchStatus,
} from "@/shared/lib/enumLabels";
import "@/shared/styles/bracket.css";

export default function BracketGraph({
  bracketGroups,
  matches,
  allowMatchNavigation = true,
  allowTeamNavigation = true,
}) {
  const [activeTeamId, setActiveTeamId] = useState(null);
  const navigate = useNavigate();

  const matchMap = useMemo(() => {
    const map = new Map();
    matches.forEach((match) => map.set(match.id, match));
    return map;
  }, [matches]);

  const findBracketMatch = (id, group) =>
    group.matches.find((match) => match.matchId === id);

  const buildLevels = (group) => {
    const cache = new Map();

    const getLevel = (matchId) => {
      if (cache.has(matchId)) return cache.get(matchId);

      const bracketMatch = findBracketMatch(matchId, group);
      if (!bracketMatch) return 0;

      const refs = bracketMatch.slots.map((slot) => slot.refMatchId).filter(Boolean);

      if (!refs.length) {
        cache.set(matchId, 0);
        return 0;
      }

      const level = Math.max(...refs.map(getLevel)) + 1;
      cache.set(matchId, level);
      return level;
    };

    group.matches.forEach((match) => getLevel(match.matchId));

    const grouped = new Map();

    group.matches.forEach((match) => {
      const level = cache.get(match.matchId) || 0;
      if (!grouped.has(level)) grouped.set(level, []);
      grouped.get(level).push(match);
    });

    return [...grouped.entries()]
      .sort((a, b) => a[0] - b[0])
      .map(([, value]) => value);
  };

  const resolveSlot = (slot, match, slotIndex) => {
    const isMatchFinished = match?.status === "FINISHED";

    if (match?.teams && match.teams.length === 2) {
      const team = match.teams[slotIndex];

      if (team && team.id) {
        return {
          id: team.id,
          name: team.name,
          result: isMatchFinished ? team.result : null,
          isReal: true,
        };
      }
    }

    if (slot.refMatchId) {
      const ref = matchMap.get(slot.refMatchId);
      const winner = ref?.teams?.find((team) => team.result === "WINNER");

      return {
        id: winner?.id || null,
        name: winner?.name || "TBD",
        result: null,
        isReal: false,
      };
    }

    return {
      id: null,
      name: "TBD",
      result: null,
      isReal: false,
    };
  };

  const isActive = (slot) => activeTeamId && slot?.id && slot.id === activeTeamId;

  const getStatusClass = (slot) => {
    if (!slot.result) return "";

    switch (slot.result) {
      case "WINNER":
        return "winner";
      case "LOSER":
        return "loser";
      case "DRAW":
        return "draw";
      default:
        return "";
    }
  };

  const getSlotClass = (slot) => {
    const classes = ["match-row"];

    if (slot.isReal && slot.result) {
      classes.push(getStatusClass(slot));
    }

    if (isActive(slot)) {
      classes.push("highlight-match");
    }

    if (slot.id) {
      classes.push("clickable");
    }

    return classes.join(" ");
  };

  const handleMatchClick = (matchId) => {
    if (!allowMatchNavigation) return;
    navigate(`/matches/${matchId}`);
  };

  const handleTeamClick = (slot, event) => {
    event.stopPropagation();

    if (!allowTeamNavigation || !slot?.id) return;
    navigate(`/teams/${slot.id}`);
  };

  return (
    <div className="bracket-wrapper">
      {bracketGroups.map((group, gi) => (
        <BracketGroupGraph
          key={gi}
          group={group}
          matchMap={matchMap}
          buildLevels={buildLevels}
          resolveSlot={resolveSlot}
          getSlotClass={getSlotClass}
          handleMatchClick={handleMatchClick}
          handleTeamClick={handleTeamClick}
          setActiveTeamId={setActiveTeamId}
        />
      ))}
    </div>
  );
}

function BracketGroupGraph({
  group,
  matchMap,
  buildLevels,
  resolveSlot,
  getSlotClass,
  handleMatchClick,
  handleTeamClick,
  setActiveTeamId,
}) {
  const graphRef = useRef(null);
  const matchRefs = useRef(new Map());
  const [paths, setPaths] = useState([]);
  const levels = useMemo(() => buildLevels(group), [buildLevels, group]);

  const matchLayout = useMemo(() => {
    const levelByMatchId = new Map();
    const slotIndexByMatchId = new Map();

    levels.forEach((level, levelIndex) => {
      level.forEach((match, matchIndex) => {
        levelByMatchId.set(match.matchId, levelIndex);
        slotIndexByMatchId.set(match.matchId, matchIndex);
      });
    });

    return { levelByMatchId, slotIndexByMatchId };
  }, [levels]);

  useLayoutEffect(() => {
    const graphEl = graphRef.current;
    if (!graphEl) return;

    const updatePaths = () => {
      const graphRect = graphEl.getBoundingClientRect();
      const nextPaths = [];

      group.matches.forEach((match) => {
        const targetEl = matchRefs.current.get(match.matchId);
        if (!targetEl) return;

        const targetRect = targetEl.getBoundingClientRect();
        const targetLevel = matchLayout.levelByMatchId.get(match.matchId) ?? 0;

        match.slots.forEach((slot, slotIndex) => {
          if (!slot.refMatchId) return;

          const sourceEl = matchRefs.current.get(slot.refMatchId);
          if (!sourceEl) return;

          const sourceRect = sourceEl.getBoundingClientRect();
          const sourceLevel = matchLayout.levelByMatchId.get(slot.refMatchId) ?? 0;

          const startX = sourceRect.right - graphRect.left;
          const startY = sourceRect.top - graphRect.top + sourceRect.height / 2;
          const endX = targetRect.left - graphRect.left;
          const endY =
            targetRect.top -
            graphRect.top +
            targetRect.height * (slotIndex === 0 ? 0.32 : 0.68);
          const gap = Math.max(28, ((endX - startX) || 0) / 2);
          const bendX = startX + Math.min(gap, 48 + (targetLevel - sourceLevel) * 12);

          nextPaths.push({
            id: `${slot.refMatchId}-${match.matchId}-${slotIndex}`,
            d: `M ${startX} ${startY} H ${bendX} V ${endY} H ${endX}`,
          });
        });
      });

      setPaths(nextPaths);
    };

    updatePaths();

    const resizeObserver = new ResizeObserver(updatePaths);
    resizeObserver.observe(graphEl);
    matchRefs.current.forEach((node) => resizeObserver.observe(node));
    window.addEventListener("resize", updatePaths);

    return () => {
      resizeObserver.disconnect();
      window.removeEventListener("resize", updatePaths);
    };
  }, [group, matchLayout]);

  return (
    <div className="bracket-group">
      <div className="bracket-group-title">{group.name}</div>

      <div className="bracket-graph-container">
        <div ref={graphRef} className="bracket-graph">
          <svg className="bracket-lines" aria-hidden="true">
            {paths.map((path) => (
              <path key={path.id} d={path.d} />
            ))}
          </svg>

          {levels.map((level, li) => (
            <div key={li} className="bracket-column">
              {level.map((bracketMatch) => {
                const realMatch = matchMap.get(bracketMatch.matchId);

                return (
                  <div
                    key={bracketMatch.matchId}
                    ref={(node) => {
                      if (node) {
                        matchRefs.current.set(bracketMatch.matchId, node);
                      } else {
                        matchRefs.current.delete(bracketMatch.matchId);
                      }
                    }}
                    className="match-node"
                    onClick={() => handleMatchClick(bracketMatch.matchId)}
                  >
                    <div className="match-header">
                      {`Матч #${bracketMatch.matchId}`}
                      {realMatch?.status ? ` · ${translateMatchStatus(realMatch.status)}` : ""}
                    </div>

                    {bracketMatch.slots.map((slot, i) => {
                      const data = resolveSlot(slot, realMatch, i);

                      return (
                        <div
                          key={i}
                          className={getSlotClass(data)}
                          onMouseEnter={() => {
                            if (data.id) setActiveTeamId(data.id);
                          }}
                          onMouseLeave={() => setActiveTeamId(null)}
                          onClick={(event) => handleTeamClick(data, event)}
                        >
                          <span>{data.name}</span>

                          {data.result && (
                            <span style={{ fontSize: 11, opacity: 0.6 }}>
                              {translateMatchResult(data.result)}
                            </span>
                          )}
                        </div>
                      );
                    })}
                  </div>
                );
              })}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
