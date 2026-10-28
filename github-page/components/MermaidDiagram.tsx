"use client";

import { useEffect, useId, useRef, useState } from "react";

type Props = {
  chart: string;
  className?: string;
  /** Accessible name for the rendered diagram; defaults to a generic one. */
  label?: string;
  /** Wide diagrams keep a readable minimum width on phones and scroll inside their frame. */
  wide?: boolean;
  /** Extra flowchart options, e.g. a wider label wrap for text-heavy diagrams (#1934). */
  flowchart?: { wrappingWidth?: number; nodeSpacing?: number; rankSpacing?: number };
};

/**
 * One Mermaid theme for the whole site (#1970): `base` with variables read from the CSS
 * design tokens (app/globals.css), so diagrams match the section accent. Charts keep their
 * own high-contrast classDefs (design-doc-mermaid skill).
 */
function themeVariables(host: HTMLElement) {
  const css = getComputedStyle(host);
  const v = (name: string, fallback: string) => css.getPropertyValue(name).trim() || fallback;
  const accent = v("--accent-600", "#0071e3");
  const ink = v("--color-ink", "#0f172a");
  const muted = v("--color-ink-muted", "#475569");
  const line = v("--color-neutral-400", "#94a3b8");
  const soft = v("--accent-50", "#eef6ff");
  return {
    fontFamily: v("--font-sans", "system-ui, sans-serif"),
    fontSize: "15px",
    background: "#ffffff",
    primaryColor: soft,
    primaryBorderColor: accent,
    primaryTextColor: ink,
    secondaryColor: v("--color-neutral-100", "#f1f5f9"),
    secondaryBorderColor: line,
    secondaryTextColor: ink,
    tertiaryColor: "#ffffff",
    tertiaryBorderColor: line,
    tertiaryTextColor: ink,
    lineColor: muted,
    textColor: ink,
    mainBkg: soft,
    nodeBorder: accent,
    clusterBkg: v("--color-neutral-50", "#f8fafc"),
    clusterBorder: v("--color-neutral-300", "#cbd5e1"),
    titleColor: ink,
    edgeLabelBackground: "#ffffff",
    actorBkg: soft,
    actorBorder: accent,
    actorTextColor: ink,
    actorLineColor: line,
    signalColor: muted,
    signalTextColor: ink,
    labelBoxBkgColor: soft,
    labelBoxBorderColor: accent,
    labelTextColor: ink,
    loopTextColor: ink,
    noteBkgColor: "#fef9c3",
    noteBorderColor: "#ca8a04",
    noteTextColor: "#422006",
    activationBkgColor: v("--accent-100", "#d9ebff"),
    activationBorderColor: accent,
    sequenceNumberColor: "#ffffff",
  };
}

export function MermaidDiagram({ chart, className, flowchart, label = "Diagram", wide = false }: Props) {
  const hostRef = useRef<HTMLDivElement>(null);
  const reactId = useId().replace(/:/g, "");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const host = hostRef.current;
        if (!host) return;
        const mermaid = (await import("mermaid")).default;
        mermaid.initialize({
          startOnLoad: false,
          theme: "base",
          themeVariables: themeVariables(host),
          securityLevel: "strict",
          flowchart: { htmlLabels: true, curve: "basis", padding: 12, ...(flowchart ?? {}) },
          sequence: { mirrorActors: false, boxMargin: 8 },
        });
        const { svg } = await mermaid.render(`mmd-${reactId}`, chart.trim());
        if (!cancelled && hostRef.current) {
          hostRef.current.innerHTML = svg;
          const el = hostRef.current.querySelector("svg");
          el?.setAttribute("aria-hidden", "true");
          el?.removeAttribute("role");
          setError(null);
        }
      } catch (e) {
        if (!cancelled) setError(e instanceof Error ? e.message : "Diagram render failed");
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [chart, reactId, flowchart]);

  if (error) {
    return (
      <pre className="whitespace-pre-wrap rounded-xl border border-red-200 bg-red-50 p-4 text-xs text-red-700">{error}</pre>
    );
  }

  return (
    <div
      ref={hostRef}
      role="img"
      aria-label={label}
      tabIndex={0}
      data-wide={wide ? "true" : undefined}
      className={`diagram-frame ${className ?? "rounded-2xl border border-slate-200 bg-white p-4"}`}
    />
  );
}
