"use client";
/**
 * #1575: a workflow status box. Fixed width (the layout relies on it), wrapped name, a text
 * type badge and a distinct shape for start and final nodes, so the type never depends on
 * color alone (#646). Shared by the editor and the read-only viewer.
 */
import { Handle, Position, type NodeProps } from "@xyflow/react";
import { useTranslations } from "next-intl";
import { getNodeMeta } from "@/lib/workflow-node-meta";
import { NODE_WIDTH, NODE_HEIGHT } from "@/lib/workflow-layout";
import { theme } from "@/theme/tokens";

export interface WorkflowNodeData extends Record<string, unknown> {
  label: string;
  type: string;
  /** #1577: validation problem code for this node, if any. */
  problem?: string;
}

const SHAPE: Record<string, "start" | "step" | "end"> = { INITIAL: "start", INTERMEDIATE: "step", FINAL: "end" };

export function WorkflowNodeBox({ id, data, selected }: NodeProps) {
  const t = useTranslations("workflowViewer");
  const { label, type, problem } = data as WorkflowNodeData;
  const meta = getNodeMeta(type);
  const shape = SHAPE[type] ?? "step";
  const typeName = t(`types.${SHAPE[type] ? type : "INTERMEDIATE"}`);
  return (
    <div
      role="group"
      aria-label={
        t("nodeAria", { name: label, type: typeName }) + (problem ? `. ${t(`problem.${problem}`)}` : "")
      }
      data-testid={`workflow-node-${id}`}
      data-shape={shape}
      data-selected={selected ? "true" : "false"}
      data-invalid={problem ? "true" : "false"}
      title={label}
      style={{
        width: `${NODE_WIDTH}px`,
        minHeight: `${NODE_HEIGHT}px`,
        background: meta.background,
        color: meta.color,
        border: `2px solid ${meta.border}`,
        // Start: rounded pill; final: double border; intermediate: plain card.
        borderRadius: shape === "start" ? "9999px" : "10px",
        borderStyle: shape === "end" ? "double" : "solid",
        borderWidth: shape === "end" ? "4px" : "2px",
        outline: problem ? `3px dashed ${theme.colors.error[600]}` : undefined,
        outlineOffset: problem ? "3px" : undefined,
        boxShadow: selected ? `0 0 0 3px ${meta.border}55, 0 4px 12px rgba(0,0,0,0.12)` : "0 1px 2px rgba(0,0,0,0.06)",
        padding: "6px 14px",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        gap: "2px",
        textAlign: "center",
      }}
    >
      <Handle type="target" position={Position.Left} />
      <span
        data-testid={`workflow-node-badge-${id}`}
        style={{ fontSize: "10px", fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.04em", opacity: 0.85 }}
      >
        {typeName}
      </span>
      <span
        style={{
          fontSize: "13px",
          fontWeight: 600,
          lineHeight: 1.25,
          overflowWrap: "anywhere",
          display: "-webkit-box",
          WebkitLineClamp: 2,
          WebkitBoxOrient: "vertical",
          overflow: "hidden",
        }}
      >
        {label}
      </span>
      {problem && (
        <span
          data-testid={`workflow-node-problem-${id}`}
          style={{ fontSize: "10px", fontWeight: 700, color: theme.colors.error[700] }}
        >
          ⚠ {t(`problem.${problem}`)}
        </span>
      )}
      <Handle type="source" position={Position.Right} />
    </div>
  );
}

export const workflowNodeTypes = { workflowNode: WorkflowNodeBox };
