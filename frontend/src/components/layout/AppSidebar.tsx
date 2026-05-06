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
    <aside className="flex flex-col w-72 min-h-screen bg-[#F5F5F7]/80 backdrop-blur-xl border-r border-border/40">
      {/* Logo */}
      <div className="flex items-center gap-3 px-7 py-8">
        <div className="flex items-center justify-center w-10 h-10 rounded-[12px] bg-[#0071e3] text-white shadow-sm">
          <Scale className="h-6 w-6" />
        </div>
        <div>
          <p className="font-semibold text-lg leading-tight text-[#1d1d1f]">Notaire</p>
          <p className="text-[11px] text-[#86868b] font-bold uppercase tracking-wider">Escribanía</p>
        </div>
      </div>

      {/* User info */}
      <div className="mx-4 mb-6 p-4 rounded-2xl bg-white/50 border border-white/20 apple-shadow-sm">
        <div className="flex items-center gap-3">
          <div className="flex items-center justify-center w-10 h-10 rounded-full bg-gradient-to-br from-primary to-blue-600 text-white text-sm font-bold">
            {user?.nombre?.charAt(0).toUpperCase() ?? "U"}
          </div>
          <div className="min-w-0">
            <p className="text-sm font-semibold text-[#1d1d1f] truncate">{user?.nombre ?? "—"}</p>
            <p className="text-[11px] text-[#86868b] font-medium capitalize">{user?.tipo?.toLowerCase() ?? ""}</p>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-4 py-2 space-y-1 overflow-y-auto">
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
                  "flex items-center gap-3 px-4 py-2.5 rounded-[12px] text-sm font-medium transition-all duration-300",
                  active
                    ? "bg-[#0071e3] text-white shadow-md shadow-blue-500/20"
                    : "text-[#424245] hover:bg-black/5 hover:text-[#1d1d1f]"
                )}
              >
                <Icon className={cn("h-4 w-4 shrink-0", active ? "text-white" : "text-[#86868b]")} />
                {item.label}
              </Link>
            );
          })}
      </nav>

      {/* Logout */}
      <div className="px-4 py-6 border-t border-border/40">
        <button
          onClick={handleLogout}
          className="flex items-center gap-3 w-full px-4 py-2.5 rounded-[12px] text-sm font-medium text-[#424245] hover:bg-red-50 hover:text-red-600 transition-all duration-300"
        >
          <LogOut className="h-4 w-4" />
          Cerrar sesión
        </button>
      </div>
    </aside>
  );
}
