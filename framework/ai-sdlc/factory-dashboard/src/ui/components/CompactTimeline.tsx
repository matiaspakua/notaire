/**
 * Compact Timeline — Horizontal progress visualization.
 *
 * Shows all stations as a compact horizontal timeline with phase markers,
 * status indicators (completed, running, pending, skipped), and clickable
 * dots to inspect any station. Phase boundaries are marked with subtle
 * dividers and labels.
 *
 * Follows the "Operate" mode: scanability, precision, minimal decoration.
 */
import { Station, StationPhase } from "@/types";

type CompactTimelineProps = {
  stations: Station[];
  phases: StationPhase[];
  phaseDataAttr: Record<StationPhase, string>;
  onStationClick: (station: Station) => void;
};

// Status icon for each station
function StatusIcon({ status }: { status: string }) {
  if (status === "completed") {
    return (
      <svg width="10" height="10" viewBox="0 0 10 10" fill="none" stroke="currentColor" strokeWidth="2">
        <path d="M2 5l2 2 4-4" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    );
  }
  if (status === "active") {
    return (
      <svg width="10" height="10" viewBox="0 0 10 10" fill="currentColor">
        <circle cx="5" cy="5" r="3" />
      </svg>
    );
  }
  if (status === "skipped") {
    return (
      <svg width="10" height="10" viewBox="0 0 10 10" fill="none" stroke="currentColor" strokeWidth="2">
        <path d="M2 2l6 6M8 2l-6 6" strokeLinecap="round" />
      </svg>
    );
  }
  if (status === "error" || status === "blocked") {
    return (
      <svg width="10" height="10" viewBox="0 0 10 10" fill="none" stroke="currentColor" strokeWidth="2">
        <path d="M5 2v4M5 7.5v0.5" strokeLinecap="round" />
      </svg>
    );
  }
  // Default: pending dot
  return (
    <svg width="10" height="10" viewBox="0 0 10 10" fill="none" stroke="currentColor" strokeWidth="1.5">
      <circle cx="5" cy="5" r="2" />
    </svg>
  );
}

export function CompactTimeline({
  stations,
  phases,
  phaseDataAttr,
  onStationClick,
}: CompactTimelineProps) {
  // Track phase transitions for divider placement
  const phaseDividers: { index: number; phase: StationPhase }[] = [];
  let lastPhase: StationPhase | null = null;

  for (let i = 0; i < stations.length; i++) {
    if (stations[i].phase !== lastPhase) {
      if (lastPhase !== null) {
        phaseDividers.push({ index: i, phase: stations[i].phase });
      }
      lastPhase = stations[i].phase;
    }
  }

  return (
    <div className="compact-timeline">
      <div className="compact-timeline-header">
        <span className="compact-timeline-label">Process Flow</span>
        <span className="compact-timeline-count">
          {stations.filter((s) => s.status === "completed").length}/{stations.length} complete
        </span>
      </div>

      <div className="compact-timeline-track">
        {stations.map((station, index) => {
          const phaseAttr = phaseDataAttr[station.phase];
          const isPhaseDivider = phaseDividers.some((d) => d.index === index);

          return (
            <div key={station.id} className="compact-timeline-item-wrapper">
              {/* Phase divider */}
              {isPhaseDivider && (
                <div className="compact-timeline-divider" data-phase={phaseAttr}>
                  <span className="compact-timeline-divider-label">{station.phase}</span>
                </div>
              )}

              {/* Station dot */}
              <button
                className={`compact-timeline-dot ${station.status}`}
                data-phase={phaseAttr}
                onClick={() => onStationClick(station)}
                title={`${station.title} (${station.status})`}
                aria-label={`${station.title}: ${station.status}`}
              >
                <StatusIcon status={station.status} />
              </button>

              {/* Tooltip on hover */}
              <span className="compact-timeline-tooltip">
                <span className="compact-timeline-tooltip-title">{station.title}</span>
                <span className="compact-timeline-tooltip-status">{station.status}</span>
              </span>
            </div>
          );
        })}
      </div>

      {/* Phase legend */}
      <div className="compact-timeline-legend">
        {phases.map((phase) => (
          <span key={phase} className="compact-timeline-legend-item">
            <span className={`phase-indicator ${phaseDataAttr[phase]}`} />
            {phase}
          </span>
        ))}
      </div>
    </div>
  );
}

