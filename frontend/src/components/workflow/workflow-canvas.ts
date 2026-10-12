/**
 * #1579: shared canvas chrome for the workflow editor and viewer: bounded zoom, localized
 * accessible names for React Flow's controls, and a MiniMap colored by node type that is
 * hidden on small screens.
 */
import { getNodeMeta } from "@/lib/workflow-node-meta";

export const WORKFLOW_ZOOM = { minZoom: 0.2, maxZoom: 2 } as const;

export const WORKFLOW_FIT = { padding: 0.2 } as const;

export const MINIMAP_CLASS = "max-md:hidden";

/** React Flow `ariaLabelConfig`; `t` is scoped to `workflowViewer.canvas`. */
export function workflowAriaLabels(t: (key: string) => string) {
  return {
    "controls.ariaLabel": t("controls"),
    "controls.zoomIn.ariaLabel": t("zoomIn"),
    "controls.zoomOut.ariaLabel": t("zoomOut"),
    "controls.fitView.ariaLabel": t("fitView"),
    "controls.interactive.ariaLabel": t("interactive"),
    "minimap.ariaLabel": t("minimap"),
  };
}

export function workflowMiniMapColor(node: { data?: Record<string, unknown> }): string {
  return getNodeMeta(node.data?.type as string | undefined).border;
}
