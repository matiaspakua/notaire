/**
 * #1576: client-side guard for a new transition, so self-loops and duplicates get a clear
 * message instead of a generic API error.
 */
export type TransitionProblem = "selfLoop" | "duplicate" | "incomplete";

interface TransitionLike {
  originNodeId?: number;
  destinationNodeId?: number;
}

export function checkNewTransition(
  transitions: TransitionLike[],
  from: number | undefined,
  to: number | undefined,
): TransitionProblem | null {
  if (from == null || to == null) return "incomplete";
  if (from === to) return "selfLoop";
  if (transitions.some((t) => t.originNodeId === from && t.destinationNodeId === to)) return "duplicate";
  return null;
}
