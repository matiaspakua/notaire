/**
 * Motion tokens (#1368) — TS mirror of globals.css `:root --motion-*` for
 * motion/react. tests/unit/motion-tokens.test.ts fails if the two drift.
 *
 * | Token     | Value | Use                                             |
 * |-----------|-------|-------------------------------------------------|
 * | instant   | 0     | reduced motion                                  |
 * | fast      | 120ms | hover, press, focus ring, toggles, tooltips     |
 * | base      | 180ms | dropdowns, selects, popovers, toasts            |
 * | slow      | 240ms | dialogs and sheets entering                     |
 * | exit      | 180ms | dialogs and sheets leaving (0.75 x slow)        |
 * | page      | 160ms | route content fade, no exit wait                |
 *
 * Only opacity and transform animate; press feedback is scale(0.98); stagger
 * is capped at 6 items x 30ms.
 */
export const motion = {
  duration: { instant: 0, fast: 0.12, base: 0.18, slow: 0.24, exit: 0.18, page: 0.16 },
  ease: {
    standard: [0.2, 0, 0, 1] as [number, number, number, number],
    emphasized: [0.3, 0, 0, 1] as [number, number, number, number],
    exit: [0.3, 0, 1, 1] as [number, number, number, number],
  },
  distance: { sm: 4 },
  stagger: { step: 0.03, maxItems: 6 },
  press: { scale: 0.98 },
} as const;

/** CSS `cubic-bezier()` for a motion easing. */
export const cssEase = (curve: readonly number[]): string => `cubic-bezier(${curve.join(", ")})`;

/** CSS milliseconds for a motion duration (seconds). */
export const cssMs = (seconds: number): string => `${Math.round(seconds * 1000)}ms`;
