"use client";

/**
 * Motion primitives — Framer Motion (motion/react) wrappers tuned to the
 * Apple design language used across Notaire.
 *
 * All animations are subtle, spring/ease-based and globally respect the user's
 * `prefers-reduced-motion` setting (configured via <MotionConfig reducedMotion="user">
 * in providers.tsx). Use these instead of ad-hoc motion.* declarations so the
 * timing and easing stay consistent system-wide.
 */

import { motion, AnimatePresence, type Variants, type Transition } from "motion/react";
import { Children, cloneElement, isValidElement, type ReactElement, type ReactNode } from "react";
import { motion as tokens } from "@/theme/motion";

/** Standard easing from the motion tokens (#1368); kept under its old name for callers. */
export const easeApple = tokens.ease.standard;

export const springSoft: Transition = { type: "spring", stiffness: 260, damping: 26, mass: 0.9 };

// ---------------------------------------------------------------------------
// Variants
// ---------------------------------------------------------------------------

/** Delay of the i-th staggered item: 30ms steps, capped at 6 items (#1368). */
export function staggerDelay(index: number): number {
  return Math.min(Math.max(index, 0), tokens.stagger.maxItems - 1) * tokens.stagger.step;
}

/** Route content: a 160ms fade-up with no exit, so navigation never waits (#1368). */
export const pageVariants: Variants = {
  hidden: { opacity: 0, y: tokens.distance.sm },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: tokens.duration.page, ease: tokens.ease.standard },
  },
};

export const staggerContainer: Variants = {
  hidden: {},
  visible: {},
};

export const fadeUpItem: Variants = {
  hidden: { opacity: 0, y: tokens.distance.sm },
  visible: (index: number = 0) => ({
    opacity: 1,
    y: 0,
    transition: { duration: tokens.duration.base, ease: tokens.ease.standard, delay: staggerDelay(index) },
  }),
};

// ---------------------------------------------------------------------------
// Components
// ---------------------------------------------------------------------------

interface MotionBoxProps {
  children: ReactNode;
  className?: string;
}

/** Page-level entrance transition. Key it by pathname to re-run on navigation. */
export function PageTransition({ children, className }: MotionBoxProps) {
  return (
    <motion.div className={className} initial="hidden" animate="visible" variants={pageVariants}>
      {children}
    </motion.div>
  );
}

/** Container that staggers the entrance of its <StaggerItem> children. */
export function Stagger({ children, className }: MotionBoxProps) {
  return (
    <motion.div className={className} initial="hidden" animate="visible" variants={staggerContainer}>
      {Children.map(children, (child, index) =>
        isValidElement(child) && child.type === StaggerItem
          ? cloneElement(child as ReactElement<StaggerItemProps>, { index })
          : child,
      )}
    </motion.div>
  );
}

interface StaggerItemProps extends MotionBoxProps {
  /** Position in the parent <Stagger>; set by Stagger itself. */
  index?: number;
}

/** Single item inside a <Stagger>. Fades and slides up in sequence. */
export function StaggerItem({ children, className, index = 0 }: StaggerItemProps) {
  return (
    <motion.div className={className} variants={fadeUpItem} custom={index}>
      {children}
    </motion.div>
  );
}

/** Simple fade + rise on mount, with optional delay. */
export function FadeIn({ children, className, delay = 0 }: MotionBoxProps & { delay?: number }) {
  return (
    <motion.div
      className={className}
      initial={{ opacity: 0, y: tokens.distance.sm }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: tokens.duration.base, ease: tokens.ease.standard, delay }}
    >
      {children}
    </motion.div>
  );
}

/** Interactive card wrapper with a tasteful hover lift and press feedback. */
export function HoverLift({ children, className, lift = 6 }: MotionBoxProps & { lift?: number }) {
  return (
    <motion.div
      className={className}
      whileHover={{ y: -lift, transition: { ...springSoft } }}
      whileTap={{ scale: tokens.press.scale }}
    >
      {children}
    </motion.div>
  );
}

export { motion, AnimatePresence };
