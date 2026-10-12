"use client";
/**
 * #1578: list view of the workflow. Two tables (statuses, transitions) with the same actions as
 * the canvas, built from native controls so the whole workflow can be defined with the keyboard
 * and a screen reader. Also the editing surface on phones (#1580).
 */
import { useState } from "react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { checkNewTransition, type TransitionProblem } from "@/lib/workflow-edit";
import type { WorkflowNodeType } from "@/types";
import type { PanelNode, PanelTransition } from "./WorkflowSelectionPanel";

export interface ListNode extends PanelNode {
  problem?: string;
}

export interface ListViewProps {
  nodes: ListNode[];
  transitions: PanelTransition[];
  statuses: { id: number; name: string }[];
  editMode: boolean;
  onAddNode: (statusId: number, type: WorkflowNodeType) => void;
  onChangeType: (nodeId: number, type: WorkflowNodeType) => void;
  onDeleteNode: (nodeId: number) => void;
  onAddTransition: (from: number, to: number, description: string) => void;
  onSaveDescription: (transitionId: number, description: string) => void;
  onDeleteTransition: (transitionId: number) => void;
}

const TYPES: WorkflowNodeType[] = ["INITIAL", "INTERMEDIATE", "FINAL"];
const FIELD = "w-full border border-input rounded-lg px-2 py-1.5 text-sm bg-background";

type Pending = { kind: "node" | "transition"; id: number; title: string; description: string } | null;

