/**
 * Factory Dashboard — Intent panel component.
 *
 * Always-visible panel showing the factory input (GitHub issue, CLI command, etc).
 */
import { Intent } from "@/types";

export function IntentPanel({ intent }: { intent: Intent | null }) {
  if (!intent) {
    return (
      <div className="intent-panel">
        <p className="intent-empty">
          No active intent. The factory is idle — submit a GitHub issue or CLI command to start.
        </p>
      </div>
    );
  }

  return (
    <div className="intent-panel">
      <div className="intent-content">
        <span className={`intent-badge ${intent.source}`}>
          {intent.source}
        </span>
        <div>
          <h2 className="intent-title">{intent.id}: {intent.title}</h2>
          <p className="intent-description">{intent.description}</p>
        </div>
      </div>
    </div>
  );
}
