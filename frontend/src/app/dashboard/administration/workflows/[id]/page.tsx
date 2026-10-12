"use client";
import { useState, useCallback, useMemo } from "react";
import { useParams, useRouter } from "next/navigation";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  applyNodeChanges,
  applyEdgeChanges,
  type Node,
  type Edge,
  type Connection,
  type NodeChange,
  type EdgeChange,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import {
  useWorkflowDefinitions,
  useWorkflowNodes,
  useWorkflowTransitions,
  useCreateWorkflowNode,
  useUpdateWorkflowNode,
  useDeleteWorkflowNode,
  useCreateWorkflowTransition,
  useDeleteWorkflowTransition,
} from "@/hooks/useWorkflow";
import { apiPost } from "@/lib/api-client";
import { presentMutationError } from "@/lib/mutation-error";
import type { WorkflowNodeType } from "@/types";
import { useManagementStatuses } from "@/hooks/useManagementStatuses";
import { theme } from "@/theme/tokens";
import { withNodeIcon } from "@/lib/workflow-node-meta";
import { workflowNodeTypes } from "@/components/workflow/WorkflowNodeBox";
import { workflowEdge } from "@/components/workflow/workflow-edges";
import { layoutWorkflow, nextFreePosition, type Point } from "@/lib/workflow-layout";

// #1575: shared status box (type badge, start/final shape, selection highlight).
const nodeTypes = workflowNodeTypes;

