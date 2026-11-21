/**
 * Factory Dashboard — Station detail panel.
 *
 * Slides in from the right showing full station configuration: status, agent,
 * skills, tools, quality gates, human approval, and RepoLens integration.
 */
import { Station, Agent } from "@/types";

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
  idle: "Idle",
  active: "Running",
  completed: "Complete",
  blocked: "Error",
  waiting: "Waiting",
  skipped: "Skipped",
  paused: "Paused",
};

const PHASE_DOT_COLORS: Record<string, string> = {
  intake: "var(--phase-intake)",
  plan: "var(--phase-plan)",
  prove: "var(--phase-prove)",
  publish: "var(--phase-publish)",
  ship: "var(--phase-ship)",
};

export function StationDetail({
  station,
  agents,
  phaseDataAttr,
  onClose,
  onSkip,
}: {
  station: Station;
  agents: Agent[];
  phaseDataAttr: string;
  onClose: () => void;
  onSkip?: (stationId: string) => void;
}) {
  const agentInfo = agents.find((a) => a.name === station.agent);
  const statusAttr = STATUS_ATTR[station.status || "idle"] || "idle";
  const statusLabel = STATUS_LABEL[station.status || "idle"] || "Idle";

  return (
    <div className="detail-panel" role="dialog" aria-label={`Station detail: ${station.title}`}>
      <div className="detail-header">
        <div className="detail-title-group">
          <h2 className="detail-title">{station.title}</h2>
          <div className="detail-phase-label">
            <span
              className="detail-phase-dot"
              style={{ background: PHASE_DOT_COLORS[phaseDataAttr] }}
            />
            {station.phase} · {station.kind}
          </div>
        </div>
        <button className="detail-close" onClick={onClose} aria-label="Close detail">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.5">
            <path d="M1 1l12 12M13 1L1 13" strokeLinecap="round" />
          </svg>
        </button>
      </div>

      <div className="detail-body">
        {/* Status */}
        <div className="detail-section">
          <div className="detail-section-label">Status</div>
          <div className="detail-status">
            <span className={`detail-status-dot ${statusAttr}`} />
            <span className={`status-pill ${statusAttr}`}>{statusLabel}</span>
          </div>
        </div>

        {/* Agent */}
        {station.agent && (
          <div className="detail-section">
            <div className="detail-section-label">Assigned Agent</div>
            <div className="detail-agent-card">
              <div className="detail-agent-avatar">{agentInfo?.emoji || "🤖"}</div>
              <div className="detail-agent-info">
                <div className="detail-agent-name">{station.agent}</div>
                <div className="detail-agent-role">{agentInfo?.role || "Agent"}</div>
              </div>
            </div>
          </div>
        )}

        {/* Skills */}
        {station.skills.length > 0 && (
          <div className="detail-section">
            <div className="detail-section-label">Skills</div>
            <div className="detail-tags">
              {station.skills.map((skill) => (
                <span key={skill} className="detail-tag">{skill}</span>
              ))}
            </div>
          </div>
        )}

        {/* Tools */}
        {station.tools.length > 0 && (
          <div className="detail-section">
            <div className="detail-section-label">Tools</div>
            <ul className="detail-tools-list">
              {station.tools.map((tool) => (
                <li key={tool}>{tool}</li>
              ))}
            </ul>
          </div>
        )}

        {/* Gate */}
        {station.gate && (
          <div className="detail-section">
            <div className="detail-section-label">Quality Gate</div>
            <div className="detail-gate-card">
              <div className="detail-gate-number">Gate {station.gate.number}</div>
              <div className="detail-gate-name">{station.gate.name}</div>
              <code className="detail-gate-check">{station.gate.check}</code>
            </div>
          </div>
        )}

        {/* Human Approval */}
        {station.human && (
          <div className="detail-section">
            <div className="detail-section-label">Human Gate</div>
            <div className="detail-human-card">
              <div className="detail-human-role">{station.human.role}</div>
              <div className="detail-human-action">{station.human.action}</div>
            </div>
          </div>
        )}

        {/* RepoLens Integration */}
        {station.tools.includes("repolens") && (
          <div className="detail-section">
            <div className="detail-section-label">RepoLens Integration</div>
            <a
              href="http://127.0.0.1:3456"
              target="_blank"
              rel="noopener noreferrer"
              className="detail-repolens-link"
            >
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.5">
                <circle cx="5.5" cy="5.5" r="3.5" />
                <path d="M11 11l-2.5-2.5" strokeLinecap="round" />
              </svg>
              Open RepoLens Explorer
            </a>
          </div>
        )}

        {/* Actions */}
        {(station.status === "active" || station.status === "waiting" || station.status === "idle") && onSkip && (
          <div className="detail-section">
            <div className="detail-section-label">Actions</div>
            <div className="detail-actions">
              <button
                className="detail-action-btn detail-action-skip"
                onClick={() => onSkip(station.id)}
                title={`Skip ${station.title} station`}
              >
                <svg width="14" height="14" viewBox="0 0 14 14" fill="currentColor">
                  <path d="M5 2.5v9l6-4.5z" />
                  <rect x="10" y="2.5" width="1.5" height="9" rx="0.5" />
                </svg>
                Skip Station
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
