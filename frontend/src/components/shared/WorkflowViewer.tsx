"use client";
import { useMemo } from "react";
import { useTranslations } from "next-intl";
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  type Node,
  type Edge,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { theme } from "@/theme/tokens";
import { NODE_META, getNodeMeta, withNodeIcon } from "@/lib/workflow-node-meta";
import type { WorkflowNode, WorkflowTransition } from "@/types";
import { layoutWorkflow, NODE_WIDTH } from "@/lib/workflow-layout";

export function toFlowNodes(nodes: WorkflowNode[], transitions: WorkflowTransition[] = []): Node[] {
  // #1574: same automatic layout as the editor, so boxes never stack at 0,0.
  const layout = layoutWorkflow(nodes, transitions);
  return nodes.map((n) => {
    const meta = getNodeMeta(n.type);
    const label = withNodeIcon(n.statusManagementName ?? `Node ${n.id}`, n.type);
    return {
      id: String(n.id),
      position: layout.get(String(n.id)) ?? { x: 0, y: 0 },
      data: { label },
      style: {
        background: meta.background,
        border: `2px solid ${meta.border}`,
        color: meta.color,
        borderRadius: "8px",
        padding: "8px 16px",
        fontWeight: 600,
        fontSize: "13px",
        width: `${NODE_WIDTH}px`,
        textAlign: "center" as const,
      },
    };
  });
}

export function toFlowEdges(transitions: WorkflowTransition[]): Edge[] {
  return transitions.map((t) => ({
    id: String(t.id),
    source: String(t.originNodeId),
    target: String(t.destinationNodeId),
    label: t.description ?? undefined,
    animated: false,
    style: { stroke: theme.colors.neutral[500] },
  }));
}

interface WorkflowViewerProps {
  nodes: WorkflowNode[];
  transitions: WorkflowTransition[];
  "data-testid"?: string;
}

export function WorkflowViewer({ nodes, transitions, "data-testid": testId }: WorkflowViewerProps) {
  const flowNodes = useMemo(() => toFlowNodes(nodes, transitions), [nodes, transitions]);
  const flowEdges = useMemo(() => toFlowEdges(transitions), [transitions]);

  if (nodes.length === 0) {
    return (
      <div
        className="flex items-center justify-center h-64 rounded-xl border border-dashed border-border text-muted-foreground text-sm"
        data-testid={testId}
      >
        <WorkflowViewerEmpty />
      </div>
    );
  }

  return (
    <div
      style={{ height: theme.sizes.workflowViewer.height }}
      className="rounded-xl border border-border overflow-hidden"
      data-testid={testId}
    >
      <ReactFlow
        nodes={flowNodes}
        edges={flowEdges}
        fitView
        nodesDraggable={false}
        nodesConnectable={false}
        elementsSelectable={false}
      >
        <Background />
        <Controls showInteractive={false} />
        <MiniMap nodeColor={(n) => {
          const nodeType = nodes.find((wn) => String(wn.id) === n.id)?.type ?? "INTERMEDIATE";
          return NODE_META[nodeType]?.border ?? theme.colors.neutral[500];
        }} />
      </ReactFlow>
    </div>
  );
}

function WorkflowViewerEmpty() {
  const t = useTranslations("workflowViewer");
  return <>{t("empty")}</>;
}
