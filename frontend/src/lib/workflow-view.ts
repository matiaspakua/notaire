/**
 * #1580: which editor view to show and whether the graph can be edited. On a phone the list
 * view is the editing surface and the graph is read-only (dragging boxes is impractical).
 */
export type WorkflowView = "graph" | "list";

export function resolveView(chosen: WorkflowView | null, isPhone: boolean): WorkflowView {
  return chosen ?? (isPhone ? "list" : "graph");
}

export function graphEditable(editMode: boolean, isPhone: boolean): boolean {
  return editMode && !isPhone;
}
