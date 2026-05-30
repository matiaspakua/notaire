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
import type { ReactNode } from "react";

// Apple's standard easing curve (matches theme.transitions.timing.ease).
export const easeApple = [0.4, 0, 0.2, 1] as const;

export const springSoft: Transition = { type: "spring", stiffness: 260, damping: 26, mass: 0.9 };

// ---------------------------------------------------------------------------
// Variants
// ---------------------------------------------------------------------------

export const pageVariants: Variants = {
  hidden: { opacity: 0, y: 10 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.4, ease: easeApple, when: "beforeChildren", staggerChildren: 0.045 },
  },
  exit: { opacity: 0, y: -8, transition: { duration: 0.2, ease: easeApple } },
};

export const staggerContainer: Variants = {
  hidden: {},
  visible: { transition: { staggerChildren: 0.05, delayChildren: 0.04 } },
};

export const fadeUpItem: Variants = {
  hidden: { opacity: 0, y: 14 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.4, ease: easeApple } },
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
    <motion.div className={className} initial="hidden" animate="visible" exit="exit" variants={pageVariants}>
      {children}
    </motion.div>
  );
}

/** Container that staggers the entrance of its <StaggerItem> children. */
export function Stagger({ children, className }: MotionBoxProps) {
  return (
    <motion.div className={className} initial="hidden" animate="visible" variants={staggerContainer}>
      {children}
    </motion.div>
  );
}

/** Single item inside a <Stagger>. Fades and slides up in sequence. */
export function StaggerItem({ children, className }: MotionBoxProps) {
  return (
    <motion.div className={className} variants={fadeUpItem}>
      {children}
    </motion.div>
  );
}

/** Simple fade + rise on mount, with optional delay. */
export function FadeIn({ children, className, delay = 0 }: MotionBoxProps & { delay?: number }) {
  return (
    <motion.div
      className={className}
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, ease: easeApple, delay }}
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
      whileTap={{ scale: 0.98 }}
    >
      {children}
    </motion.div>
  );
}

export { motion, AnimatePresence };
