/**
 * #1577: client-side workflow validation. Same rules and order as the backend's
 * WorkflowValidationService (which stays the source of truth on "Validar"), but each problem
 * names the nodes it concerns so the canvas can highlight them.
 */
export type WorkflowProblemCode = "empty" | "noStart" | "multipleStart" | "noFinal" | "unreachable";

export interface WorkflowProblem {
  code: WorkflowProblemCode;
  nodeIds: number[];
}

export interface WorkflowValidation {
  valid: boolean;
  problems: WorkflowProblem[];
}

interface NodeLike {
  id?: number;
  type?: string;
}

interface TransitionLike {
  originNodeId?: number;
  destinationNodeId?: number;
}

export function validateWorkflow(nodes: NodeLike[], transitions: TransitionLike[]): WorkflowValidation {
  if (nodes.length === 0) return { valid: false, problems: [{ code: "empty", nodeIds: [] }] };
  const problems: WorkflowProblem[] = [];
  const starts = nodes.filter((n) => n.type === "INITIAL").map((n) => n.id!);
  if (starts.length === 0) problems.push({ code: "noStart", nodeIds: [] });
  else if (starts.length > 1) problems.push({ code: "multipleStart", nodeIds: starts });
  if (!nodes.some((n) => n.type === "FINAL")) problems.push({ code: "noFinal", nodeIds: [] });
  if (problems.length > 0) return { valid: false, problems };

  const next = new Map<number, number[]>();
  for (const t of transitions) {
    if (t.originNodeId == null || t.destinationNodeId == null) continue;
    next.set(t.originNodeId, [...(next.get(t.originNodeId) ?? []), t.destinationNodeId]);
  }
  const seen = new Set<number>([starts[0]]);
  const stack = [starts[0]];
  while (stack.length) {
    for (const to of next.get(stack.pop()!) ?? []) {
      if (!seen.has(to)) {
        seen.add(to);
        stack.push(to);
      }
    }
  }
  const unreachable = nodes.map((n) => n.id!).filter((id) => !seen.has(id));
  if (unreachable.length) problems.push({ code: "unreachable", nodeIds: unreachable });
  return { valid: problems.length === 0, problems };
}

/** node id → first problem code that concerns it. */
export function nodeProblems(result: WorkflowValidation): Map<number, WorkflowProblemCode> {
  const out = new Map<number, WorkflowProblemCode>();
  for (const p of result.problems) for (const id of p.nodeIds) if (!out.has(id)) out.set(id, p.code);
  return out;
}

export function invalidNodeIds(result: WorkflowValidation): Set<number> {
  return new Set(nodeProblems(result).keys());
}
