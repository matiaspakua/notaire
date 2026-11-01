"use client";

import { useCallback, useEffect, useState } from "react";
import { useReducedMotion } from "framer-motion";

/**
 * Step state shared by every interactive diagram (#1977). Nothing plays on its own: the reader
 * presses Play or moves step by step. With reduced motion the steps still change, but instantly.
 */
export function useStepper(count: number, intervalMs = 2200) {
  const [index, setIndex] = useState(0);
  const [playing, setPlaying] = useState(false);
  const reduced = useReducedMotion() ?? false;

  const go = useCallback((i: number) => setIndex(((i % count) + count) % count), [count]);
  const next = useCallback(() => setIndex((i) => Math.min(i + 1, count - 1)), [count]);
  const prev = useCallback(() => setIndex((i) => Math.max(i - 1, 0)), []);
  const toggle = useCallback(() => {
    if (playing) {
      setPlaying(false);
      return;
    }
    if (index >= count - 1) setIndex(0);
    setPlaying(true);
  }, [playing, index, count]);

  // Play walks to the last step once and stops there; the stop happens in the timer callback.
  useEffect(() => {
    if (!playing) return;
    const t = setTimeout(() => {
      const last = index + 1 >= count - 1;
      setIndex(Math.min(index + 1, count - 1));
      if (last) setPlaying(false);
    }, intervalMs);
    return () => clearTimeout(t);
  }, [playing, index, count, intervalMs]);

  const stop = useCallback(() => setPlaying(false), []);
  /** The reader picked a step: stop playing and show it. */
  const jump = useCallback((i: number) => {
    setPlaying(false);
    go(i);
  }, [go]);

  return { index, go, jump, next, prev, playing, toggle, stop, reduced, count };
}

export type Stepper = ReturnType<typeof useStepper>;

/** Motion tokens from .claude/rules/ui-ux-design.md: 200–300 ms, standard easing, none when reduced. */
export const EASE = [0.4, 0, 0.2, 1] as const;
export function transition(reduced: boolean, duration = 0.3) {
  return reduced ? { duration: 0 } : { duration, ease: EASE };
}
