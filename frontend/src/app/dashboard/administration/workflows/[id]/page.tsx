"use client";
import { useState, useCallback, useMemo, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  applyNodeChanges,
  type Node,
  type Edge,
  type Connection,
  type NodeChange,
  type ReactFlowInstance,
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
  useUpdateWorkflowTransition,
} from "@/hooks/useWorkflow";
import { apiPost } from "@/lib/api-client";
import { presentMutationError } from "@/lib/mutation-error";
import type { WorkflowNodeType } from "@/types";
import { useManagementStatuses } from "@/hooks/useManagementStatuses";
import { theme } from "@/theme/tokens";
import { withNodeIcon } from "@/lib/workflow-node-meta";
import { workflowNodeTypes } from "@/components/workflow/WorkflowNodeBox";
import { workflowEdge } from "@/components/workflow/workflow-edges";
import { WorkflowSelectionPanel, type PanelSelection } from "@/components/workflow/WorkflowSelectionPanel";
import { WORKFLOW_ZOOM, WORKFLOW_FIT, MINIMAP_CLASS, workflowAriaLabels, workflowMiniMapColor } from "@/components/workflow/workflow-canvas";
import { checkNewTransition } from "@/lib/workflow-edit";
import { layoutWorkflow, nextFreePosition, NODE_WIDTH, NODE_HEIGHT, type Point } from "@/lib/workflow-layout";

// #1575: shared status box (type badge, start/final shape, selection highlight).
const nodeTypes = workflowNodeTypes;

