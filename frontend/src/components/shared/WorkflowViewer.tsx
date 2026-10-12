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
import { getNodeMeta, withNodeIcon } from "@/lib/workflow-node-meta";
import type { WorkflowNode, WorkflowTransition } from "@/types";
import { layoutWorkflow, NODE_WIDTH, NODE_HEIGHT } from "@/lib/workflow-layout";
import { workflowNodeTypes } from "@/components/workflow/WorkflowNodeBox";
import { workflowEdge } from "@/components/workflow/workflow-edges";
import { WORKFLOW_ZOOM, WORKFLOW_FIT, MINIMAP_CLASS, workflowAriaLabels, workflowMiniMapColor } from "@/components/workflow/workflow-canvas";

export function toFlowNodes(nodes: WorkflowNode[], transitions: WorkflowTransition[] = []): Node[] {
  // #1574: same automatic layout as the editor, so boxes never stack at 0,0.
  const layout = layoutWorkflow(nodes, transitions);
  // #1575: rendered by the shared WorkflowNodeBox (type badge, shape, wrapped name).
  return nodes.map((n) => {
    const type = n.type ?? "INTERMEDIATE";
    return {
      id: String(n.id),
      type: "workflowNode",
      position: layout.get(String(n.id)) ?? { x: 0, y: 0 },
      // #1579: fixed box size, so the MiniMap draws nodes React Flow never reports as measured.
      initialWidth: NODE_WIDTH,
      initialHeight: NODE_HEIGHT,
      data: { label: withNodeIcon(n.statusManagementName ?? `Node ${n.id}`, type), type, meta: getNodeMeta(type) },
    };
  });
}

export function toFlowEdges(transitions: WorkflowTransition[]): Edge[] {
  return transitions.map((t) => workflowEdge(t));
}

interface WorkflowViewerProps {
  nodes: WorkflowNode[];
  transitions: WorkflowTransition[];
  "data-testid"?: string;
}

export function WorkflowViewer({ nodes, transitions, "data-testid": testId }: WorkflowViewerProps) {
  const flowNodes = useMemo(() => toFlowNodes(nodes, transitions), [nodes, transitions]);
  const flowEdges = useMemo(() => toFlowEdges(transitions), [transitions]);
  const tCanvas = useTranslations("workflowViewer.canvas");

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
        nodeTypes={workflowNodeTypes}
        fitView
        fitViewOptions={WORKFLOW_FIT}
        {...WORKFLOW_ZOOM}
        ariaLabelConfig={workflowAriaLabels(tCanvas)}
        nodesDraggable={false}
        nodesConnectable={false}
        elementsSelectable={false}
      >
        <Background />
        <Controls showInteractive={false} />
        <MiniMap className={MINIMAP_CLASS} nodeColor={workflowMiniMapColor} />
      </ReactFlow>
    </div>
  );
}

function WorkflowViewerEmpty() {
  const t = useTranslations("workflowViewer");
  return <>{t("empty")}</>;
}
