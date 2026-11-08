"use client";

import { QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { queryClient } from "@/lib/query-client";
import { MotionConfig } from "motion/react";
import { OtelBrowserProvider } from "@/components/otel-browser-provider";

export function Providers({
  children,
  browserOtelEnabled = false,
}: {
  children: React.ReactNode;
  /** Server layout reads NEXT_PUBLIC_OTEL_TRACES_ENABLED at request time (#1739). */
  browserOtelEnabled?: boolean;
}) {
  return (
    <QueryClientProvider client={queryClient}>
      <OtelBrowserProvider enabled={browserOtelEnabled}>
        {/* reducedMotion="user" makes every motion primitive honour the OS
            "Reduce Motion" accessibility setting automatically. */}
        <MotionConfig reducedMotion="user">{children}</MotionConfig>
      </OtelBrowserProvider>
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  );
}
