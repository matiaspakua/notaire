"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { LogOut, Scale, X } from "lucide-react";
import * as DialogPrimitive from "@radix-ui/react-dialog";
import { useTranslations } from "next-intl";
import { motion } from "motion/react";
import { cn } from "@/lib/utils";
import { apiLogout } from "@/lib/api-client";
import { DASHBOARD_NAV_ITEMS } from "@/lib/dashboard-nav";
import { useAuthStore } from "@/store/auth-store";
import { LanguageSwitcher } from "@/components/shared/LanguageSwitcher";

/** Id of the mobile navigation sheet, for the toggle's aria-controls. */
export const MOBILE_SIDEBAR_ID = "mobile-sidebar";

interface AppSidebarProps {
  open?: boolean;
  onClose?: () => void;
}

export function AppSidebar({ open = false, onClose }: AppSidebarProps) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, logout, isAdmin } = useAuthStore();
  const t = useTranslations("navigation");

  async function handleLogout() {
    // Clear HttpOnly JWT cookie via API before clearing client state (#1051).
    await apiLogout();
    logout();
    router.replace("/login");
  }

  /** Logo, user, navigation and footer, shared by the desktop sidebar and the mobile sheet. */
  const body = (onNavigate: (() => void) | undefined, animateActive: boolean) => (
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
          <div className="flex items-center justify-center w-10 h-10 rounded-full bg-gradient-to-br from-primary to-blue-600 text-primary-foreground text-sm font-bold">
            {user?.nombre?.charAt(0).toUpperCase() ?? "U"}
          </div>
          <div className="min-w-0">
            <p className="text-sm font-semibold text-[hsl(var(--sidebar-foreground))] truncate">
              {user?.nombre ?? "—"}
            </p>
            <p className="text-[11px] text-[hsl(var(--sidebar-muted))] font-medium capitalize">
              {user?.tipo?.toLowerCase() ?? ""}
            </p>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav aria-label={t("mainNav")} className="flex-1 px-4 py-2 space-y-1 overflow-y-auto">
        {DASHBOARD_NAV_ITEMS.filter((item) => !item.adminOnly || isAdmin()).map(
          (item) => {
            const Icon = item.icon;
            const active =
              pathname === item.href ||
              (item.href !== "/dashboard" && pathname.startsWith(item.href));
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
                  "relative flex items-center gap-3 px-4 py-2.5 rounded-[12px] text-sm font-medium transition-colors duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-1",
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
                      active
                        ? "text-primary-foreground"
                        : "text-[hsl(var(--sidebar-muted))]",
                    )}
                  />
                  {label}
                </span>
              </Link>
            );
          },
        )}
      </nav>

      {/* Language switcher + Logout */}
      <div className="px-4 py-6 border-t border-[hsl(var(--sidebar-border))] space-y-3">
        <LanguageSwitcher />
        <button
          data-testid="btn-logout"
          onClick={handleLogout}
          className="flex items-center gap-3 w-full px-4 py-2.5 rounded-[12px] text-sm font-medium text-[hsl(var(--sidebar-foreground))] hover:bg-red-50 hover:text-red-600 transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-destructive focus-visible:ring-offset-1"
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
        {body(undefined, true)}
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
              className="absolute right-3 top-3 flex h-9 w-9 items-center justify-center rounded-[10px] text-[hsl(var(--sidebar-foreground))] hover:bg-[hsl(var(--sidebar-hover))] transition-colors duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-1"
            >
              <X className="h-5 w-5" aria-hidden="true" />
            </DialogPrimitive.Close>
            {body(onClose, false)}
          </DialogPrimitive.Content>
        </DialogPrimitive.Portal>
      </DialogPrimitive.Root>
    </>
  );
}
