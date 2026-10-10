import type { ComponentType } from "react";
import {
  ArrowRightLeft,
  BookMarked,
  Building2,
  CalendarClock,
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
  /** Only for ADMIN users; must match `ADMIN_ROUTE_PREFIXES` (lib/admin-access.ts). */
  adminOnly?: boolean;
  /** Also shown as a card in the dashboard module grid (#1367). */
  featured?: boolean;
}

export interface DashboardNavGroup {
  /** Key under `navigation.groups.*`. */
  labelKey: string;
  items: DashboardNavItem[];
}

/** "Inicio" sits above the sections. */
export const DASHBOARD_NAV_HOME: DashboardNavItem = { labelKey: "home", href: "/dashboard", icon: Home };

/**
 * Sidebar sections (#1367, owner decision 2026-10-09). The dashboard module
 * grid is derived from the `featured` items, so the home page and the sidebar
 * agree on the app's areas. Suplencias and Reportes stay discoverable (#1058).
 * Items is not in the written proposal; it sits under Gestión.
 */
export const DASHBOARD_NAV_GROUPS: DashboardNavGroup[] = [
  {
    labelKey: "clientes",
    items: [
      { labelKey: "personas", href: "/dashboard/personas", icon: Users, featured: true },
      { labelKey: "inmuebles", href: "/dashboard/inmuebles", icon: Building2, featured: true },
    ],
  },
  {
    labelKey: "tramites",
    items: [
      { labelKey: "gestiones", href: "/dashboard/gestiones", icon: FolderKanban, featured: true },
      { labelKey: "presupuestos", href: "/dashboard/presupuestos", icon: Calculator, featured: true },
      { labelKey: "pagos", href: "/dashboard/pagos", icon: CreditCard, featured: true },
    ],
  },
  {
    labelKey: "documentacion",
    items: [
      { labelKey: "documentos", href: "/dashboard/documentos", icon: FileText, featured: true },
      { labelKey: "documentosNecesarios", href: "/dashboard/documentos-necesarios", icon: ClipboardList },
      { labelKey: "documentosEntidadesExternas", href: "/dashboard/documentos-entidades-externas", icon: Landmark },
      { labelKey: "reingresoDocumentacion", href: "/dashboard/reingreso-documentacion", icon: RotateCcw },
      { labelKey: "proximosVencimientos", href: "/dashboard/proximos-vencimientos", icon: CalendarClock },
    ],
  },
  {
    labelKey: "escriturasProtocolo",
    items: [
      { labelKey: "escrituras", href: "/dashboard/escrituras", icon: ScrollText, featured: true },
      { labelKey: "testimonios", href: "/dashboard/testimonios", icon: FileCheck2 },
      { labelKey: "movimientosTestimonio", href: "/dashboard/movimientos-testimonio", icon: ArrowRightLeft },
      { labelKey: "copias", href: "/dashboard/copias", icon: Copy, featured: true },
      { labelKey: "minutasInscripcion", href: "/dashboard/minutas-inscripcion", icon: Stamp },
      { labelKey: "protocolo", href: "/dashboard/protocolo", icon: BookMarked, featured: true },
    ],
  },
  {
    labelKey: "gestion",
    items: [
      { labelKey: "reportes", href: "/dashboard/reportes", icon: FileBarChart, featured: true },
      { labelKey: "auditoria", href: "/dashboard/auditoria", icon: ShieldCheck, adminOnly: true, featured: true },
      { labelKey: "suplencias", href: "/dashboard/suplencias", icon: UserRoundCog, featured: true },
      { labelKey: "items", href: "/dashboard/items", icon: ListTodo, featured: true },
    ],
  },
  {
    labelKey: "administracion",
    items: [
      { labelKey: "administracion", href: "/dashboard/administracion", icon: Settings, adminOnly: true, featured: true },
    ],
  },
];

/** Flat list (Inicio + every section item, in sidebar order). */
export const DASHBOARD_NAV_ITEMS: DashboardNavItem[] = [
  DASHBOARD_NAV_HOME,
  ...DASHBOARD_NAV_GROUPS.flatMap((g) => g.items),
];

/** Dashboard module grid cards: the featured nav items, in sidebar order. */
export const DASHBOARD_MODULES: DashboardNavItem[] = DASHBOARD_NAV_GROUPS.flatMap((g) => g.items).filter(
  (i) => i.featured,
);

/** Whether `href` is the current route (or a parent of it). */
export function isNavItemActive(pathname: string, href: string): boolean {
  return pathname === href || (href !== "/dashboard" && pathname.startsWith(`${href}/`));
}

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
