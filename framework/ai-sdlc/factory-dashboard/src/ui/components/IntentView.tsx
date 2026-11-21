/**
 * IntentView — The core input of the factory.
 *
 * The Intent is the "Work Order" — it describes WHAT to build.
 * Always visible at the top of the Production Line, it anchors every
 * decision the factory makes. Supports simple prompts, task IDs
 * (GitHub/Jira), and full specifications with acceptance criteria,
 * restrictions, links, and references.
 *
 * Design: Not a card. A prominent header that frames the entire
 * dashboard, similar to a factory's work order posted on the floor.
 * Collapsible to a compact bar, but always accessible.
 */
import { useState, useCallback } from "react";
import { Intent, IntentLink, IntentAcceptanceCriterion, IntentRestriction } from "@/types";

// ───────────────────── Source icon ─────────────────────

function SourceIcon({ source }: { source: string }) {
  switch (source) {
    case "github":
      return (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
          <path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22" />
        </svg>
      );
    case "jira":
      return (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
          <rect x="3" y="3" width="18" height="18" rx="3" />
          <path d="M8 12h8M12 8v8" />
        </svg>
      );
    case "cli":
      return (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
          <polyline points="4 17 10 11 4 5" />
          <line x1="12" y1="19" x2="20" y2="19" />
        </svg>
      );
    case "mcp":
      return (
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
          <circle cx="12" cy="12" r="3" />
          <path d="M12 1v4M12 19v4M4.22 4.22l2.83 2.83M16.95 16.95l2.83 2.83M1 12h4M19 12h4M4.22 19.78l2.83-2.83M16.95 7.05l2.83-2.83" />
        </svg>
      );
    default:
      return null;
  }
}

// ───────────────────── Link type badge ─────────────────────

const LINK_TYPE_LABELS: Record<string, string> = {
  documentation: "Doc",
  design: "Design",
  related: "Related",
  dependency: "Dep",
  reference: "Ref",
};

// ───────────────────── Acceptance criterion item ─────────────────────

function CriterionItem({ criterion }: { criterion: IntentAcceptanceCriterion }) {
  return (
    <li className={`intent-criterion ${criterion.done ? "intent-criterion-done" : ""}`}>
      <span className="intent-criterion-check">{criterion.done ? "✓" : "○"}</span>
      <span className="intent-criterion-text">{criterion.text}</span>
    </li>
  );
}

// ───────────────────── Restriction item ─────────────────────

function RestrictionItem({ restriction }: { restriction: IntentRestriction }) {
  return (
    <span className={`intent-restriction intent-restriction-${restriction.severity}`}>
      <span className="intent-restriction-icon">{restriction.severity === "required" ? "⛔" : "⚠"}</span>
      {restriction.text}
    </span>
  );
}

// ───────────────────── Link item ─────────────────────

function LinkItem({ link }: { link: IntentLink }) {
  return (
    <a
      href={link.url}
      target="_blank"
      rel="noopener noreferrer"
      className="intent-link"
    >
      <span className={`intent-link-badge intent-link-${link.type}`}>
        {LINK_TYPE_LABELS[link.type] || link.type}
      </span>
      <span className="intent-link-label">{link.label}</span>
      <svg width="12" height="12" viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5">
        <path d="M6 3H3v10h10v-3" />
        <path d="M13 3L7 9M13 3H9M13 3v4" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    </a>
  );
}

// ───────────────────── Section ─────────────────────

function IntentSection({ title, children, defaultOpen = false }: {
  title: string;
  children: React.ReactNode;
  defaultOpen?: boolean;
}) {
  const [open, setOpen] = useState(defaultOpen);

  return (
    <div className="intent-section">
      <button
        className="intent-section-toggle"
        onClick={() => setOpen(!open)}
        aria-expanded={open}
      >
        <svg
          width="12"
          height="12"
          viewBox="0 0 12 12"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.5"
          className={`intent-section-arrow ${open ? "intent-section-arrow-open" : ""}`}
        >
          <path d="M3 4.5L6 7.5L9 4.5" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
        {title}
      </button>
      {open && <div className="intent-section-content">{children}</div>}
    </div>
  );
}

// ───────────────────── Main Intent View ─────────────────────

