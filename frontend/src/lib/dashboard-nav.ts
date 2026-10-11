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

/** Home sits above the sections. */
export const DASHBOARD_NAV_HOME: DashboardNavItem = { labelKey: "home", href: "/dashboard", icon: Home };

/**
 * Sidebar sections (#1367, owner decision 2026-10-09). The dashboard module
 * grid is derived from the `featured` items, so the home page and the sidebar
 * agree on the app's areas. Substitutions and Reports stay discoverable (#1058).
 * Items is not in the written proposal; it sits under the management group.
 */
export const DASHBOARD_NAV_GROUPS: DashboardNavGroup[] = [
  {
    labelKey: "clientes",
    items: [
      { labelKey: "personas", href: "/dashboard/people", icon: Users, featured: true },
      { labelKey: "inmuebles", href: "/dashboard/properties", icon: Building2, featured: true },
    ],
  },
  {
    labelKey: "tramites",
    items: [
      { labelKey: "gestiones", href: "/dashboard/managements", icon: FolderKanban, featured: true },
      { labelKey: "presupuestos", href: "/dashboard/budgets", icon: Calculator, featured: true },
      { labelKey: "pagos", href: "/dashboard/payments", icon: CreditCard, featured: true },
    ],
  },
  {
    labelKey: "documentacion",
    items: [
      { labelKey: "documentos", href: "/dashboard/documents", icon: FileText, featured: true },
      { labelKey: "documentosNecesarios", href: "/dashboard/required-documents", icon: ClipboardList },
      { labelKey: "documentosEntidadesExternas", href: "/dashboard/external-entity-documents", icon: Landmark },
      { labelKey: "reingresoDocumentacion", href: "/dashboard/document-reentry", icon: RotateCcw },
      { labelKey: "proximosVencimientos", href: "/dashboard/upcoming-expirations", icon: CalendarClock },
    ],
  },
  {
    labelKey: "escriturasProtocolo",
    items: [
      { labelKey: "escrituras", href: "/dashboard/deeds", icon: ScrollText, featured: true },
      { labelKey: "testimonios", href: "/dashboard/testimonies", icon: FileCheck2 },
      { labelKey: "movimientosTestimonio", href: "/dashboard/testimony-movements", icon: ArrowRightLeft },
      { labelKey: "copias", href: "/dashboard/copies", icon: Copy, featured: true },
      { labelKey: "minutasInscripcion", href: "/dashboard/registration-drafts", icon: Stamp },
      { labelKey: "protocolo", href: "/dashboard/protocol", icon: BookMarked, featured: true },
    ],
  },
  {
    labelKey: "gestion",
    items: [
      { labelKey: "reportes", href: "/dashboard/reports", icon: FileBarChart, featured: true },
      { labelKey: "auditoria", href: "/dashboard/audit", icon: ShieldCheck, adminOnly: true, featured: true },
      { labelKey: "suplencias", href: "/dashboard/substitutions", icon: UserRoundCog, featured: true },
      { labelKey: "items", href: "/dashboard/items", icon: ListTodo, featured: true },
    ],
  },
  {
    labelKey: "administracion",
    items: [
      { labelKey: "administracion", href: "/dashboard/administration", icon: Settings, adminOnly: true, featured: true },
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

/** Canonical routes that replaced duplicate administration pages (#1058). */
export const CANONICAL_ADMIN_REDIRECTS = [
  {
    source: "/dashboard/administration/items",
    destination: "/dashboard/items",
  },
  {
    source: "/dashboard/administration/audit",
    destination: "/dashboard/audit",
  },
] as const;
