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
  UserCheck,
  BarChart2,
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
  { label: "Gestiones", href: "/dashboard/gestiones", icon: ClipboardList },
  { label: "Presupuestos", href: "/dashboard/presupuestos", icon: Calculator },
  { label: "Personas", href: "/dashboard/personas", icon: Users },
  { label: "Escrituras", href: "/dashboard/escrituras", icon: FileText },
  { label: "Pagos", href: "/dashboard/pagos", icon: CreditCard },
  { label: "Protocolo", href: "/dashboard/protocolo", icon: BookOpen },
  { label: "Suplencias", href: "/dashboard/suplencias", icon: UserCheck },
  { label: "Reportes", href: "/dashboard/reportes", icon: BarChart2 },
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
    <aside className="flex flex-col w-64 min-h-screen bg-slate-900 text-white">
      {/* Logo */}
      <div className="flex items-center gap-3 px-6 py-5 border-b border-slate-700">
        <Scale className="h-7 w-7 text-amber-400" />
        <div>
          <p className="font-bold text-lg leading-tight">Notaire</p>
          <p className="text-xs text-slate-400">Sistema de Escribanía</p>
        </div>
      </div>

      {/* User info */}
      <div className="px-6 py-4 border-b border-slate-700">
        <p className="text-sm font-medium truncate">{user?.nombre ?? "—"}</p>
        <p className="text-xs text-slate-400 capitalize">{user?.tipo?.toLowerCase() ?? ""}</p>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        {navItems
          .filter((item) => !item.adminOnly || isAdmin())
          .map((item) => {
            const Icon = item.icon;
            const active = pathname.startsWith(item.href);
            return (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors",
                  active
                    ? "bg-amber-400 text-slate-900"
                    : "text-slate-300 hover:bg-slate-800 hover:text-white"
                )}
              >
                <Icon className="h-4 w-4 shrink-0" />
                {item.label}
              </Link>
            );
          })}
      </nav>

      {/* Logout */}
      <div className="px-3 py-4 border-t border-slate-700">
        <button
          onClick={handleLogout}
          className="flex items-center gap-3 w-full px-3 py-2.5 rounded-lg text-sm font-medium text-slate-300 hover:bg-slate-800 hover:text-white transition-colors"
        >
          <LogOut className="h-4 w-4" />
          Cerrar sesión
        </button>
      </div>
    </aside>
  );
}
