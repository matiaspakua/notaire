"use client";
import { useState, useCallback } from "react";
import { useParams, useRouter } from "next/navigation";
import { toast } from "sonner";
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  addEdge,
  useNodesState,
  useEdgesState,
  type Node,
  type Edge,
  type Connection,
  Handle,
  Position,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { AppHeader } from "@/components/layout/AppHeader";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
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
import type { WorkflowNodeType } from "@/types";
import { useEstadosGestion } from "@/hooks/useEstadosGestion";

const NODE_COLORS: Record<string, { bg: string; border: string; text: string }> = {
  INITIAL: { bg: "#dcfce7", border: "#16a34a", text: "#15803d" },
  INTERMEDIATE: { bg: "#dbeafe", border: "#2563eb", text: "#1d4ed8" },
  FINAL: { bg: "#fee2e2", border: "#dc2626", text: "#b91c1c" },
};

function WorkflowNodeComponent({ data }: { data: { label: string; tipo: string } }) {
  const colors = NODE_COLORS[data.tipo] ?? NODE_COLORS.INTERMEDIATE;
  return (
    <div
      style={{
        background: colors.bg,
        border: `2px solid ${colors.border}`,
        color: colors.text,
        borderRadius: "8px",
        padding: "8px 16px",
        fontWeight: 600,
        fontSize: "13px",
        minWidth: "120px",
        textAlign: "center",
      }}
    >
      <Handle type="target" position={Position.Left} />
      {data.label}
      <Handle type="source" position={Position.Right} />
    </div>
  );
}

const nodeTypes = { workflowNode: WorkflowNodeComponent };

