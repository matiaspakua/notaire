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
import { FormContainer, FormSection, FormField, FormActions } from "@/theme/form-patterns";
import { useQuery } from "@tanstack/react-query";
import { apiGet, apiPost, apiPut, apiDelete } from "@/lib/api-client";
import type { PlantillaPresupuesto, TipoDeTramite, Concepto } from "@/types";

interface PlantillaKey {
  tipoTramiteId: number;
  conceptoId: number;
}

export default function PlantillasPage() {
  const t = useTranslations("administracion.plantillas");
  const tc = useTranslations("common");

  const { data = [], isLoading, refetch } = useQuery({
    queryKey: ["plantilla-presupuestos"],
    queryFn: () => apiGet<PlantillaPresupuesto[]>("/plantilla-presupuestos"),
  });
  const { data: tiposTramite = [] } = useQuery({
    queryKey: ["tipo-tramite"],
    queryFn: () => apiGet<TipoDeTramite[]>("/tipo-tramite"),
  });
  const { data: conceptos = [] } = useQuery({
    queryKey: ["conceptos"],
    queryFn: () => apiGet<Concepto[]>("/conceptos"),
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [tipoTramiteId, setTipoTramiteId] = useState("");
  const [conceptoId, setConceptoId] = useState("");
  const [observaciones, setObservaciones] = useState("");
  const [deleteKey, setDeleteKey] = useState<PlantillaKey | null>(null);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);

  function openCreate() {
    setIsEditMode(false);
    setTipoTramiteId("");
    setConceptoId("");
    setObservaciones("");
    setModalOpen(true);
  }

  function openEdit(p: PlantillaPresupuesto) {
    setIsEditMode(true);
    setTipoTramiteId(String(p.plantillaPresupuestoPK?.fkIdTipoTramite ?? ""));
    setConceptoId(String(p.plantillaPresupuestoPK?.fkIdConcepto ?? ""));
    setObservaciones(p.observaciones ?? "");
    setModalOpen(true);
  }

  async function handleSave() {
    const tt = Number(tipoTramiteId);
    const cc = Number(conceptoId);
    if (!tt || !cc) {
      toast.error(t("selectionRequired"));
      return;
    }
    setSaving(true);
    try {
      // The backend derives the composite PK from the related entities. It reads
      // TipoDeTramite via getIdTipoTramite(), so the relation must carry the
      // `idTipoTramite` key (the typed field is idTipoDeTramite — hence the cast).
      const body = {
        plantillaPresupuestoPK: { fkIdTipoTramite: tt, fkIdConcepto: cc },
        tipoDeTramite: { idTipoTramite: tt },
        concepto: { idConcepto: cc },
        observaciones: observaciones.trim() || undefined,
      } as unknown as PlantillaPresupuesto;
      if (isEditMode) {
        await apiPut(`/plantilla-presupuestos/tipo-tramite/${tt}/concepto/${cc}`, body);
        toast.success(t("updated"));
      } else {
        await apiPost("/plantilla-presupuestos", body);
        toast.success(t("created"));
      }
      setModalOpen(false);
      refetch();
    } catch {
      toast.error(t("errorCreate"));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteKey) return;
    setDeleting(true);
    try {
      await apiDelete(`/plantilla-presupuestos/tipo-tramite/${deleteKey.tipoTramiteId}/concepto/${deleteKey.conceptoId}`);
      toast.success(t("deleted"));
      refetch();
    } catch {
      toast.error(t("errorDelete"));
    } finally {
      setDeleting(false);
      setDeleteKey(null);
    }
  }

  const tramiteName = (id?: number) => tiposTramite.find((x) => x.idTipoDeTramite === id)?.nombre ?? `#${id}`;
  const conceptoName = (id?: number) => conceptos.find((x) => x.idConcepto === id)?.nombre ?? `#${id}`;

  const columns: Column<PlantillaPresupuesto>[] = [
    {
      key: "tramite",
      header: t("fields.tipoTramite"),
      render: (p) => <span className="font-medium">{p.tipoDeTramite?.nombre ?? tramiteName(p.plantillaPresupuestoPK?.fkIdTipoTramite)}</span>,
    },
    {
      key: "concepto",
      header: t("fields.concepto"),
      render: (p) => p.concepto?.nombre ?? conceptoName(p.plantillaPresupuestoPK?.fkIdConcepto),
    },
    { key: "obs", header: t("fields.observaciones"), render: (p) => p.observaciones ?? "—" },
    {
      key: "actions",
      header: "",
      className: "w-24",
      render: (p) => (
        <div className="flex gap-2 justify-end">
          <Button size="sm" variant="ghost" onClick={() => openEdit(p)} data-testid="btn-edit-plantilla" aria-label={tc("edit")}>
            <NotaireIcon src="/icons/actions/generar.png" alt={tc("edit")} size={16} />
          </Button>
          <Button
            size="sm"
            variant="ghost"
            className="text-destructive hover:text-destructive"
            onClick={() =>
              setDeleteKey({
                tipoTramiteId: p.plantillaPresupuestoPK!.fkIdTipoTramite,
                conceptoId: p.plantillaPresupuestoPK!.fkIdConcepto,
              })
            }
            data-testid="btn-delete-plantilla"
            aria-label={tc("delete")}
          >
            <NotaireIcon src="/icons/actions/borrar.png" alt={tc("delete")} size={16} />
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
          <Button onClick={openCreate} data-testid="btn-nueva-plantilla">
            <NotaireIcon src="/icons/actions/agregar.png" alt={tc("add")} size={16} className="mr-1" />
            {t("newPlantilla")}
          </Button>
        }
      />
      <DataTable
        data={data}
        columns={columns}
        isLoading={isLoading}
        keyExtractor={(p) => `${p.plantillaPresupuestoPK?.fkIdTipoTramite}-${p.plantillaPresupuestoPK?.fkIdConcepto}`}
        emptyMessage={t("noData")}
      />

      <Dialog open={modalOpen} onOpenChange={setModalOpen}>
        <DialogContent>
          <FormContainer>
            <FormSection title={isEditMode ? t("editPlantilla") : t("newPlantilla")}>
              <FormField label={t("fields.tipoTramite")} required>
                <Select value={tipoTramiteId} onValueChange={setTipoTramiteId}>
                  <SelectTrigger data-testid="select-tipo-tramite" disabled={isEditMode}>
                    <SelectValue placeholder={t("fields.tipoTramitePlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    {tiposTramite.map((tt) => (
                      <SelectItem key={tt.idTipoDeTramite} value={String(tt.idTipoDeTramite)}>
                        {tt.nombre}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={t("fields.concepto")} required>
                <Select value={conceptoId} onValueChange={setConceptoId}>
                  <SelectTrigger data-testid="select-concepto" disabled={isEditMode}>
                    <SelectValue placeholder={t("fields.conceptoPlaceholder")} />
                  </SelectTrigger>
                  <SelectContent>
                    {conceptos.map((c) => (
                      <SelectItem key={c.idConcepto} value={String(c.idConcepto)}>
                        {c.nombre}
                        {c.valor ? ` ($${c.valor})` : ""}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label={t("fields.observaciones")}>
                <Input
                  value={observaciones}
                  onChange={(e) => setObservaciones(e.target.value)}
                  data-testid="input-observaciones-plantilla"
                />
              </FormField>
            </FormSection>
            <FormActions align="right">
              <Button variant="secondary" onClick={() => setModalOpen(false)}>
                <NotaireIcon src="/icons/actions/cerrar.png" alt={tc("cancel")} size={16} className="mr-1" />
                {tc("cancel")}
              </Button>
              <Button onClick={handleSave} disabled={saving}>
                <NotaireIcon src="/icons/actions/guardar.png" alt={tc("save")} size={16} className="mr-1 brightness-0 invert" />
                {isEditMode ? tc("update") : tc("save")}
              </Button>
            </FormActions>
          </FormContainer>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteKey} onOpenChange={(v) => !v && setDeleteKey(null)} onConfirm={handleDelete} loading={deleting} />
    </div>
  );
}