export function WorkflowListView(props: ListViewProps) {
  const t = useTranslations("administracion.workflows.editor");
  const tv = useTranslations("workflowViewer");
  const { nodes, transitions, statuses, editMode } = props;
  const name = (id?: number) => nodes.find((n) => n.id === id)?.label ?? `#${id}`;
  const [pending, setPending] = useState<Pending>(null);

  const [statusId, setStatusId] = useState<number | "">("");
  const [type, setType] = useState<WorkflowNodeType>("INTERMEDIATE");
  const [from, setFrom] = useState<number | "">("");
  const [to, setTo] = useState<number | "">("");
  const [desc, setDesc] = useState("");
  const [problem, setProblem] = useState<TransitionProblem | null>(null);
  const [drafts, setDrafts] = useState<Record<number, string>>({});

  const usedStatus = new Set(nodes.map((n) => n.label));
  function addNode() {
    if (statusId === "") return;
    props.onAddNode(statusId, type);
    setStatusId("");
    setType("INTERMEDIATE");
  }
  function addTransition() {
    const a = from === "" ? undefined : from;
    const b = to === "" ? undefined : to;
    const p = checkNewTransition(transitions, a, b);
    setProblem(p);
    if (p || a === undefined || b === undefined) return;
    props.onAddTransition(a, b, desc.trim());
    setTo("");
    setDesc("");
  }

  return (
    <div className="space-y-6" data-testid="wf-list-view">
      <section aria-labelledby="wf-list-nodes-title" className="space-y-2">
        <h2 id="wf-list-nodes-title" className="text-sm font-semibold">{t("list.statuses")}</h2>
        <div className="overflow-x-auto rounded-lg border border-border">
          <table className="w-full text-sm">
            <thead className="bg-muted/50 text-left">
              <tr>
                <th scope="col" className="p-2">{t("list.status")}</th>
                <th scope="col" className="p-2">{t("fields.tipo")}</th>
                <th scope="col" className="p-2">{t("panel.outgoing")}</th>
                {editMode && <th scope="col" className="p-2"><span className="sr-only">{t("list.actions")}</span></th>}
              </tr>
            </thead>
            <tbody>
              {nodes.map((n) => {
                const out = transitions.filter((x) => x.originNodeId === n.id);
                return (
                  <tr key={n.id} className="border-t border-border align-top" data-testid={`wf-list-node-${n.id}`}>
                    <th scope="row" className="p-2 text-left font-medium">
                      {n.label}
                      {n.problem && (
                        <span className="block text-xs text-destructive">⚠ {tv(`problem.${n.problem}`)}</span>
                      )}
                    </th>
                    <td className="p-2">
                      <select
                        className={FIELD}
                        aria-label={t("list.typeOf", { name: n.label })}
                        value={n.type}
                        disabled={!editMode}
                        onChange={(e) => props.onChangeType(n.id, e.target.value as WorkflowNodeType)}
                      >
                        {TYPES.map((ty) => <option key={ty} value={ty}>{t(`types.${ty}`)}</option>)}
                      </select>
                    </td>
                    <td className="p-2">
                      {out.length === 0 ? <span className="text-muted-foreground">{t("panel.none")}</span>
                        : out.map((x) => <span key={x.id} className="block">→ {name(x.destinationNodeId)}</span>)}
                    </td>
                    {editMode && (
                      <td className="p-2 text-right">
                        <Button
                          variant="destructive"
                          size="sm"
                          aria-label={t("list.deleteStatus", { name: n.label })}
                          data-testid={`wf-list-delete-node-${n.id}`}
                          onClick={() => {
                            const linked = transitions.some((x) => x.originNodeId === n.id || x.destinationNodeId === n.id);
                            setPending({
                              kind: "node", id: n.id,
                              title: t("panel.deleteNodeTitle", { name: n.label }),
                              description: linked ? t("panel.deleteNodeHasTransitions") : t("panel.deleteNodeDesc"),
                            });
                          }}
                        >
                          {t("list.delete")}
                        </Button>
                      </td>
                    )}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
        {editMode && (
          <fieldset className="flex flex-wrap items-end gap-2">
            <legend className="text-xs font-semibold mb-1">{t("addNodeTitle")}</legend>
            <label className="flex-1 min-w-40 text-xs space-y-1">
              <span>{t("fields.estado")}</span>
              <select className={FIELD} value={statusId} data-testid="wf-list-add-status"
                onChange={(e) => setStatusId(e.target.value ? Number(e.target.value) : "")}>
                <option value="">{t("fields.estadoPlaceholder")}</option>
                {statuses.filter((s) => !usedStatus.has(s.name)).map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
            </label>
            <label className="min-w-32 text-xs space-y-1">
              <span>{t("fields.tipo")}</span>
              <select className={FIELD} value={type} data-testid="wf-list-add-type"
                onChange={(e) => setType(e.target.value as WorkflowNodeType)}>
                {TYPES.map((ty) => <option key={ty} value={ty}>{t(`types.${ty}`)}</option>)}
              </select>
            </label>
            <Button size="sm" onClick={addNode} data-testid="wf-list-add-node">{t("list.addStatus")}</Button>
          </fieldset>
        )}
      </section>

      <section aria-labelledby="wf-list-tr-title" className="space-y-2">
        <h2 id="wf-list-tr-title" className="text-sm font-semibold">{t("list.transitions")}</h2>
        <div className="overflow-x-auto rounded-lg border border-border">
          <table className="w-full text-sm">
            <thead className="bg-muted/50 text-left">
              <tr>
                <th scope="col" className="p-2">{t("list.fromTo")}</th>
                <th scope="col" className="p-2">{t("panel.description")}</th>
                {editMode && <th scope="col" className="p-2"><span className="sr-only">{t("list.actions")}</span></th>}
              </tr>
            </thead>
            <tbody>
              {transitions.length === 0 && (
                <tr><td colSpan={3} className="p-2 text-muted-foreground">{t("panel.none")}</td></tr>
              )}
              {transitions.map((x) => {
                const id = x.id!;
                const label = `${name(x.originNodeId)} → ${name(x.destinationNodeId)}`;
                return (
                  <tr key={id} className="border-t border-border align-top" data-testid={`wf-list-transition-${id}`}>
                    <th scope="row" className="p-2 text-left font-medium">{label}</th>
                    <td className="p-2">
                      <input
                        className={FIELD}
                        aria-label={t("list.descriptionOf", { name: label })}
                        value={drafts[id] ?? x.description ?? ""}
                        maxLength={255}
                        disabled={!editMode}
                        onChange={(e) => setDrafts((d) => ({ ...d, [id]: e.target.value }))}
                      />
                    </td>
                    {editMode && (
                      <td className="p-2 text-right whitespace-nowrap space-x-1">
                        <Button size="sm" variant="secondary" data-testid={`wf-list-save-desc-${id}`}
                          aria-label={t("list.saveDescription", { name: label })}
                          onClick={() => props.onSaveDescription(id, (drafts[id] ?? x.description ?? "").trim())}>
                          {t("panel.save")}
                        </Button>
                        <Button size="sm" variant="destructive" data-testid={`wf-list-delete-transition-${id}`}
                          aria-label={t("list.deleteTransition", { name: label })}
                          onClick={() => setPending({
                            kind: "transition", id,
                            title: t("panel.deleteTransitionTitle"),
                            description: t("panel.deleteTransitionDesc", { from: name(x.originNodeId), to: name(x.destinationNodeId) }),
                          })}>
                          {t("list.delete")}
                        </Button>
                      </td>
                    )}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
        {editMode && (
          <fieldset className="flex flex-wrap items-end gap-2">
            <legend className="text-xs font-semibold mb-1">{t("panel.addTransition")}</legend>
            <label className="min-w-36 flex-1 text-xs space-y-1">
              <span>{t("list.from")}</span>
              <select className={FIELD} value={from} data-testid="wf-list-add-from"
                onChange={(e) => { setFrom(e.target.value ? Number(e.target.value) : ""); setProblem(null); }}>
                <option value="">{t("list.fromPlaceholder")}</option>
                {nodes.map((n) => <option key={n.id} value={n.id}>{n.label}</option>)}
              </select>
            </label>
            <label className="min-w-36 flex-1 text-xs space-y-1">
              <span>{t("panel.to")}</span>
              <select className={FIELD} value={to} data-testid="wf-list-add-to"
                onChange={(e) => { setTo(e.target.value ? Number(e.target.value) : ""); setProblem(null); }}>
                <option value="">{t("panel.toPlaceholder")}</option>
                {nodes.map((n) => <option key={n.id} value={n.id}>{n.label}</option>)}
              </select>
            </label>
            <label className="min-w-36 flex-1 text-xs space-y-1">
              <span>{t("panel.description")}</span>
              <input className={FIELD} value={desc} maxLength={255} data-testid="wf-list-add-desc"
                onChange={(e) => setDesc(e.target.value)} />
            </label>
            <Button size="sm" onClick={addTransition} data-testid="wf-list-add-transition">{t("panel.add")}</Button>
            {problem && (
              <p role="alert" className="w-full text-xs text-destructive" data-testid="wf-list-add-error">
                {t(`panel.problem.${problem}`)}
              </p>
            )}
          </fieldset>
        )}
      </section>

      <ConfirmDialog
        open={pending !== null}
        onOpenChange={(v) => !v && setPending(null)}
        title={pending?.title}
        description={pending?.description}
        onConfirm={() => {
          if (!pending) return;
          if (pending.kind === "node") props.onDeleteNode(pending.id);
          else props.onDeleteTransition(pending.id);
          setPending(null);
        }}
      />
    </div>
  );
}