export function IntentView({ intent }: { intent: Intent | null }) {
  const [expanded, setExpanded] = useState(false);

  if (!intent) {
    return (
      <div className="intent-view">
        <div className="intent-empty-state">
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none" stroke="currentColor" strokeWidth="1.5">
            <rect x="4" y="4" width="24" height="24" rx="4" />
            <path d="M12 12h8M12 16h5" strokeLinecap="round" />
          </svg>
          <div className="intent-empty-text">
            <h3>No active intent</h3>
            <p>The factory is idle. Submit a GitHub issue, Jira ticket, CLI command, or MCP request to start a build.</p>
          </div>
        </div>
      </div>
    );
  }

  const hasRichContent =
    (intent.acceptance_criteria && intent.acceptance_criteria.length > 0) ||
    (intent.restrictions && intent.restrictions.length > 0) ||
    (intent.links && intent.links.length > 0) ||
    intent.flow ||
    intent.markdown_body ||
    (intent.references && intent.references.length > 0);

  const toggleExpand = useCallback(() => setExpanded((e) => !e), []);

  return (
    <div className={`intent-view ${expanded ? "intent-view-expanded" : ""}`}>
      {/* ── Compact header — always visible ── */}
      <div className="intent-header" onClick={toggleExpand} role="button" tabIndex={0} aria-expanded={expanded} onKeyDown={(e) => e.key === "Enter" && toggleExpand()}>
        <div className="intent-header-left">
          <span className={`intent-source-icon intent-source-${intent.source}`}>
            <SourceIcon source={intent.source} />
          </span>
          <div className="intent-header-main">
            <span className="intent-id">{intent.id}</span>
            <span className="intent-title">{intent.title}</span>
          </div>
          {intent.run_id && (
            <span className="intent-run-badge">
              <svg width="10" height="10" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.5">
                <circle cx="6" cy="6" r="5" />
                <path d="M6 3v3l2 1" strokeLinecap="round" />
              </svg>
              {intent.run_id}
            </span>
          )}
        </div>
        <div className="intent-header-right">
          {hasRichContent && (
            <span className="intent-rich-badge">
              <svg width="10" height="10" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="1.5">
                <path d="M2 3h8M2 6h6M2 9h4" strokeLinecap="round" />
              </svg>
              Spec
            </span>
          )}
          <svg
            width="14"
            height="14"
            viewBox="0 0 14 14"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.5"
            className={`intent-expand-arrow ${expanded ? "intent-expand-arrow-open" : ""}`}
          >
            <path d="M3 5.5L7 9.5L11 5.5" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </div>
      </div>

      {/* ── Expanded detail ── */}
      {expanded && (
        <div className="intent-detail">
          <div className="intent-detail-grid">
            {/* Left column: description, flow, acceptance criteria */}
            <div className="intent-detail-left">
              {intent.description && (
                <div className="intent-block">
                  <h4 className="intent-block-title">Description</h4>
                  <p className="intent-description">{intent.description}</p>
                </div>
              )}
              {intent.flow && (
                <div className="intent-block">
                  <h4 className="intent-block-title">Flow</h4>
                  <p className="intent-flow">{intent.flow}</p>
                </div>
              )}
              {intent.acceptance_criteria && intent.acceptance_criteria.length > 0 && (
                <div className="intent-block">
                  <h4 className="intent-block-title">
                    Acceptance Criteria
                    <span className="intent-count-badge">
                      {intent.acceptance_criteria.filter((c) => c.done).length}/{intent.acceptance_criteria.length}
                    </span>
                  </h4>
                  <ul className="intent-criteria-list">
                    {intent.acceptance_criteria.map((criterion, i) => (
                      <CriterionItem key={i} criterion={criterion} />
                    ))}
                  </ul>
                </div>
              )}
              {intent.markdown_body && (
                <div className="intent-block">
                  <h4 className="intent-block-title">Full Specification</h4>
                  <div className="intent-markdown-body">{intent.markdown_body}</div>
                </div>
              )}
            </div>

            {/* Right column: restrictions, links, references */}
            <div className="intent-detail-right">
              {intent.restrictions && intent.restrictions.length > 0 && (
                <div className="intent-block">
                  <h4 className="intent-block-title">Restrictions</h4>
                  <div className="intent-restrictions-list">
                    {intent.restrictions.map((restriction, i) => (
                      <RestrictionItem key={i} restriction={restriction} />
                    ))}
                  </div>
                </div>
              )}
              {intent.links && intent.links.length > 0 && (
                <div className="intent-block">
                  <h4 className="intent-block-title">Links</h4>
                  <div className="intent-links-list">
                    {intent.links.map((link, i) => (
                      <LinkItem key={i} link={link} />
                    ))}
                  </div>
                </div>
              )}
              {intent.references && intent.references.length > 0 && (
                <div className="intent-block">
                  <h4 className="intent-block-title">References</h4>
                  <ul className="intent-references-list">
                    {intent.references.map((ref, i) => (
                      <li key={i} className="intent-reference-item">{ref}</li>
                    ))}
                  </ul>
                </div>
              )}
              <div className="intent-meta">
                <span className="intent-meta-item">
                  Created {new Date(intent.created_at).toLocaleDateString()}
                </span>
                <span className="intent-meta-item">
                  Source: {intent.source}
                </span>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
