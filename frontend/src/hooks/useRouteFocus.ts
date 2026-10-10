"use client";

import { useEffect, useRef } from "react";

/** Attribute the dashboard layout puts on each page wrapper (keyed by pathname). */
export const ROUTE_PATH_ATTR = "data-route-path";

const RETRY_MS = 50;
const MAX_TRIES = 20; // ~1s: covers the page-enter transition and lazy headers

/**
 * Moves focus to the new page's <h1> after a client-side navigation (#1352,
 * WCAG 2.4.3). The App Router keeps focus on the clicked link, so keyboard and
 * screen-reader users would not learn that the page changed.
 *
 * - The first load is left alone (the browser starts at the document).
 * - Only pathname changes count: `?page=` or filter changes keep focus.
 * - The heading must belong to the page wrapper for the *current* pathname, so
 *   an outgoing page that is still animating out is never focused.
 * - If no heading appears, focus falls back to the container (`<main>`).
 */
export function useRouteFocus(pathname: string, containerId: string): void {
  const previous = useRef<string | null>(null);

  useEffect(() => {
    if (previous.current === null || previous.current === pathname) {
      previous.current = pathname;
      return;
    }
    previous.current = pathname;

    let tries = 0;
    let timer: ReturnType<typeof setTimeout> | undefined;
    const attempt = () => {
      const container = document.getElementById(containerId);
      if (!container) return;
      const page = Array.from(container.querySelectorAll<HTMLElement>(`[${ROUTE_PATH_ATTR}]`)).find(
        (el) => el.getAttribute(ROUTE_PATH_ATTR) === pathname,
      );
      const heading = page?.querySelector<HTMLElement>("h1");
      if (heading) {
        if (!heading.hasAttribute("tabindex")) heading.setAttribute("tabindex", "-1");
        heading.focus({ preventScroll: true });
        return;
      }
      tries += 1;
      if (tries < MAX_TRIES) {
        timer = setTimeout(attempt, RETRY_MS);
      } else {
        container.focus({ preventScroll: true });
      }
    };
    attempt();
    return () => clearTimeout(timer);
  }, [pathname, containerId]);
}
