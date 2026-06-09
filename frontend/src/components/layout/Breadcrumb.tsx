"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const SEGMENT_LABELS: Record<string, string> = {
  dashboard: "Dashboard",
  administracion: "Administración",
  usuarios: "Usuarios",
  tramites: "Tipos de Trámite",
  documentos: "Tipos de Documento",
  folios: "Tipos de Folio",
  conceptos: "Conceptos",
  "estados-gestion": "Estados de Gestión",
  plantillas: "Plantillas",
  items: "Ítems",
  auditoria: "Auditoría",
  workflows: "Workflows",
  suplencias: "Suplencias",
  personas: "Personas",
  escrituras: "Escrituras",
  gestiones: "Gestiones",
  protocolo: "Protocolo",
  presupuestos: "Presupuestos",
  pagos: "Pagos",
  copias: "Copias",
  reportes: "Reportes",
  inmuebles: "Inmuebles",
};

function labelForSegment(segment: string): string {
  return SEGMENT_LABELS[segment] ?? segment.replace(/-/g, " ").replace(/\b\w/g, (c) => c.toUpperCase());
}

export function Breadcrumb() {
  const pathname = usePathname();
  const segments = pathname.split("/").filter(Boolean);

  if (segments.length <= 1) return null;

  const crumbs = segments.map((seg, i) => ({
    label: labelForSegment(seg),
    href: "/" + segments.slice(0, i + 1).join("/"),
    isLast: i === segments.length - 1,
  }));

  return (
    <nav aria-label="Breadcrumb" data-testid="breadcrumb">
      <ol className="flex items-center gap-1 text-sm text-muted-foreground flex-wrap">
        {crumbs.map((crumb, i) => (
          <li key={crumb.href} className="flex items-center gap-1">
            {i > 0 && <span className="text-neutral-300 select-none">/</span>}
            {crumb.isLast ? (
              <span className="font-medium text-foreground">{crumb.label}</span>
            ) : (
              <Link
                href={crumb.href}
                className="hover:text-foreground transition-colors"
              >
                {crumb.label}
              </Link>
            )}
          </li>
        ))}
      </ol>
    </nav>
  );
}
