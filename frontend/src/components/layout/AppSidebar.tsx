"use client";

import Link from "next/link";
import { useState } from "react";
import { usePathname, useRouter } from "next/navigation";
import { ChevronDown, LogOut, Scale, X } from "lucide-react";
import * as DialogPrimitive from "@radix-ui/react-dialog";
import { useTranslations } from "next-intl";
import { motion } from "motion/react";
import { cn } from "@/lib/utils";
import { apiLogout } from "@/lib/api-client";
import {
  DASHBOARD_NAV_GROUPS,
  DASHBOARD_NAV_HOME,
  isNavItemActive,
  type DashboardNavItem,
} from "@/lib/dashboard-nav";
import { useAuthStore } from "@/store/auth-store";
import { LanguageSwitcher } from "@/components/shared/LanguageSwitcher";

/** Id of the mobile navigation sheet, for the toggle's aria-controls. */
export const MOBILE_SIDEBAR_ID = "mobile-sidebar";

/** localStorage key holding the labelKeys of the sections the user collapsed (#1367). */
export const SIDEBAR_COLLAPSED_KEY = "notaire.sidebar.collapsed";

function readCollapsed(): string[] {
  try {
    const raw = window.localStorage.getItem(SIDEBAR_COLLAPSED_KEY);
    const parsed: unknown = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? parsed.filter((k): k is string => typeof k === "string") : [];
  } catch {
    return [];
  }
}

function writeCollapsed(keys: string[]) {
  try {
    window.localStorage.setItem(SIDEBAR_COLLAPSED_KEY, JSON.stringify(keys));
  } catch {
    // storage unavailable (private mode): the state just isn't remembered
  }
}

interface AppSidebarProps {
  open?: boolean;
  onClose?: () => void;
}

