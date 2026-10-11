"use client";
import { useState } from "react";
import { toast } from "sonner";
import { useTranslations } from "next-intl";
import { NotaireIcon } from "@/components/ui/notaire-icon";
import { AppHeader } from "@/components/layout/AppHeader";
import { DataTable, type Column } from "@/components/shared/DataTable";
import { ConfirmDialog } from "@/components/shared/ConfirmDialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { FormContainer, FormSection, FormField, FormActions, RadioField } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import { presentMutationError } from "@/lib/mutation-error";
import type { BudgetTemplate, ProcedureType, Concept, DocumentType, DocumentCostTemplate } from "@/types";
import { useDeleteError } from "@/hooks/useDeleteError";

interface TemplateKey {
  procedureTypeId: number;
  conceptId: number;
}

export default function TemplatesPage() {
  const t = useTranslations("administracion.plantillas");
  const showDeleteError = useDeleteError();
  const tc = useTranslations("common");

  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["budget-templates"],
    queryFn: () => apiGet<BudgetTemplate[]>("/budget-templates"),
  });
  const { data: procedureTypes = [] } = useQuery({
    queryKey: ["procedure-types"],
    queryFn: () => apiGet<ProcedureType[]>("/procedure-types"),
  });
  const { data: concepts = [] } = useQuery({
    queryKey: ["concepts"],
    queryFn: () => apiGet<Concept[]>("/concepts"),
  });
  const { data: documentTypes = [] } = useQuery({
    queryKey: ["document-types"],
    queryFn: () => apiGet<DocumentType[]>("/document-types"),
  });

  const [costProcedureId, setCostProcedureId] = useState("");
  const {
    data: documentCosts = [],
    isLoading: isLoadingCosts,
    refetch: refetchCosts,
  } = useQuery({
    queryKey: ["document-cost-templates", costProcedureId],
    queryFn: () =>
      apiGet<DocumentCostTemplate[]>(`/document-cost-templates/procedure-types/${costProcedureId}`),
    enabled: !!costProcedureId,
  });
  const [costModalOpen, setCostModalOpen] = useState(false);
  const [costDocumentTypeId, setCostDocumentTypeId] = useState("");
  const [costKind, setCostKind] = useState<"fixed" | "variable">("fixed");
  const [costAmount, setCostAmount] = useState("");
  const [savingCost, setSavingCost] = useState(false);

  function openCreateCost() {
    setCostDocumentTypeId("");
    setCostKind("fixed");
    setCostAmount("");
    setCostModalOpen(true);
  }

  async function handleSaveCost() {
    const idTypeDocument = Number(costDocumentTypeId);
    const amount = Number(costAmount);
    if (!costProcedureId || !idTypeDocument || !amount) {
      toast.error(t("costosDocumento.selectionRequired"));
      return;
    }
    setSavingCost(true);
    try {
      await apiPost("/document-cost-templates", {
        idProcedureType: Number(costProcedureId),
        idDocumentType: idTypeDocument,
        fixedAmount: costKind === "fixed" ? amount : null,
        variablePercentage: costKind === "variable" ? amount : null,
      });
      toast.success(t("costosDocumento.created"));
      setCostModalOpen(false);
      refetchCosts();
    } catch (err) {
      presentMutationError(err, { fallback: t("costosDocumento.errorCreate") });
    } finally {
      setSavingCost(false);
    }
  }

  const typeDocumentName = (id?: number) =>
    documentTypes.find((x) => x.idDocumentType === id)?.name ?? `#${id}`;

  const [modalOpen, setModalOpen] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [procedureTypeId, setProcedureTypeId] = useState("");
  const [conceptId, setConceptId] = useState("");
  const [notes, setNotes] = useState("");
  const [deleteKey, setDeleteKey] = useState<TemplateKey | null>(null);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);

  function openCreate() {
    setIsEditMode(false);
    setProcedureTypeId("");
    setConceptId("");
    setNotes("");
    setModalOpen(true);
  }

  function openEdit(p: BudgetTemplate) {
    setIsEditMode(true);
    setProcedureTypeId(String(p.budgetTemplatePK?.fkIdProcedureType ?? ""));
    setConceptId(String(p.budgetTemplatePK?.fkIdConcept ?? ""));
    setNotes(p.notes ?? "");
    setModalOpen(true);
  }

  async function handleSave() {
    const tt = Number(procedureTypeId);
    const cc = Number(conceptId);
    if (!tt || !cc) {
      toast.error(t("selectionRequired"));
      return;
    }
    setSaving(true);
    try {
      // The backend derives the composite PK from the related entities. It reads
      // ProcedureType via getIdProcedureType(), so the relation must carry the
      // idProcedureType key.
      const body: BudgetTemplate = {
        budgetTemplatePK: { fkIdProcedureType: tt, fkIdConcept: cc },
        procedureType: { idProcedureType: tt },
        concept: { idConcept: cc },
        notes: notes.trim() || undefined,
      };
      if (isEditMode) {
        await apiPut(`/budget-templates/procedure-types/${tt}/concept/${cc}`, body);
        toast.success(t("updated"));
      } else {
        await apiPost("/budget-templates", body);
        toast.success(t("created"));
      }
      setModalOpen(false);
      refetch();
    } catch (err) {
      presentMutationError(err, { fallback: t("errorCreate") });
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteKey) return;
    setDeleting(true);
    try {
      await apiDelete(`/budget-templates/procedure-types/${deleteKey.procedureTypeId}/concept/${deleteKey.conceptId}`);
      toast.success(t("deleted"));
      refetch();
    } catch (err) {
      showDeleteError(err, t("errorDelete"));
    } finally {
      setDeleting(false);
      setDeleteKey(null);
    }
  }

  const procedureName = (id?: number) => procedureTypes.find((x) => x.idProcedureType === id)?.name ?? `#${id}`;
  const conceptName = (id?: number) => concepts.find((x) => x.idConcept === id)?.name ?? `#${id}`;

  const columns: Column<BudgetTemplate>[] = [
    {
      key: "procedure",
      header: t("fields.tipoTramite"),
      render: (p) => <span className="font-medium">{p.procedureType?.name ?? procedureName(p.budgetTemplatePK?.fkIdProcedureType)}</span>,
    },
    {
      key: "concept",
      header: t("fields.concepto"),
      render: (p) => p.concept?.name ?? conceptName(p.budgetTemplatePK?.fkIdConcept),
    },
    { key: "notes", header: t("fields.observaciones"), render: (p) => p.notes ?? "—" },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (p) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(p)} data-testid="btn-edit-template" aria-label={tc("edit")}>
            <NotaireIcon src="/icons/actions/generate.png" alt={tc("edit")} size={16} />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() =>
              setDeleteKey({
                procedureTypeId: p.budgetTemplatePK!.fkIdProcedureType,
                conceptId: p.budgetTemplatePK!.fkIdConcept,
              })
            }
            data-testid="btn-delete-template"
            aria-label={tc("delete")}
          >
            <NotaireIcon src="/icons/actions/delete.png" alt={tc("delete")} size={16} />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <AppHeader
        title={t("title")}
        description={t("description")}
        actions={
          <Button onClick={openCreate} data-testid="btn-new-template">
            <NotaireIcon src="/icons/actions/add.png" alt={tc("add")} size={16} className="mr-1" />
            {t("newPlantilla")}
          </Button>
        }
      />
      <DataTable
        data={data}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(p) => `${p.budgetTemplatePK?.fkIdProcedureType}-${p.budgetTemplatePK?.fkIdConcept}`}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={isEditMode ? t("editPlantilla") : t("newPlantilla")}>
              <FormField label={t("fields.tipoTramite")} required>
                <Select value={procedureTypeId} onValueChange={setProcedureTypeId}>
                  <SelectTrigger data-testid="select-procedure-type" disabled={isEditMode}>
                    <SelectValue placeholder={t("fields.tipoTramitePlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    {procedureTypes.map((tt) => (
                      <SelectItem key={tt.idProcedureType} value={String(tt.idProcedureType)}>
                        {tt.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={t("fields.concepto")} required>
                <Select value={conceptId} onValueChange={setConceptId}>
                  <SelectTrigger data-testid="select-concept" disabled={isEditMode}>
                    <SelectValue placeholder={t("fields.conceptoPlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    {concepts.map((c) => (
                      <SelectItem key={c.idConcept} value={String(c.idConcept)}>
                        {c.name}
                        {c.value ? ` ($${c.value})` : ""}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={t("fields.observaciones")}>
                <Input
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  data-testid="input-notes-template"
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cancel.png" alt={tc("cancel")} size={16} className="mr-1" />
                {tc("cancel")}
              </Button>
              <Button onClick={handleSave} disabled={saving}>
                <NotaireIcon src="/icons/actions/save.png" alt={tc("save")} size={16} className="mr-1 brightness-0 invert" />
                {isEditMode ? tc("update") : tc("save")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteKey} onOpenChange={(v) => !v && setDeleteKey(null)} onConfirm={handleDelete} loading={deleting} />

      <AppHeader title={t("costosDocumento.title")} description={t("costosDocumento.description")} />
      <FormField label={t("costosDocumento.fields.tipoTramite")}>
        <Select value={costProcedureId} onValueChange={setCostProcedureId}>
          <SelectTrigger data-testid="select-procedure-type-costs">
            <SelectValue placeholder={t("fields.tipoTramitePlaceholder")} />
          </SelectTrigger>
          <SelectContent>
            {procedureTypes.map((tt) => (
              <SelectItem key={tt.idProcedureType} value={String(tt.idProcedureType)}>
                {tt.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </FormField>

      {costProcedureId && (
        <>
          <div className="flex justify-end mb-4">
            <Button onClick={openCreateCost} data-testid="btn-new-cost-document">
              <NotaireIcon src="/icons/actions/add.png" alt={tc("add")} size={16} className="mr-1" />
              {t("costosDocumento.newCosto")}
            </Button>
          </div>
          <DataTable
            data={documentCosts}
            isLoading={isLoadingCosts}
            emptyMessage={t("costosDocumento.noData")}
            keyExtractor={(c) =>
              `${c.documentCostTemplatePK?.fkIdProcedureType}-${c.documentCostTemplatePK?.fkIdDocumentType}`
            }
            columns={[
              {
                key: "documentType",
                header: t("costosDocumento.fields.tipoDocumento"),
                render: (c) =>
                  c.documentType?.name ?? typeDocumentName(c.documentCostTemplatePK?.fkIdDocumentType),
              },
              {
                key: "fixedAmount",
                header: t("costosDocumento.fields.montoFijo"),
                render: (c) => (c.fixedAmount != null ? `$${c.fixedAmount}` : "—"),
              },
              {
                key: "variablePercentage",
                header: t("costosDocumento.fields.porcentajeVariable"),
                render: (c) => (c.variablePercentage != null ? `${c.variablePercentage}%` : "—"),
              },
            ]}
          />
        </>
      )}

      <Dialog open={costModalOpen} onOpenChange={setCostModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection dialogTitle title={t("costosDocumento.newCosto")}>
              <FormField label={t("costosDocumento.fields.tipoDocumento")} required>
                <Select value={costDocumentTypeId} onValueChange={setCostDocumentTypeId}>
                  <SelectTrigger data-testid="select-document-type-cost">
                    <SelectValue placeholder={t("costosDocumento.fields.tipoDocumentoPlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    {documentTypes.map((td) => (
                      <SelectItem key={td.idDocumentType} value={String(td.idDocumentType)}>
                        {td.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <RadioField
                label={t("costosDocumento.fields.tipoCosto")}
                selected={costKind}
                onChange={(v) => setCostKind(v as "fixed" | "variable")}
                options={[
                  { value: "fixed", label: t("costosDocumento.fields.montoFijo") },
                  { value: "variable", label: t("costosDocumento.fields.porcentajeVariable") },
                ]}
              />
              <FormField label={t("costosDocumento.fields.valor")} required>
                <Input
                  type="number"
                  value={costAmount}
                  onChange={(e) => setCostAmount(e.target.value)}
                  data-testid="input-value-cost-document"
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setCostModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cancel.png" alt={tc("cancel")} size={16} className="mr-1" />
                {tc("cancel")}
              </Button>
              <Button onClick={handleSaveCost} disabled={savingCost} data-testid="btn-save-cost-document">
                <NotaireIcon src="/icons/actions/save.png" alt={tc("save")} size={16} className="mr-1 brightness-0 invert" />
                {tc("save")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>
    </div>
  );
}