export default function WorkflowEditorPage() {
  const t = useTranslations("administracion.workflows.editor");
  const tc = useTranslations("common");
  const tCanvas = useTranslations("workflowViewer.canvas");
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
  const updateTransition = useUpdateWorkflowTransition();

  const [editMode, setEditMode] = useState(false);
  const [addNodeOpen, setAddNodeOpen] = useState(false);
  const [newNodeStatusId, setNewNodeStatusId] = useState<number | "">("");
  const [newNodeType, setNewNodeType] = useState<WorkflowNodeType>("INTERMEDIATE");
  const [validationErrors, setValidationErrors] = useState<string[]>([]);

  // #1574: the canvas is derived from the query data (useNodesState only kept its first,
  // empty value). Saved positions win; missing or overlapping ones get an automatic layout.
  const layout = useMemo(() => layoutWorkflow(rawNodes, rawTransitions), [rawNodes, rawTransitions]);
  const [local, setLocal] = useState<Record<string, { position?: Point; dragging?: boolean; measured?: Node["measured"] }>>({});
  // #1576: one selection drives the highlight and the side panel, in view and edit mode.
  const [selection, setSelection] = useState<PanelSelection>(null);
  // #1576: re-fit after the graph grows, otherwise a new box can land outside the view.
  const [flow, setFlow] = useState<ReactFlowInstance | null>(null);
  const [fitTick, setFitTick] = useState(0);
  const refit = () => setFitTick((n) => n + 1);

  const nodes: Node[] = useMemo(
    () =>
      rawNodes.map((n) => {
        const id = String(n.id);
        return {
          id,
          type: "workflowNode",
          position: local[id]?.position ?? layout.get(id) ?? { x: 0, y: 0 },
          selected: selection?.kind === "node" && String(selection.id) === id,
          dragging: local[id]?.dragging ?? false,
          initialWidth: NODE_WIDTH,
          initialHeight: NODE_HEIGHT,
          // Keep React Flow's measured size; without it a node added in edit mode stays hidden.
          ...(local[id]?.measured ? { measured: local[id].measured } : {}),
          data: {
            label: withNodeIcon(
              n.statusManagementName ?? t("nodeFallback", { id: n.id ?? 0 }),
              n.type ?? "INTERMEDIATE",
            ),
            type: n.type ?? "INTERMEDIATE",
          },
        };
      }),
    [rawNodes, layout, local, selection, t],
  );

  const edges: Edge[] = useMemo(
    () =>
      rawTransitions.map((tr) => workflowEdge(tr, selection?.kind === "edge" && selection.id === tr.id)),
    [rawTransitions, selection],
  );

  // Fit once the boxes are rendered and measured (on add node / auto-organize).
  useEffect(() => {
    if (!flow || fitTick === 0) return;
    const id = setTimeout(() => flow.fitView({ ...WORKFLOW_FIT, duration: 200 }), 60);
    return () => clearTimeout(id);
  }, [flow, fitTick, nodes.length]);

  const onNodesChange = useCallback(
    (changes: NodeChange[]) => {
      const next = applyNodeChanges(changes, nodes);
      setLocal((prev) => {
        const out = { ...prev };
        for (const n of next) out[n.id] = { position: n.position, dragging: n.dragging, measured: n.measured };
        return out;
      });
    },
    [nodes],
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
      setLocal((prev) => Object.fromEntries(Object.entries(prev).map(([k, v]) => [k, { measured: v.measured }])));
      await refetchNodes();
      refit();
      toast.success(t("autoLayoutDone"));
    } catch (err) {
      presentMutationError(err, { fallback: t("errorSavePosition") });
    }
  }

  const onConnect = useCallback(
    async (connection: Connection) => {
      if (!editMode) return;
      await handleAddTransition(Number(connection.source), Number(connection.target), "");
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [editMode, rawTransitions]
  );

  async function handleAddTransition(from: number, to: number, description: string) {
    const problem = checkNewTransition(rawTransitions, from, to);
    if (problem) {
      toast.error(t(`panel.problem.${problem}`));
      return;
    }
    try {
      await createTransition.mutateAsync({
        workflowDefinitionId: workflowId,
        originNodeId: from,
        destinationNodeId: to,
        ...(description ? { description } : {}),
      });
      refetchTransitions();
      toast.success(t("panel.transitionAdded"));
    } catch (err) {
      presentMutationError(err, { fallback: t("errorCreateTransition") });
    }
  }

  async function handleChangeType(nodeId: number, type: WorkflowNodeType) {
    try {
      await updateNode.mutateAsync({ id: nodeId, data: { workflowDefinitionId: workflowId, type } });
      await refetchNodes();
      toast.success(t("panel.typeSaved"));
    } catch (err) {
      presentMutationError(err, { fallback: t("panel.errorSaveType") });
    }
  }

  async function handleSaveDescription(id: number, description: string) {
    const tr = rawTransitions.find((x) => x.id === id);
    if (!tr) return;
    try {
      await updateTransition.mutateAsync({
        id,
        data: {
          workflowDefinitionId: workflowId,
          originNodeId: tr.originNodeId,
          destinationNodeId: tr.destinationNodeId,
          condition: tr.condition,
          description,
        },
      });
      await refetchTransitions();
      toast.success(t("panel.descriptionSaved"));
    } catch (err) {
      presentMutationError(err, { fallback: t("panel.errorSaveDescription") });
    }
  }

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
      await refetchNodes();
      refit();
      toast.success(t("nodeAdded"));
    } catch (err) {
      presentMutationError(err, { fallback: t("errorAddNode") });
    }
  }

  async function handleDeleteNode(nodeId: string) {
    try {
      await deleteNodeMut.mutateAsync({ id: Number(nodeId), workflowId });
      setSelection(null);
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
      setSelection(null);
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

      <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_20rem]">
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
          onConnect={editMode ? onConnect : undefined}
          onNodeDragStop={editMode ? handleNodeDragStop : undefined}
          onNodeClick={(_, node) => setSelection({ kind: "node", id: Number(node.id) })}
          onEdgeClick={(_, edge) => setSelection({ kind: "edge", id: Number(edge.id) })}
          onPaneClick={() => setSelection(null)}
          zoomOnDoubleClick={false}
          onInit={setFlow}
          nodesDraggable={editMode}
          nodesConnectable={editMode}
          fitView
          fitViewOptions={WORKFLOW_FIT}
          {...WORKFLOW_ZOOM}
          ariaLabelConfig={workflowAriaLabels(tCanvas)}
        >
          <Background />
          <Controls />
          <MiniMap className={MINIMAP_CLASS} nodeColor={workflowMiniMapColor} pannable zoomable />
        </ReactFlow>
        )}
      </div>
      <WorkflowSelectionPanel
        selection={selection}
        nodes={rawNodes.map((n) => ({
          id: Number(n.id),
          label: n.statusManagementName ?? t("nodeFallback", { id: n.id ?? 0 }),
          type: n.type ?? "INTERMEDIATE",
        }))}
        transitions={rawTransitions}
        editMode={editMode}
        onChangeType={handleChangeType}
        onDeleteNode={(id) => handleDeleteNode(String(id))}
        onDeleteTransition={(id) => handleDeleteEdge(String(id))}
        onAddTransition={handleAddTransition}
        onSaveDescription={handleSaveDescription}
      />
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
