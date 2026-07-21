"use client";
import { useMemo } from "react";
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

export function toFlowNodes(nodes: WorkflowNode[]): Node[] {
  return nodes.map((n) => {
    const meta = getNodeMeta(n.tipo);
    const label = withNodeIcon(n.estadoGestionNombre ?? `Nodo ${n.id}`, n.tipo);
    return {
      id: String(n.id),
      position: { x: n.posicionX ?? 0, y: n.posicionY ?? 0 },
      data: { label },
      style: {
        background: meta.background,
        border: `2px solid ${meta.border}`,
        color: meta.color,
        borderRadius: "8px",
        padding: "8px 16px",
        fontWeight: 600,
        fontSize: "13px",
        minWidth: "120px",
        textAlign: "center" as const,
      },
    };
  });
}

export function toFlowEdges(transitions: WorkflowTransition[]): Edge[] {
  return transitions.map((t) => ({
    id: String(t.id),
    source: String(t.nodoOrigenId),
    target: String(t.nodoDestinoId),
    label: t.descripcion ?? undefined,
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
  const flowNodes = useMemo(() => toFlowNodes(nodes), [nodes]);
  const flowEdges = useMemo(() => toFlowEdges(transitions), [transitions]);

  if (nodes.length === 0) {
    return (
      <div
        className="flex items-center justify-center h-64 rounded-xl border border-dashed border-neutral-300 text-neutral-400 text-sm"
        data-testid={testId}
      >
        Sin nodos. Agrega estados al workflow.
      </div>
    );
  }

  return (
    <div
      style={{ height: theme.sizes.workflowViewer.height }}
      className="rounded-xl border border-neutral-200 overflow-hidden"
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
          const tipo = nodes.find((wn) => String(wn.id) === n.id)?.tipo ?? "INTERMEDIATE";
          return NODE_META[tipo]?.border ?? theme.colors.neutral[500];
        }} />
      </ReactFlow>
    </div>
  );
}
