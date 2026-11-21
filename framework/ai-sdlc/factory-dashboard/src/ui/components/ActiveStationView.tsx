/**
 * Active Station View — Hybrid Approach core component.
 *
 * Shows the currently running station as a prominent card with live status,
 * agent info, skills, tools, gates, and elapsed time. When no station is
 * active, shows the next pending station with a "ready" state.
 *
 * Follows the "Operate" mode: single focus, scanability, precision,
 * no decoration competing with data.
 */
import { useMemo } from "react";
import { Station, Agent, StationPhase } from "@/types";

type ActiveStationViewProps = {
  stations: Station[];
  agents: Agent[];
  phases: StationPhase[];
  phaseDataAttr: Record<StationPhase, string>;
  onStationClick: (station: Station) => void;
};

// Format elapsed time from a timestamp
function formatElapsed(isoString: string): string {
  const ms = Date.now() - new Date(isoString).getTime();
  const seconds = Math.floor(ms / 1000);
  const minutes = Math.floor(seconds / 60);
  const secs = seconds % 60;
  if (minutes > 0) return `${minutes}m ${secs}s`;
  return `${secs}s`;
}

// Agent emoji lookup
function getAgentEmoji(agentName: string, agents: Agent[]): string {
  const found = agents.find((a) => a.name === agentName);
  return found?.emoji || "⚙️";
}

// Agent role lookup
function getAgentRole(agentName: string, agents: Agent[]): string {
  const found = agents.find((a) => a.name === agentName);
  return found?.role || agentName;
}

export function ActiveStationView({
  stations,
  agents,
  phases,
  phaseDataAttr,
  onStationClick,
}: ActiveStationViewProps) {
  // Find the active (running) station, or the next pending one
  const activeStation = useMemo(() => {
    const running = stations.find((s) => s.status === "active");
    if (running) return running;
    // Find first non-completed, non-skipped station
    return stations.find((s) => s.status !== "completed" && s.status !== "skipped");
  }, [stations]);

  if (!activeStation) {
    return (
      <div className="active-station-empty">
        <div className="active-station-empty-icon">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none" stroke="currentColor" strokeWidth="1.5">
            <circle cx="24" cy="24" r="20" />
            <path d="M24 16v8M24 28v2" strokeLinecap="round" />
          </svg>
        </div>
        <h3 className="active-station-empty-title">Factory Idle</h3>
        <p className="active-station-empty-text">
          No active station. Start a new run to begin the production line.
        </p>
      </div>
    );
  }

  const isRunning = activeStation.status === "active";
  const phaseAttr = phaseDataAttr[activeStation.phase];
  const agentEmoji = getAgentEmoji(activeStation.agent, agents);
  const agentRole = getAgentRole(activeStation.agent, agents);

  // Calculate position in overall process
  const totalStations = stations.length;
  const completedBefore = stations.filter(
    (s, idx) => idx < stations.indexOf(activeStation) && s.status === "completed"
  ).length;
  const currentIndex = stations.indexOf(activeStation) + 1;

  return (
    <div
      className={`active-station-card ${isRunning ? "running" : "pending"}`}
      data-phase={phaseAttr}
      onClick={() => onStationClick(activeStation)}
      role="button"
      tabIndex={0}
      aria-label={`Active station: ${activeStation.title}`}
    >
      {/* Header with status indicator */}
      <div className="active-station-header">
        <div className="active-station-status">
          <span className={`active-station-dot ${isRunning ? "pulse" : ""}`} />
          <span className="active-station-status-text">
            {isRunning ? "Running Now" : "Next Up"}
          </span>
        </div>
        <div className="active-station-position">
          Step {currentIndex} of {totalStations}
        </div>
      </div>

      {/* Main content */}
      <div className="active-station-body">
        <div className="active-station-title-group">
          <span className={`phase-indicator ${phaseAttr}`} />
          <div>
            <h2 className="active-station-title">{activeStation.title}</h2>
            <span className="active-station-phase">{activeStation.phase}</span>
          </div>
        </div>

        {/* Agent info */}
        <div className="active-station-agent">
          <span className="agent-emoji">{agentEmoji}</span>
          <div>
            <div className="agent-name">{activeStation.agent}</div>
            <div className="agent-role">{agentRole}</div>
          </div>
        </div>

        {/* Skills and tools */}
        <div className="active-station-meta">
          {activeStation.skills && activeStation.skills.length > 0 && (
            <div className="meta-group">
              <span className="meta-label">Skills</span>
              <div className="meta-tags">
                {activeStation.skills.map((skill) => (
                  <span key={skill} className="meta-tag">{skill}</span>
                ))}
              </div>
            </div>
          )}
          {activeStation.tools && activeStation.tools.length > 0 && (
            <div className="meta-group">
              <span className="meta-label">Tools</span>
              <div className="meta-tags">
                {activeStation.tools.map((tool) => (
                  <span key={tool} className="meta-tag">{tool}</span>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Gate info */}
        {activeStation.gate && (
          <div className="active-station-gate">
            <span className="gate-icon">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.5">
                <path d="M7 1l2 4h4l-3 3 1 4-4-2-4 2 1-4-3-3h4z" />
              </svg>
            </span>
            <span className="gate-text">
              Gate {activeStation.gate.number}: {activeStation.gate.name}
            </span>
            <span className="gate-check">{activeStation.gate.check}</span>
          </div>
        )}

        {/* Human gate */}
        {activeStation.human && (
          <div className="active-station-human">
            <span className="human-icon">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.5">
                <circle cx="7" cy="4" r="2.5" />
                <path d="M2 13c0-2.5 2.2-4.5 5-4.5s5 2 5 4.5" />
              </svg>
            </span>
            <span className="human-text">
              {activeStation.human.role}: {activeStation.human.action}
            </span>
          </div>
        )}
      </div>

      {/* Footer with elapsed time and click hint */}
      <div className="active-station-footer">
        {isRunning && (
          <span className="active-station-elapsed">
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.5">
              <circle cx="6" cy="6" r="5" />
              <path d="M6 3v3l2 1" strokeLinecap="round" />
            </svg>
            Running
          </span>
        )}
        <span className="active-station-hint">Click to inspect details →</span>
      </div>
    </div>
  );
}

