"use client";

import { useEffect } from "react";
import { startBrowserTracing } from "@/lib/otel/browser-tracing";

/**
 * Starts browser OTel tracing once when enabled (#1739).
 * Failures are swallowed so missing collectors never break the UI.
 */
export function OtelBrowserProvider({
  children,
  enabled = false,
}: {
  children: React.ReactNode;
  /** From server layout — runtime Compose env, not build-time inline. */
  enabled?: boolean;
}) {
  useEffect(() => {
    void startBrowserTracing({ enabled }).catch(() => {
      // Collector / dynamic import failures must not surface to the user.
    });
  }, [enabled]);

  return <>{children}</>;
}
