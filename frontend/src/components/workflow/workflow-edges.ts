/**
 * #1575: transitions are drawn as smooth-step arrows with a closed head (direction is always
 * visible, left to right) and a label on a solid background.
 */
import { MarkerType, type Edge } from "@xyflow/react";
import { theme } from "@/theme/tokens";

export interface TransitionLike {
  id?: number;
  originNodeId?: number;
  destinationNodeId?: number;
  description?: string | null;
}

export function workflowEdge(tr: TransitionLike, selected = false): Edge {
  const color = selected ? theme.colors.primary[600] : theme.colors.neutral[500];
  return {
    id: String(tr.id),
    source: String(tr.originNodeId),
    target: String(tr.destinationNodeId),
    type: "smoothstep",
    label: tr.description ?? undefined,
    animated: false,
    selected,
    markerEnd: { type: MarkerType.ArrowClosed, color, width: 18, height: 18 },
    style: { stroke: color, strokeWidth: selected ? 2.5 : 1.5 },
    labelStyle: { fontSize: 11, fill: theme.colors.neutral[700] },
    labelBgStyle: { fill: theme.colors.neutral[0], fillOpacity: 0.95 },
    labelBgPadding: [4, 2],
    labelBgBorderRadius: 4,
  };
}
