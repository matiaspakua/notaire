"use client";

import { useEffect, useRef, type RefObject } from "react";

const FOCUSABLE = [
  "a[href]",
  "button:not([disabled])",
  "input:not([disabled])",
  "select:not([disabled])",
  "textarea:not([disabled])",
  '[tabindex]:not([tabindex="-1"])',
].join(",");

function focusables(root: HTMLElement): HTMLElement[] {
  return Array.from(root.querySelectorAll<HTMLElement>(FOCUSABLE)).filter(
    (el) => el.tabIndex >= 0 && !el.closest("[hidden],[inert]"),
  );
}

/**
 * Modal focus management (#1970): while `active`, Tab and Shift+Tab cycle inside `ref`,
 * Escape calls `onEscape`, and on close focus goes back to `returnTo` (or whatever had it).
 * `initial` picks the element focused on open; otherwise the first focusable one.
 */
export function useFocusTrap(
  ref: RefObject<HTMLElement | null>,
  active: boolean,
  onEscape: () => void,
  options: { returnTo?: RefObject<HTMLElement | null>; initial?: RefObject<HTMLElement | null> } = {},
) {
  const { returnTo, initial } = options;
  const escape = useRef(onEscape);
  useEffect(() => {
    escape.current = onEscape;
  }, [onEscape]);

  useEffect(() => {
    if (!active) return;
    const root = ref.current;
    if (!root) return;
    const previous = document.activeElement as HTMLElement | null;
    const restoreTo = returnTo?.current ?? previous;
    (initial?.current ?? focusables(root)[0] ?? root).focus();

    function onKey(e: KeyboardEvent) {
      if (!root) return;
      if (e.key === "Escape") {
        e.preventDefault();
        escape.current();
        return;
      }
      if (e.key !== "Tab") return;
      const items = focusables(root);
      if (items.length === 0) {
        e.preventDefault();
        root.focus();
        return;
      }
      const current = document.activeElement as HTMLElement | null;
      const index = current ? items.indexOf(current) : -1;
      const next = e.shiftKey
        ? items[index <= 0 ? items.length - 1 : index - 1]
        : items[index < 0 || index === items.length - 1 ? 0 : index + 1];
      e.preventDefault();
      next.focus();
    }

    document.addEventListener("keydown", onKey);
    const { overflow } = document.body.style;
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.style.overflow = overflow;
      restoreTo?.focus?.();
    };
  }, [active, ref, returnTo, initial]);
}
