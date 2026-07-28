"use client";

import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import { Menu } from "lucide-react";
import { AppSidebar } from "@/components/layout/AppSidebar";
import { Breadcrumb } from "@/components/layout/Breadcrumb";
import { useAuthStore } from "@/store/auth-store";
import { AnimatePresence, PageTransition } from "@/components/motion";

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { isAuthenticated } = useAuthStore();
  const router = useRouter();
  const pathname = usePathname();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  // Delay auth check until after client hydration so Zustand can read localStorage.
  // Without this, the layout redirects before persist has loaded the stored auth state.
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- intentional one-time hydration flag
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!mounted) return;
    if (!isAuthenticated) {
      router.replace("/login");
    }
  }, [mounted, isAuthenticated, router]);

  if (!mounted || !isAuthenticated) return null;

  return (
    <div className="flex min-h-screen bg-[#F5F5F7]">
      <AppSidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="flex-1 flex flex-col min-w-0">
        {/* Each page renders its own AppHeader with a title; a title-less one
            here only produced an empty (a11y-invalid) duplicate <h1>. */}
        <div className="flex items-center gap-3 px-6 lg:px-8 pt-4 pb-0">
          <button
            data-testid="btn-sidebar-toggle"
            onClick={() => setSidebarOpen(true)}
            aria-label="Abrir menú"
            className="md:hidden flex items-center justify-center w-9 h-9 rounded-[10px] border border-border/60 text-foreground hover:bg-accent transition-colors duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-1"
          >
            <Menu className="h-5 w-5" aria-hidden="true" />
          </button>
          <Breadcrumb />
        </div>
        <main className="flex-1 overflow-y-auto p-6 lg:p-8">
          <AnimatePresence mode="wait" initial={false}>
            <PageTransition key={pathname}>{children}</PageTransition>
          </AnimatePresence>
        </main>
      </div>
    </div>
  );
}
