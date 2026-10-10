"use client";

import { useSyncExternalStore } from "react";

/**
 * Subscribes to a CSS media query. The server snapshot is `false` so the first
 * client render matches the server HTML (no hydration mismatch); the real value
 * is applied right after hydration. Environments without `matchMedia` report
 * `false`.
 */
export function useMediaQuery(query: string): boolean {
  return useSyncExternalStore(
    (onChange) => {
      if (typeof window === "undefined" || typeof window.matchMedia !== "function") return () => {};
      const mql = window.matchMedia(query);
      mql.addEventListener?.("change", onChange);
      return () => mql.removeEventListener?.("change", onChange);
    },
    () =>
      typeof window !== "undefined" && typeof window.matchMedia === "function"
        ? window.matchMedia(query).matches
        : false,
    () => false,
  );
}

/** Below Tailwind's `md` breakpoint (768px). */
export const MOBILE_QUERY = "(max-width: 767px)";
