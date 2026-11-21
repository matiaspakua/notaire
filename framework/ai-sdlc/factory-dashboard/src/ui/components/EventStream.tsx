/**
 * Factory Dashboard — Event stream component.
 *
 * Log view showing real-time evidence events from the factory engine.
 */
import { EvidenceEvent } from "@/types";

export function EventStream({ events }: { events: EvidenceEvent[] }) {
  if (events.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-state-icon">
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none" stroke="currentColor" strokeWidth="1.5" opacity="0.4">
            <path d="M4 10h24M4 16h24M4 22h16" strokeLinecap="round" />
          </svg>
        </div>
        <p>Waiting for evidence events from the factory engine…</p>
      </div>
    );
  }

  return (
    <div className="event-stream" role="log" aria-live="polite" aria-label="Factory event stream">
      {events.map((event, index) => {
        const time = new Date(event.timestamp).toLocaleTimeString("en-GB", {
          hour: "2-digit",
          minute: "2-digit",
          second: "2-digit",
        });

        return (
          <div key={index} className="event-item">
            <span className="event-time">{time}</span>
            <span className="event-kind">{event.kind}</span>
            <span className="event-detail">
              {event.run_id && `run:${event.run_id} `}
              {event.step && `step:${event.step} `}
              {event.status && `→ ${event.status}`}
            </span>
          </div>
        );
      })}
    </div>
  );
}
