/**
 * #1574: automatic layout for the workflow editor and viewer.
 *
 * Saved positions win, unless they overlap a box already placed (the API defaults new
 * nodes to 0,0 and the seed has none). Every other node gets a left-to-right dagre layout
 * that follows the transitions, shifted down until it overlaps nothing.
 */
import dagre from "@dagrejs/dagre";
import type { WorkflowNode, WorkflowTransition } from "@/types";

export const NODE_WIDTH = 200;
export const NODE_HEIGHT = 64;
const GAP = 24;

export interface Point {
  x: number;
  y: number;
}

const overlaps = (a: Point, b: Point) =>
  a.x < b.x + NODE_WIDTH + GAP / 2 &&
  b.x < a.x + NODE_WIDTH + GAP / 2 &&
  a.y < b.y + NODE_HEIGHT + GAP / 2 &&
  b.y < a.y + NODE_HEIGHT + GAP / 2;

function dagrePositions(nodes: WorkflowNode[], transitions: WorkflowTransition[]): Map<string, Point> {
  const g = new dagre.graphlib.Graph();
  g.setGraph({ rankdir: "LR", nodesep: 40, ranksep: 150, marginx: 0, marginy: 0 });
  g.setDefaultEdgeLabel(() => ({}));
  const ids = new Set(nodes.map((n) => String(n.id)));
  for (const n of nodes) g.setNode(String(n.id), { width: NODE_WIDTH, height: NODE_HEIGHT });
  for (const t of transitions) {
    const from = String(t.originNodeId);
    const to = String(t.destinationNodeId);
    if (ids.has(from) && ids.has(to) && from !== to) g.setEdge(from, to);
  }
  dagre.layout(g);
  const out = new Map<string, Point>();
  for (const id of ids) {
    const p = g.node(id);
    // dagre gives centers; React Flow wants the top-left corner.
    out.set(id, { x: Math.round(p.x - NODE_WIDTH / 2), y: Math.round(p.y - NODE_HEIGHT / 2) });
  }
  return out;
}

export function layoutWorkflow(
  nodes: WorkflowNode[],
  transitions: WorkflowTransition[],
  options: { force?: boolean } = {},
): Map<string, Point> {
  const result = new Map<string, Point>();
  const pending: WorkflowNode[] = [];
  for (const n of nodes) {
    const saved = n.positionX != null && n.positionY != null ? { x: n.positionX, y: n.positionY } : null;
    if (!options.force && saved && ![...result.values()].some((p) => overlaps(p, saved))) {
      result.set(String(n.id), saved);
    } else {
      pending.push(n);
    }
  }
  if (pending.length === 0) return result;

  const auto = dagrePositions(nodes, transitions);
  // When some boxes keep their saved place, start the automatic ones below them.
  const offsetY = result.size > 0 ? Math.max(...[...result.values()].map((p) => p.y)) + NODE_HEIGHT + GAP * 2 : 0;
  const minAutoY = Math.min(...pending.map((n) => auto.get(String(n.id))!.y));
  for (const n of pending) {
    const a = auto.get(String(n.id))!;
    const p = { x: a.x, y: a.y - minAutoY + offsetY };
    while ([...result.values()].some((q) => overlaps(q, p))) p.y += NODE_HEIGHT + GAP;
    result.set(String(n.id), p);
  }
  return result;
}

/** Where a new node goes: right of everything already drawn, top aligned. */
export function nextFreePosition(existing: Point[]): Point {
  if (existing.length === 0) return { x: 0, y: 0 };
  const maxX = Math.max(...existing.map((p) => p.x));
  const minY = Math.min(...existing.map((p) => p.y));
  return { x: maxX + NODE_WIDTH + 150, y: minY };
}
