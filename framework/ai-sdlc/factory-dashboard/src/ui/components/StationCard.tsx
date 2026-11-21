/**
 * Factory Dashboard — Station card component.
 *
 * Card showing station number, title, status pill, agent, and tags.
 * Uses data attributes for CSS status/phase styling. Clickable to open detail.
 */
import { Station } from "@/types";

const STATUS_ATTR: Record<string, string> = {
  idle: "idle",
  active: "running",
  completed: "done",
  blocked: "error",
  waiting: "waiting",
  skipped: "skipped",
  paused: "paused",
};

const STATUS_LABEL: Record<string, string> = {
  idle: "idle",
  active: "running",
  completed: "done",
  blocked: "error",
  waiting: "waiting",
  skipped: "skipped",
  paused: "paused",
};

export function StationCard({
  station,
  index,
  phaseDataAttr,
  onClick,
}: {
  station: Station;
  index: number;
  phaseDataAttr: string;
  onClick: (station: Station) => void;
}) {
  const statusAttr = STATUS_ATTR[station.status || "idle"] || "idle";
  const statusLabel = STATUS_LABEL[station.status || "idle"] || "idle";

  return (
    <div
      className="station-card"
      data-status={statusAttr}
      data-phase={phaseDataAttr}
      onClick={() => onClick(station)}
      role="button"
      tabIndex={0}
      aria-label={`${station.title} — ${statusLabel}`}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          onClick(station);
        }
      }}
    >
      <div className="station-header">
        <span className="station-number">{index}</span>
        <span className={`status-pill ${statusAttr}`}>{statusLabel}</span>
      </div>

      <h3 className="station-title">{station.title}</h3>

      {station.agent && (
        <div className="station-agent">
          <span className="station-agent-icon">
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.2">
              <circle cx="6" cy="4" r="2.5" />
              <path d="M2 11c0-2.2 1.8-4 4-4s4 1.8 4 4" strokeLinecap="round" />
            </svg>
          </span>
          {station.agent}
        </div>
      )}

      <div className="station-tags">
        {station.skills.slice(0, 2).map((skill) => (
          <span key={skill} className="tag">{skill}</span>
        ))}
        {station.gate && <span className="tag gate">Gate {station.gate.number}</span>}
        {station.human && <span className="tag human">Human</span>}
      </div>
    </div>
  );
}
