"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import {
  FileText,
  Users,
  ClipboardList,
  Calculator,
  BookOpen,
  CreditCard,
  Settings,
  LogOut,
  Scale,
  Home,
  Building2,
  Copy,
  ListTodo,
  FileCheck,
  Shield,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { useAuthStore } from "@/store/auth-store";

interface NavItem {
  label: string;
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  adminOnly?: boolean;
}

const navItems: NavItem[] = [
  { label: "Inicio", href: "/dashboard", icon: Home },
  { label: "Gestiones", href: "/dashboard/gestiones", icon: ClipboardList },
  { label: "Presupuestos", href: "/dashboard/presupuestos", icon: Calculator },
  { label: "Personas", href: "/dashboard/personas", icon: Users },
  { label: "Escrituras", href: "/dashboard/escrituras", icon: FileText },
  { label: "Pagos", href: "/dashboard/pagos", icon: CreditCard },
  { label: "Protocolo", href: "/dashboard/protocolo", icon: BookOpen },
  { label: "Inmuebles", href: "/dashboard/inmuebles", icon: Building2 },
  { label: "Copias", href: "/dashboard/copias", icon: Copy },
  { label: "Items", href: "/dashboard/items", icon: ListTodo },
  { label: "Documentos", href: "/dashboard/documentos", icon: FileCheck },
  { label: "Auditoría", href: "/dashboard/auditoria", icon: Shield },
  {
    label: "Administración",
    href: "/dashboard/administracion",
    icon: Settings,
    adminOnly: true,
  },
];

export function AppSidebar() {
  const pathname = usePathname();
  const router = useRouter();
  const { user, logout, isAdmin } = useAuthStore();

  function handleLogout() {
    logout();
    router.push("/login");
  }

  return (
    <aside className="flex flex-col w-64 min-h-screen bg-sidebar border-r border-sidebar-border">
      {/* Logo */}
      <div className="flex items-center gap-3 px-5 py-5 border-b border-sidebar-border">
        <div className="flex items-center justify-center w-9 h-9 rounded-xl bg-primary/10">
          <Scale className="h-5 w-5 text-primary" />
        </div>
        <div>
          <p className="font-semibold text-base leading-tight text-sidebar-foreground">Notaire</p>
          <p className="text-[11px] text-sidebar-muted font-medium">Sistema de Escribanía</p>
        </div>
      </div>

      {/* User info */}
      <div className="px-5 py-4 border-b border-sidebar-border">
        <div className="flex items-center gap-3">
          <div className="flex items-center justify-center w-8 h-8 rounded-full bg-primary/10 text-primary text-xs font-semibold">
            {user?.nombre?.charAt(0).toUpperCase() ?? "U"}
          </div>
          <div className="min-w-0">
            <p className="text-sm font-medium text-sidebar-foreground truncate">{user?.nombre ?? "—"}</p>
            <p className="text-[11px] text-sidebar-muted capitalize">{user?.tipo?.toLowerCase() ?? ""}</p>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 py-3 space-y-0.5 overflow-y-auto">
        {navItems
          .filter((item) => !item.adminOnly || isAdmin())
          .map((item) => {
            const Icon = item.icon;
            const active = pathname === item.href || (item.href !== "/dashboard" && pathname.startsWith(item.href));
            return (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  "flex items-center gap-3 px-3 py-2 rounded-xl text-sm font-medium transition-all duration-200",
                  active
                    ? "bg-primary text-primary-foreground shadow-sm"
                    : "text-sidebar-foreground/70 hover:bg-sidebar-hover hover:text-sidebar-foreground"
                )}
              >
                <Icon className="h-4 w-4 shrink-0" />
                {item.label}
              </Link>
            );
          })}
      </nav>

      {/* Logout */}
      <div className="px-3 py-4 border-t border-sidebar-border">
        <button
          onClick={handleLogout}
          className="flex items-center gap-3 w-full px-3 py-2 rounded-xl text-sm font-medium text-sidebar-foreground/70 hover:bg-sidebar-hover hover:text-sidebar-foreground transition-all duration-200"
        >
          <LogOut className="h-4 w-4" />
          Cerrar sesión
        </button>
      </div>
    </aside>
  );
}
