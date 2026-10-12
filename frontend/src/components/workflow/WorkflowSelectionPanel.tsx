"use client";
/**
 * #1576: side panel for the selected status or transition. Every edit has a visible control
 * (no hidden double-click deletes) and every delete asks for confirmation.
 */
import { useState } from "react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { checkNewTransition, type TransitionProblem } from "@/lib/workflow-edit";
import type { WorkflowNodeType } from "@/types";

export type PanelSelection = { kind: "node"; id: number } | { kind: "edge"; id: number } | null;

export interface PanelNode {
  id: number;
  label: string;
  type: string;
}

export interface PanelTransition {
  id?: number;
  originNodeId?: number;
  destinationNodeId?: number;
  description?: string;
}

export interface PanelProps {
  selection: PanelSelection;
  nodes: PanelNode[];
  transitions: PanelTransition[];
  editMode: boolean;
  onChangeType: (nodeId: number, type: WorkflowNodeType) => void;
  onDeleteNode: (nodeId: number) => void;
  onDeleteTransition: (transitionId: number) => void;
  onAddTransition: (from: number, to: number, description: string) => void;
  onSaveDescription: (transitionId: number, description: string) => void;
}

const TYPES: WorkflowNodeType[] = ["INITIAL", "INTERMEDIATE", "FINAL"];
const FIELD = "w-full border border-input rounded-lg px-3 py-2 text-sm bg-background";

export function WorkflowSelectionPanel(props: PanelProps) {
  const t = useTranslations("administracion.workflows.editor.panel");
  const { selection, nodes, transitions } = props;
  const name = (id?: number) => nodes.find((n) => n.id === id)?.label ?? `#${id}`;

  return (
    <aside
      className="rounded-xl border border-border p-4 text-sm space-y-3"
      aria-label={t("title")}
      data-testid="wf-panel"
    >
      {selection === null && (
        <p className="text-muted-foreground" data-testid="wf-panel-empty">{t("empty")}</p>
      )}
      {selection?.kind === "node" && (
        <NodePanel key={`n${selection.id}`} {...props} nodeId={selection.id} name={name} />
      )}
      {selection?.kind === "edge" && (() => {
        const tr = transitions.find((x) => x.id === selection.id);
        return tr ? <EdgePanel key={`e${selection.id}:${tr.description ?? ""}`} {...props} tr={tr} name={name} /> : null;
      })()}
    </aside>
  );
}