export default function WorkflowEditorPage() {
  const t = useTranslations("administracion.workflows.editor");
  const tc = useTranslations("common");
  const params = useParams();
  const router = useRouter();
  const workflowId = Number(params.id);

  const { data: workflows = [] } = useWorkflowDefinitions();
  const workflow = workflows.find((wf) => wf.id === workflowId);
  const { data: rawNodes = [], refetch: refetchNodes, isFetched: nodesFetched } = useWorkflowNodes(workflowId);
  const { isFetched: transitionsFetched } = useWorkflowTransitions(workflowId);
  const { data: rawTransitions = [], refetch: refetchTransitions } = useWorkflowTransitions(workflowId);

  const { data: statuses = [] } = useManagementStatuses();
  const createNode = useCreateWorkflowNode();
  const updateNode = useUpdateWorkflowNode();
  const deleteNodeMut = useDeleteWorkflowNode();
  const createTransition = useCreateWorkflowTransition();
  const deleteTransitionMut = useDeleteWorkflowTransition();

  const [editMode, setEditMode] = useState(false);
  const [addNodeOpen, setAddNodeOpen] = useState(false);
  const [newNodeStatusId, setNewNodeStatusId] = useState<number | "">("");
  const [newNodeType, setNewNodeType] = useState<WorkflowNodeType>("INTERMEDIATE");
  const [validationErrors, setValidationErrors] = useState<string[]>([]);

  // #1574: the canvas is derived from the query data (useNodesState only kept its first,
  // empty value). Saved positions win; missing or overlapping ones get an automatic layout.
  const layout = useMemo(() => layoutWorkflow(rawNodes, rawTransitions), [rawNodes, rawTransitions]);
  const [local, setLocal] = useState<Record<string, { position?: Point; selected?: boolean; dragging?: boolean }>>({});
  const [selectedEdges, setSelectedEdges] = useState<Record<string, boolean>>({});

  const nodes: Node[] = useMemo(
    () =>
      rawNodes.map((n) => {
        const id = String(n.id);
        return {
          id,
          type: "workflowNode",
          position: local[id]?.position ?? layout.get(id) ?? { x: 0, y: 0 },
          selected: local[id]?.selected ?? false,
          dragging: local[id]?.dragging ?? false,
          data: {
            label: withNodeIcon(
              n.statusManagementName ?? t("nodeFallback", { id: n.id ?? 0 }),
              n.type ?? "INTERMEDIATE",
            ),
            type: n.type ?? "INTERMEDIATE",
          },
        };
      }),
    [rawNodes, layout, local, t],
  );

  const edges: Edge[] = useMemo(
    () =>
      rawTransitions.map((tr) => workflowEdge(tr, selectedEdges[String(tr.id)] ?? false)),
    [rawTransitions, selectedEdges],
  );

  const onNodesChange = useCallback(
    (changes: NodeChange[]) => {
      const next = applyNodeChanges(changes, nodes);
      setLocal((prev) => {
        const out = { ...prev };
        for (const n of next) out[n.id] = { position: n.position, selected: n.selected, dragging: n.dragging };
        return out;
      });
    },
    [nodes],
  );

  const onEdgesChange = useCallback(
    (changes: EdgeChange[]) => {
      const next = applyEdgeChanges(changes, edges);
      setSelectedEdges(Object.fromEntries(next.map((e) => [e.id, !!e.selected])));
    },
    [edges],
  );

  async function handleAutoLayout() {
    const forced = layoutWorkflow(rawNodes, rawTransitions, { force: true });
    try {
      await Promise.all(
        rawNodes.map((n) => {
          const p = forced.get(String(n.id))!;
          return updateNode.mutateAsync({
            id: Number(n.id),
            data: { workflowDefinitionId: workflowId, positionX: p.x, positionY: p.y },
          });
        }),
      );
      setLocal({});
      await refetchNodes();
      toast.success(t("autoLayoutDone"));
    } catch (err) {
      presentMutationError(err, { fallback: t("errorSavePosition") });
    }
  }

  const onConnect = useCallback(
    async (connection: Connection) => {
      if (!editMode) return;
      try {
        await createTransition.mutateAsync({
          workflowDefinitionId: workflowId,
          originNodeId: Number(connection.source),
          destinationNodeId: Number(connection.target),
        });
        refetchTransitions();
      } catch (err) {
        presentMutationError(err, { fallback: t("errorCreateTransition") });
      }
    },
    [editMode, createTransition, workflowId, refetchTransitions, t]
  );

  async function handleNodeDragStop(_: unknown, node: Node) {
    if (!editMode) return;
    try {
      await updateNode.mutateAsync({
        id: Number(node.id),
        data: {
          workflowDefinitionId: workflowId,
          positionX: node.position.x,
          positionY: node.position.y,
        },
      });
    } catch (err) {
      presentMutationError(err, { fallback: t("errorSavePosition") });
    }
  }

  async function handleAddNode() {
    if (!newNodeStatusId) {
      toast.error(t("selectStatus"));
      return;
    }
    const free = nextFreePosition(nodes.map((n) => n.position));
    try {
      await createNode.mutateAsync({
        workflowDefinitionId: workflowId,
        statusManagementId: Number(newNodeStatusId),
        type: newNodeType,
        positionX: Math.round(free.x),
        positionY: Math.round(free.y),
      });
      setAddNodeOpen(false);
      setNewNodeStatusId("");
      setNewNodeType("INTERMEDIATE");
      refetchNodes();
      toast.success(t("nodeAdded"));
    } catch (err) {
      presentMutationError(err, { fallback: t("errorAddNode") });
    }
  }

  async function handleDeleteNode(nodeId: string) {
    try {
      await deleteNodeMut.mutateAsync({ id: Number(nodeId), workflowId });
      refetchNodes();
      refetchTransitions();
      toast.success(t("nodeDeleted"));
    } catch (err) {
      presentMutationError(err, {
        fallback: t("errorDeleteNodeHasTransitions"),
      });
    }
  }

  async function handleDeleteEdge(edgeId: string) {
    try {
      await deleteTransitionMut.mutateAsync({ id: Number(edgeId), workflowId });
      refetchTransitions();
      toast.success(t("transitionDeleted"));
    } catch (err) {
      presentMutationError(err, { fallback: t("errorDeleteTransition") });
    }
  }

  async function handleValidate() {
    try {
      const result = await apiPost<{ valid: boolean; errors: string[] }>(
        `/workflow-definition/${workflowId}/validate`,
        {}
      );
      setValidationErrors(result.errors ?? []);
      if (result.valid) {
        toast.success(t("valid"));
      } else {
        toast.error(t("invalid"));
      }
    } catch (err) {
      presentMutationError(err, { fallback: t("errorValidate") });
    }
  }

  return (
    <div>
      <AppHeader
        title={workflow?.name ?? t("title")}
        description={workflow?.description ?? ""}
        actions={
          <div className="flex gap-2">
            <Button variant="secondary" onClick={() => router.back()}>
              {t("back")}
            </Button>
            <Button
              variant={editMode ? "default" : "secondary"}
              onClick={() => setEditMode((v) => !v)}
              data-testid="btn-toggle-edit"
            >
              {editMode ? t("editMode") : t("viewMode")}
            </Button>
            {editMode && (
              <Button onClick={() => setAddNodeOpen(true)} data-testid="btn-add-node">
                {t("addNode")}
              </Button>
            )}
            {editMode && (
              <Button variant="secondary" onClick={handleAutoLayout} disabled={updateNode.isPending} data-testid="btn-auto-layout">
                {t("autoLayout")}
              </Button>
            )}
            <Button variant="secondary" onClick={handleValidate} data-testid="btn-validate">
              {t("validate")}
            </Button>
          </div>
        }
      />

      {validationErrors.length > 0 && (
        <div className="mb-4 p-3 rounded-lg bg-destructive/10 border border-destructive/30" data-testid="validation-errors">
          <p className="text-sm font-semibold text-destructive mb-1">{t("consistencyErrors")}</p>
          <ul className="list-disc list-inside text-sm text-destructive">
            {validationErrors.map((e, i) => <li key={i}>{e}</li>)}
          </ul>
        </div>
      )}

      <div className="mb-3 flex gap-3 text-xs text-muted-foreground">
        <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-full bg-success/20 border border-success inline-block" />{t("types.INITIAL")}</span>
        <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-full bg-info/20 border border-info inline-block" />{t("types.INTERMEDIATE")}</span>
        <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-full bg-destructive/20 border border-destructive inline-block" />{t("types.FINAL")}</span>
      </div>

      <div
        style={{ height: theme.sizes.workflowEditor.height }}
        className="rounded-xl border border-border overflow-hidden"
        data-testid="workflow-editor"
      >
        {/* fitView only runs on mount: mount once the graph is loaded so it fits every box. */}
        {nodesFetched && transitionsFetched && (
        <ReactFlow
          nodes={nodes}
          edges={edges}
          nodeTypes={nodeTypes}
          onNodesChange={editMode ? onNodesChange : undefined}
          onEdgesChange={editMode ? onEdgesChange : undefined}
          onConnect={editMode ? onConnect : undefined}
          onNodeDragStop={editMode ? handleNodeDragStop : undefined}
          onNodeDoubleClick={editMode ? (_, node) => handleDeleteNode(node.id) : undefined}
          onEdgeDoubleClick={editMode ? (_, edge) => handleDeleteEdge(edge.id) : undefined}
          nodesDraggable={editMode}
          nodesConnectable={editMode}
          fitView
          fitViewOptions={{ padding: 0.2 }}
          minZoom={0.2}
        >
          <Background />
          <Controls />
          <MiniMap />
        </ReactFlow>
        )}
      </div>
      {editMode && (
        <p className="text-xs text-muted-foreground mt-1">
          {t("hint")}
        </p>
      )}

      <Dialog open={addNodeOpen} onOpenChange={setAddNodeOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={t("addNodeTitle")}>
              <FormField label={t("fields.estado")} required>
                <select
                  className="w-full border border-input rounded-lg px-3 py-2 text-sm bg-background"
                  value={newNodeStatusId}
                  onChange={(e) => setNewNodeStatusId(e.target.value ? Number(e.target.value) : "")}
                  data-testid="select-status-node"
                >
                  <option value="">{t("fields.estadoPlaceholder")}</option>
                  {statuses.map((e) => (
                    <option key={e.idManagementStatus} value={e.idManagementStatus}>{e.name}</option>
                  ))}
                </select>
              </FormField>
              <FormField label={t("fields.tipo")} required>
                <select
                  className="w-full border border-input rounded-lg px-3 py-2 text-sm bg-background"
                  value={newNodeType}
                  onChange={(e) => setNewNodeType(e.target.value as WorkflowNodeType)}
                  data-testid="select-node-type"
                >
                  <option value="INITIAL">{t("types.INITIAL")}</option>
                  <option value="INTERMEDIATE">{t("types.INTERMEDIATE")}</option>
                  <option value="FINAL">{t("types.FINAL")}</option>
                </select>
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setAddNodeOpen(false)}>{tc("cancel")}</Button>
              <Button onClick={handleAddNode} data-testid="btn-confirm-node">{tc("add")}</Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>
    </div>
  );
}
