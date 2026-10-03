import type { ComponentType } from "react";
import {
  ArrowRightLeft,
  BookMarked,
  Building2,
  Calculator,
  ClipboardList,
  Copy,
  CreditCard,
  FileBarChart,
  FileCheck2,
  FileText,
  FolderKanban,
  Home,
  Landmark,
  ListTodo,
  RotateCcw,
  ScrollText,
  Settings,
  ShieldCheck,
  Stamp,
  UserRoundCog,
  Users,
} from "lucide-react";

export type LucideIcon = ComponentType<{ className?: string }>;

export interface DashboardNavItem {
  labelKey: string;
  href: string;
  icon: LucideIcon;
  adminOnly?: boolean;
}

/**
 * Primary dashboard sidebar entries (issue #1058).
 * Suplencias and Reportes must be discoverable here — not only via deep links.
 */
export const DASHBOARD_NAV_ITEMS: DashboardNavItem[] = [
  { labelKey: "home", href: "/dashboard", icon: Home },
  { labelKey: "gestiones", href: "/dashboard/gestiones", icon: FolderKanban },
  {
    labelKey: "presupuestos",
    href: "/dashboard/presupuestos",
    icon: Calculator,
  },
  { labelKey: "personas", href: "/dashboard/personas", icon: Users },
  { labelKey: "escrituras", href: "/dashboard/escrituras", icon: ScrollText },
  { labelKey: "testimonios", href: "/dashboard/testimonios", icon: FileCheck2 },
  {
    labelKey: "movimientosTestimonio",
    href: "/dashboard/movimientos-testimonio",
    icon: ArrowRightLeft,
  },
  {
    labelKey: "documentosEntidadesExternas",
    href: "/dashboard/documentos-entidades-externas",
    icon: Landmark,
  },
  { labelKey: "pagos", href: "/dashboard/pagos", icon: CreditCard },
  { labelKey: "protocolo", href: "/dashboard/protocolo", icon: BookMarked },
  { labelKey: "inmuebles", href: "/dashboard/inmuebles", icon: Building2 },
  {
    labelKey: "minutasInscripcion",
    href: "/dashboard/minutas-inscripcion",
    icon: Stamp,
  },
  { labelKey: "copias", href: "/dashboard/copias", icon: Copy },
  {
    labelKey: "suplencias",
    href: "/dashboard/suplencias",
    icon: UserRoundCog,
  },
  { labelKey: "reportes", href: "/dashboard/reportes", icon: FileBarChart },
  { labelKey: "items", href: "/dashboard/items", icon: ListTodo },
  { labelKey: "documentos", href: "/dashboard/documentos", icon: FileText },
  {
    labelKey: "documentosNecesarios",
    href: "/dashboard/documentos-necesarios",
    icon: ClipboardList,
  },
  {
    labelKey: "reingresoDocumentacion",
    href: "/dashboard/reingreso-documentacion",
    icon: RotateCcw,
  },
  { labelKey: "auditoria", href: "/dashboard/auditoria", icon: ShieldCheck },
  {
    labelKey: "administracion",
    href: "/dashboard/administracion",
    icon: Settings,
    adminOnly: true,
  },
];

/** Canonical routes that replaced duplicate administración pages (#1058). */
export const CANONICAL_ADMIN_REDIRECTS = [
  {
    source: "/dashboard/administracion/items",
    destination: "/dashboard/items",
  },
  {
    source: "/dashboard/administracion/auditoria",
    destination: "/dashboard/auditoria",
  },
] as const;
