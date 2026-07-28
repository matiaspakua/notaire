"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  BookMarked,
  Building2,
  Calculator,
  Copy,
  CreditCard,
  FileText,
  FolderKanban,
  Home,
  ListTodo,
  LogOut,
  Scale,
  ScrollText,
  Settings,
  ShieldCheck,
  Users,
} from "lucide-react";
import { useTranslations } from "next-intl";
import { motion } from "motion/react";
import { cn } from "@/lib/utils";
import { useAuthStore } from "@/store/auth-store";
import { LanguageSwitcher } from "@/components/shared/LanguageSwitcher";

type LucideIcon = React.ComponentType<{ className?: string }>;

interface NavItem {
  labelKey: string;
  href: string;
  icon: LucideIcon;
  adminOnly?: boolean;
}

const navItems: NavItem[] = [
  { labelKey: "home", href: "/dashboard", icon: Home },
  { labelKey: "gestiones", href: "/dashboard/gestiones", icon: FolderKanban },
  {
    labelKey: "presupuestos",
    href: "/dashboard/presupuestos",
    icon: Calculator,
  },
  { labelKey: "personas", href: "/dashboard/personas", icon: Users },
  { labelKey: "escrituras", href: "/dashboard/escrituras", icon: ScrollText },
  { labelKey: "pagos", href: "/dashboard/pagos", icon: CreditCard },
  { labelKey: "protocolo", href: "/dashboard/protocolo", icon: BookMarked },
  { labelKey: "inmuebles", href: "/dashboard/inmuebles", icon: Building2 },
  { labelKey: "copias", href: "/dashboard/copias", icon: Copy },
  { labelKey: "items", href: "/dashboard/items", icon: ListTodo },
  { labelKey: "documentos", href: "/dashboard/documentos", icon: FileText },
  { labelKey: "auditoria", href: "/dashboard/auditoria", icon: ShieldCheck },
  {
    labelKey: "administracion",
    href: "/dashboard/administracion",
    icon: Settings,
    adminOnly: true,
  },
];

interface AppSidebarProps {
  open?: boolean;
  onClose?: () => void;
}

export function AppSidebar({ open = false, onClose }: AppSidebarProps) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, logout, isAdmin } = useAuthStore();
  const t = useTranslations("navigation");

  function handleLogout() {
    logout();
    router.replace("/login");
  }

  return (
    <>
      {open && (
        <div
          data-testid="sidebar-backdrop"
          onClick={onClose}
          className="fixed inset-y-0 left-72 right-0 z-30 bg-black/40 md:hidden"
          aria-hidden="true"
        />
      )}
      <aside
        data-testid="sidebar"
        className={cn(
          "fixed inset-y-0 left-0 z-40 flex-col w-72 min-h-screen bg-[hsl(var(--sidebar))] backdrop-blur-xl border-r border-[hsl(var(--sidebar-border))] md:static md:z-auto md:flex",
          open ? "flex" : "hidden",
        )}
      >
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
        <nav className="flex-1 px-4 py-2 space-y-1 overflow-y-auto">
          {navItems
            .filter((item) => !item.adminOnly || isAdmin())
            .map((item) => {
              const Icon = item.icon;
              const active =
                pathname === item.href ||
                (item.href !== "/dashboard" && pathname.startsWith(item.href));
              const label = t(item.labelKey as Parameters<typeof t>[0]);
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  onClick={onClose}
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
                      layoutId="sidebar-active"
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
            })}
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
      </aside>
    </>
  );
}