export default function WorkflowEditorPage() {
  const params = useParams();
  const router = useRouter();
  const workflowId = Number(params.id);

  const { data: workflows = [] } = useWorkflowDefinitions();
  const workflow = workflows.find((wf) => wf.id === workflowId);
  const { data: rawNodes = [], refetch: refetchNodes } = useWorkflowNodes(workflowId);
  const { data: rawTransitions = [], refetch: refetchTransitions } = useWorkflowTransitions(workflowId);

  const { data: estados = [] } = useEstadosGestion();
  const createNode = useCreateWorkflowNode();
  const updateNode = useUpdateWorkflowNode();
  const deleteNodeMut = useDeleteWorkflowNode();
  const createTransition = useCreateWorkflowTransition();
  const deleteTransitionMut = useDeleteWorkflowTransition();

  const [editMode, setEditMode] = useState(false);
  const [addNodeOpen, setAddNodeOpen] = useState(false);
  const [newNodeEstadoId, setNewNodeEstadoId] = useState<number | "">("");
  const [newNodeTipo, setNewNodeTipo] = useState<WorkflowNodeType>("INTERMEDIATE");
  const [validationErrors, setValidationErrors] = useState<string[]>([]);

  const flowNodes: Node[] = rawNodes.map((n) => ({
    id: String(n.id),
    type: "workflowNode",
    position: { x: n.posicionX ?? 0, y: n.posicionY ?? 0 },
    data: { label: n.estadoGestionNombre ?? `Nodo ${n.id}`, tipo: n.tipo ?? "INTERMEDIATE" },
  }));

  const flowEdges: Edge[] = rawTransitions.map((t) => ({
    id: String(t.id),
    source: String(t.nodoOrigenId),
    target: String(t.nodoDestinoId),
    label: t.descripcion ?? undefined,
    animated: false,
    style: { stroke: "#94a3b8" },
  }));

  const [nodes, , onNodesChange] = useNodesState(flowNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(flowEdges);

  const onConnect = useCallback(
    async (connection: Connection) => {
      if (!editMode) return;
      setEdges((eds) => addEdge(connection, eds));
      try {
        await createTransition.mutateAsync({
          workflowDefinitionId: workflowId,
          nodoOrigenId: Number(connection.source),
          nodoDestinoId: Number(connection.target),
        });
        refetchTransitions();
      } catch {
        toast.error("Error al crear transición");
      }
    },
    [editMode, createTransition, workflowId, refetchTransitions, setEdges]
  );

  async function handleNodeDragStop(_: unknown, node: Node) {
    if (!editMode) return;
    try {
      await updateNode.mutateAsync({
        id: Number(node.id),
        data: {
          workflowDefinitionId: workflowId,
          posicionX: node.position.x,
          posicionY: node.position.y,
        },
      });
    } catch {
      toast.error("Error al guardar posición");
    }
  }

  async function handleAddNode() {
    if (!newNodeEstadoId) {
      toast.error("Selecciona un estado");
      return;
    }
    try {
      await createNode.mutateAsync({
        workflowDefinitionId: workflowId,
        estadoGestionId: Number(newNodeEstadoId),
        tipo: newNodeTipo,
        posicionX: 100 + rawNodes.length * 160,
        posicionY: 100,
      });
      setAddNodeOpen(false);
      setNewNodeEstadoId("");
      setNewNodeTipo("INTERMEDIATE");
      refetchNodes();
      toast.success("Nodo agregado");
    } catch {
      toast.error("Error al agregar nodo");
    }
  }

  async function handleDeleteNode(nodeId: string) {
    try {
      await deleteNodeMut.mutateAsync({ id: Number(nodeId), workflowId });
      refetchNodes();
      refetchTransitions();
      toast.success("Nodo eliminado");
    } catch {
      toast.error("No se puede eliminar: el nodo tiene transiciones");
    }
  }

  async function handleDeleteEdge(edgeId: string) {
    try {
      await deleteTransitionMut.mutateAsync({ id: Number(edgeId), workflowId });
      refetchTransitions();
      toast.success("Transición eliminada");
    } catch {
      toast.error("Error al eliminar transición");
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
        toast.success("Workflow válido y consistente");
      } else {
        toast.error("Workflow con errores de consistencia");
      }
    } catch {
      toast.error("Error al validar");
    }
  }

  return (
    <div>
      <AppHeader
        title={workflow?.nombre ?? "Editor de Workflow"}
        description={workflow?.descripcion ?? ""}
        actions={
          <div className="flex gap-2">
            <Button variant="secondary" onClick={() => router.back()}>
              Volver
            </Button>
            <Button
              variant={editMode ? "default" : "secondary"}
              onClick={() => setEditMode((v) => !v)}
              data-testid="btn-toggle-edit"
            >
              {editMode ? "Modo Edición" : "Modo Vista"}
            </Button>
            {editMode && (
              <Button onClick={() => setAddNodeOpen(true)} data-testid="btn-add-node">
                + Agregar Nodo
              </Button>
            )}
            <Button variant="secondary" onClick={handleValidate} data-testid="btn-validate">
              Validar Workflow
            </Button>
          </div>
        }
      />

      {validationErrors.length > 0 && (
        <div className="mb-4 p-3 rounded-lg bg-red-50 border border-red-200" data-testid="validation-errors">
          <p className="text-sm font-semibold text-red-700 mb-1">Errores de consistencia:</p>
          <ul className="list-disc list-inside text-sm text-red-600">
            {validationErrors.map((e, i) => <li key={i}>{e}</li>)}
          </ul>
        </div>
      )}

      <div className="mb-3 flex gap-3 text-xs text-neutral-500">
        <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-full bg-green-200 border border-green-600 inline-block" />Inicial</span>
        <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-full bg-blue-200 border border-blue-600 inline-block" />Intermedio</span>
        <span className="flex items-center gap-1"><span className="w-3 h-3 rounded-full bg-red-200 border border-red-600 inline-block" />Final</span>
      </div>

      <div style={{ height: 520 }} className="rounded-xl border border-neutral-200 overflow-hidden" data-testid="workflow-editor">
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
        >
          <Background />
          <Controls />
          <MiniMap />
        </ReactFlow>
      </div>
      {editMode && (
        <p className="text-xs text-neutral-400 mt-1">
          Doble clic sobre un nodo o arista para eliminarlo. Arrastra desde el handle lateral para crear transiciones.
        </p>
      )}

      <Dialog open={addNodeOpen} onOpenChange={setAddNodeOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title="Agregar Nodo al Workflow">
              <FormField label="Estado de Gestión" required>
                <select
                  className="w-full border border-neutral-300 rounded-lg px-3 py-2 text-sm bg-white"
                  value={newNodeEstadoId}
                  onChange={(e) => setNewNodeEstadoId(e.target.value ? Number(e.target.value) : "")}
                  data-testid="select-estado-nodo"
                >
                  <option value="">Seleccionar estado...</option>
                  {estados.map((e) => (
                    <option key={e.idEstadoGestion} value={e.idEstadoGestion}>{e.nombre}</option>
                  ))}
                </select>
              </FormField>
              <FormField label="Tipo de Nodo" required>
                <select
                  className="w-full border border-neutral-300 rounded-lg px-3 py-2 text-sm bg-white"
                  value={newNodeTipo}
                  onChange={(e) => setNewNodeTipo(e.target.value as WorkflowNodeType)}
                  data-testid="select-tipo-nodo"
                >
                  <option value="INITIAL">Inicial</option>
                  <option value="INTERMEDIATE">Intermedio</option>
                  <option value="FINAL">Final</option>
                </select>
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setAddNodeOpen(false)}>Cancelar</Button>
              <Button onClick={handleAddNode} data-testid="btn-confirmar-nodo">Agregar</Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>
    </div>
  );
}
