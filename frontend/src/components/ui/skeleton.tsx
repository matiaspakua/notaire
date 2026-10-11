import * as React from "react";
import { cn } from "@/lib/utils";

/**
 * Placeholder block while content loads (#1359). Decorative (aria-hidden): the
 * busy container carries `aria-busy` and the loading label. It pulses only when
 * the user allows motion; under `prefers-reduced-motion` it is a static tint.
 */
function Skeleton({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      aria-hidden="true"
      data-slot="skeleton"
      className={cn("rounded-full bg-secondary motion-safe:animate-pulse", className)}
      {...props}
    />
  );
}

export { Skeleton };