export function AppSidebar({ open = false, onClose }: AppSidebarProps) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, logout, isAdmin } = useAuthStore();
  const t = useTranslations("navigation");
  const tg = useTranslations("navGroups");

  // #1367: collapsible sections. The section holding the current route is
  // opened on load and whenever the route moves into it; the user's other
  // choices are remembered. (The layout renders the sidebar client-side only,
  // after hydration, so reading localStorage in the initializer is safe.)
  const activeGroup = DASHBOARD_NAV_GROUPS.find((g) =>
    g.items.some((i) => isNavItemActive(pathname, i.href)),
  )?.labelKey;
  const [collapsed, setCollapsed] = useState<string[]>(() =>
    readCollapsed().filter((k) => k !== activeGroup),
  );
  const [seenPath, setSeenPath] = useState(pathname);
  if (seenPath !== pathname) {
    setSeenPath(pathname);
    if (activeGroup && collapsed.includes(activeGroup)) {
      setCollapsed(collapsed.filter((k) => k !== activeGroup));
    }
  }

  function toggleGroup(key: string) {
    const next = collapsed.includes(key) ? collapsed.filter((k) => k !== key) : [...collapsed, key];
    setCollapsed(next);
    writeCollapsed(next);
  }

  async function handleLogout() {
    // Clear HttpOnly JWT cookie via API before clearing client state (#1051).
    await apiLogout();
    logout();
    router.replace("/login");
  }

  /** Logo, user, navigation and footer, shared by the desktop sidebar and the mobile sheet. */
  function navLink(item: DashboardNavItem, onNavigate: (() => void) | undefined, animateActive: boolean) {
    const Icon = item.icon;
    const active = isNavItemActive(pathname, item.href);
    const label = t(item.labelKey as Parameters<typeof t>[0]);
    return (
      <Link
        key={item.href}
        href={item.href}
        onClick={onNavigate}
        data-testid={`nav-${item.labelKey}`}
        aria-label={label}
        aria-current={active ? "page" : undefined}
        className={cn(
          "relative flex items-center gap-3 px-4 py-2.5 rounded-[12px] text-sm font-medium transition-colors duration-fast focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-1",
          active
            ? "text-primary-foreground"
            : "text-[hsl(var(--sidebar-foreground))] hover:bg-[hsl(var(--sidebar-hover))] hover:text-[hsl(var(--sidebar-foreground))]",
        )}
      >
        {active && (
          <motion.span
            layoutId={animateActive ? "sidebar-active" : undefined}
            className="absolute inset-0 rounded-[12px] bg-primary shadow-md shadow-primary/20"
            transition={{
              type: "spring",
              stiffness: 380,
              damping: 32,
            }}
          />
        )}
        <span className="relative z-10 flex items-center gap-3">
          <Icon
            className={cn(
              "h-[18px] w-[18px] shrink-0",
              active ? "text-primary-foreground" : "text-[hsl(var(--sidebar-muted))]",
            )}
          />
          {label}
        </span>
      </Link>
    );
  }

  const body = (onNavigate: (() => void) | undefined, animateActive: boolean, idPrefix: string) => (
    <>
      {/* Logo */}
      <div className="flex items-center gap-3 px-7 py-8">
        <div className="flex items-center justify-center w-10 h-10 rounded-[12px] bg-primary text-primary-foreground shadow-sm">
          <Scale className="h-6 w-6" />
        </div>
        <div>
          <p className="font-semibold text-lg leading-tight text-[hsl(var(--sidebar-foreground))]">
            {t("brand")}
          </p>
          <p className="text-[11px] text-[hsl(var(--sidebar-muted))] font-bold uppercase tracking-wider">
            {t("brandSubtitle")}
          </p>
        </div>
      </div>

      {/* User info */}
      <div className="mx-4 mb-6 p-4 rounded-[16px] bg-white/50 border border-white/20 apple-shadow">
        <div className="flex items-center gap-3">
          <div className="flex items-center justify-center w-10 h-10 rounded-full bg-primary text-primary-foreground text-sm font-bold">
            {user?.name?.charAt(0).toUpperCase() ?? "U"}
          </div>
          <div className="min-w-0">
            <p className="text-sm font-semibold text-[hsl(var(--sidebar-foreground))] truncate">
              {user?.name ?? "—"}
            </p>
            <p className="text-[11px] text-[hsl(var(--sidebar-muted))] font-medium capitalize">
              {user?.type?.toLowerCase() ?? ""}
            </p>
          </div>
        </div>
      </div>

      {/* Navigation: Inicio, then the task-based sections (#1367) */}
      <nav aria-label={t("mainNav")} className="flex-1 px-4 py-2 space-y-1 overflow-y-auto">
        {navLink(DASHBOARD_NAV_HOME, onNavigate, animateActive)}
        {DASHBOARD_NAV_GROUPS.map((group) => {
          const items = group.items.filter((item) => !item.adminOnly || isAdmin());
          if (items.length === 0) return null;
          const expanded = !collapsed.includes(group.labelKey);
          const headingId = `${idPrefix}-nav-group-${group.labelKey}`;
          const listId = `${headingId}-items`;
          const label = tg(group.labelKey as Parameters<typeof tg>[0]);
          return (
            <div key={group.labelKey} data-nav-group={group.labelKey} role="group" aria-labelledby={headingId} className="pt-3">
              <h2 id={headingId} className="m-0">
                <button
                  type="button"
                  data-testid={`nav-group-${group.labelKey}`}
                  aria-expanded={expanded}
                  aria-controls={listId}
                  onClick={() => toggleGroup(group.labelKey)}
                  className="flex w-full min-h-8 items-center justify-between rounded-[8px] px-4 py-1.5 text-[11px] font-bold uppercase tracking-wider text-[hsl(var(--sidebar-muted))] hover:text-[hsl(var(--sidebar-foreground))] transition-colors duration-fast focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-1"
                >
                  {label}
                  <ChevronDown
                    aria-hidden="true"
                    className={cn("h-3.5 w-3.5 shrink-0 transition-transform duration-fast", !expanded && "-rotate-90")}
                  />
                </button>
              </h2>
              {expanded && (
                <div id={listId} className="mt-1 space-y-1">
                  {items.map((item) => navLink(item, onNavigate, animateActive))}
                </div>
              )}
            </div>
          );
        })}
      </nav>

      {/* Language switcher + Logout */}
      <div className="px-4 py-6 border-t border-[hsl(var(--sidebar-border))] space-y-3">
        <LanguageSwitcher />
        <button
          data-testid="btn-logout"
          onClick={handleLogout}
          className="flex items-center gap-3 w-full px-4 py-2.5 rounded-[12px] text-sm font-medium text-[hsl(var(--sidebar-foreground))] hover:bg-destructive/10 hover:text-destructive transition-colors duration-fast focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-destructive focus-visible:ring-offset-1"
        >
          <LogOut className="h-[18px] w-[18px] shrink-0" aria-hidden="true" />
          {t("logout")}
        </button>
      </div>
    </>
  );

  const panel =
    "flex-col w-72 min-h-screen bg-[hsl(var(--sidebar))] backdrop-blur-xl border-r border-[hsl(var(--sidebar-border))]";

  return (
    <>
      {/* Desktop: a static column. While the mobile sheet is open it carries the test id. */}
      <aside data-testid={open ? undefined : "sidebar"} className={cn("hidden md:static md:flex", panel)}>
        {body(undefined, true, "desktop")}
      </aside>

      {/* Below 768px: a modal navigation sheet (WAI-ARIA dialog, #1350). Radix traps
          focus, closes on Escape or an outside click, locks the page scroll and
          returns focus to the toggle. */}
      <DialogPrimitive.Root open={open} onOpenChange={(next) => { if (!next) onClose?.(); }}>
        <DialogPrimitive.Portal>
          <DialogPrimitive.Overlay
            data-testid="sidebar-backdrop"
            className="fixed inset-y-0 left-72 right-0 z-30 bg-black/40 md:hidden"
          />
          <DialogPrimitive.Content
            id={MOBILE_SIDEBAR_ID}
            data-testid="sidebar"
            aria-describedby={undefined}
            onOpenAutoFocus={(e) => {
              // Start on the first navigation link, not on the close button.
              e.preventDefault();
              (e.currentTarget as HTMLElement).querySelector<HTMLElement>("nav a")?.focus();
            }}
            onCloseAutoFocus={(e) => {
              // There is no Radix Dialog.Trigger (the toggle lives in the layout header),
              // so Radix has nothing to return focus to: send it back to the toggle.
              e.preventDefault();
              document.querySelector<HTMLElement>(`[aria-controls="${MOBILE_SIDEBAR_ID}"]`)?.focus();
            }}
            className={cn("fixed inset-y-0 left-0 z-40 flex overflow-y-auto md:hidden", panel)}
          >
            <DialogPrimitive.Title className="sr-only">{t("mainNav")}</DialogPrimitive.Title>
            <DialogPrimitive.Close
              aria-label={t("closeMenu")}
              data-testid="btn-sidebar-close"
              className="absolute right-3 top-3 flex h-9 w-9 items-center justify-center rounded-[10px] text-[hsl(var(--sidebar-foreground))] hover:bg-[hsl(var(--sidebar-hover))] transition-colors duration-fast focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-1"
            >
              <X className="h-5 w-5" aria-hidden="true" />
            </DialogPrimitive.Close>
            {body(onClose, false, "mobile")}
          </DialogPrimitive.Content>
        </DialogPrimitive.Portal>
      </DialogPrimitive.Root>
    </>
  );
}
