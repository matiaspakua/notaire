"use client";

import { useEffect, useId, useRef, useState } from "react";

type Props = {
  chart: string;
  className?: string;
};

export function MermaidDiagram({ chart, className }: Props) {
  const hostRef = useRef<HTMLDivElement>(null);
  const reactId = useId().replace(/:/g, "");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const mermaid = (await import("mermaid")).default;
        mermaid.initialize({
          startOnLoad: false,
          theme: "neutral",
          securityLevel: "strict",
          fontFamily: "var(--font-body)",
        });
        const id = `mmd-${reactId}`;
        const { svg } = await mermaid.render(id, chart.trim());
        if (!cancelled && hostRef.current) {
          hostRef.current.innerHTML = svg;
          setError(null);
        }
      } catch (e) {
        if (!cancelled) {
          setError(e instanceof Error ? e.message : "Diagram render failed");
        }
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [chart, reactId]);

  if (error) {
    return (
      <pre className="text-xs text-red-600 whitespace-pre-wrap p-4 rounded-xl border border-red-200 bg-red-50">
        {error}
      </pre>
    );
  }

  return (
    <div
      ref={hostRef}
      className={className ?? "overflow-x-auto rounded-2xl border border-neutral-200 bg-white p-4"}
      aria-label="Mermaid diagram"
    />
  );
}