function NodePanel({
  nodeId, nodes, transitions, editMode, onChangeType, onDeleteNode, onAddTransition, name,
}: PanelProps & { nodeId: number; name: (id?: number) => string }) {
  const t = useTranslations("administracion.workflows.editor");
  const node = nodes.find((n) => n.id === nodeId);
  const [to, setTo] = useState<number | "">("");
  const [desc, setDesc] = useState("");
  const [problem, setProblem] = useState<TransitionProblem | null>(null);
  const [confirm, setConfirm] = useState(false);
  if (!node) return null;
  const outgoing = transitions.filter((x) => x.originNodeId === nodeId);
  const incoming = transitions.filter((x) => x.destinationNodeId === nodeId);
  const linked = outgoing.length + incoming.length > 0;

  function add() {
    const target = to === "" ? undefined : to;
    const p = checkNewTransition(transitions, nodeId, target);
    setProblem(p);
    if (p || target === undefined) return;
    onAddTransition(nodeId, target, desc.trim());
    setTo("");
    setDesc("");
  }

  return (
    <div className="space-y-3" data-testid="wf-panel-node">
      <h3 className="font-semibold">{node.label}</h3>
      <label className="block space-y-1">
        <span className="text-xs text-muted-foreground">{t("fields.tipo")}</span>
        <select
          className={FIELD}
          value={node.type}
          disabled={!editMode}
          onChange={(e) => onChangeType(nodeId, e.target.value as WorkflowNodeType)}
          data-testid="wf-panel-type"
        >
          {TYPES.map((ty) => <option key={ty} value={ty}>{t(`types.${ty}`)}</option>)}
        </select>
      </label>
      <TransitionList title={t("panel.outgoing")} testId="wf-panel-outgoing"
        items={outgoing.map((x) => `→ ${name(x.destinationNodeId)}`)} empty={t("panel.none")} />
      <TransitionList title={t("panel.incoming")} testId="wf-panel-incoming"
        items={incoming.map((x) => `${name(x.originNodeId)} →`)} empty={t("panel.none")} />
      {editMode && (
        <>
          <fieldset className="space-y-2 border-t border-border pt-3">
            <legend className="text-xs font-semibold">{t("panel.addTransition")}</legend>
            <select
              className={FIELD}
              aria-label={t("panel.to")}
              value={to}
              onChange={(e) => { setTo(e.target.value ? Number(e.target.value) : ""); setProblem(null); }}
              data-testid="wf-panel-add-to"
            >
              <option value="">{t("panel.toPlaceholder")}</option>
              {nodes.filter((n) => n.id !== nodeId).map((n) => (
                <option key={n.id} value={n.id}>{n.label}</option>
              ))}
            </select>
            <input
              className={FIELD}
              aria-label={t("panel.description")}
              placeholder={t("panel.description")}
              value={desc}
              maxLength={255}
              onChange={(e) => setDesc(e.target.value)}
              data-testid="wf-panel-add-desc"
            />
            {problem && (
              <p role="alert" className="text-xs text-destructive" data-testid="wf-panel-add-error">
                {t(`panel.problem.${problem}`)}
              </p>
            )}
            <Button size="sm" onClick={add} data-testid="wf-panel-add-transition">{t("panel.add")}</Button>
          </fieldset>
          <Button variant="destructive" size="sm" onClick={() => setConfirm(true)} data-testid="wf-panel-delete-node">
            {t("panel.deleteNode")}
          </Button>
          <ConfirmDialog
            open={confirm}
            onOpenChange={setConfirm}
            title={t("panel.deleteNodeTitle", { name: node.label })}
            description={linked ? t("panel.deleteNodeHasTransitions") : t("panel.deleteNodeDesc")}
            onConfirm={() => { setConfirm(false); onDeleteNode(nodeId); }}
          />
        </>
      )}
    </div>
  );
}

function EdgePanel({
  tr, editMode, onDeleteTransition, onSaveDescription, name,
}: PanelProps & { tr: PanelTransition; name: (id?: number) => string }) {
  const t = useTranslations("administracion.workflows.editor.panel");
  const [desc, setDesc] = useState(tr.description ?? "");
  const [confirm, setConfirm] = useState(false);
  const id = tr.id!;
  return (
    <div className="space-y-3" data-testid="wf-panel-edge">
      <h3 className="font-semibold">{name(tr.originNodeId)} → {name(tr.destinationNodeId)}</h3>
      <label className="block space-y-1">
        <span className="text-xs text-muted-foreground">{t("description")}</span>
        <input
          className={FIELD}
          value={desc}
          maxLength={255}
          disabled={!editMode}
          onChange={(e) => setDesc(e.target.value)}
          data-testid="wf-panel-desc"
        />
      </label>
      {editMode && (
        <div className="flex gap-2">
          <Button size="sm" onClick={() => onSaveDescription(id, desc.trim())} data-testid="wf-panel-save-desc">
            {t("save")}
          </Button>
          <Button variant="destructive" size="sm" onClick={() => setConfirm(true)} data-testid="wf-panel-delete-edge">
            {t("deleteTransition")}
          </Button>
          <ConfirmDialog
            open={confirm}
            onOpenChange={setConfirm}
            title={t("deleteTransitionTitle")}
            description={t("deleteTransitionDesc", { from: name(tr.originNodeId), to: name(tr.destinationNodeId) })}
            onConfirm={() => { setConfirm(false); onDeleteTransition(id); }}
          />
        </div>
      )}
    </div>
  );
}

function TransitionList({ title, items, empty, testId }: { title: string; items: string[]; empty: string; testId: string }) {
  return (
    <div data-testid={testId}>
      <p className="text-xs font-semibold text-muted-foreground">{title}</p>
      {items.length === 0 ? (
        <p className="text-xs text-muted-foreground">{empty}</p>
      ) : (
        <ul className="text-sm">{items.map((s, i) => <li key={i}>{s}</li>)}</ul>
      )}
    </div>
  );
}
