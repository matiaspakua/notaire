"use client";

import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import { Menu } from "lucide-react";
import { useTranslations } from "next-intl";
import { AppSidebar, MOBILE_SIDEBAR_ID } from "@/components/layout/AppSidebar";
import { Breadcrumb } from "@/components/layout/Breadcrumb";
import { useAuthStore } from "@/store/auth-store";
import { AnimatePresence, PageTransition } from "@/components/motion";
import { theme } from "@/theme/tokens";

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { isAuthenticated } = useAuthStore();
  const router = useRouter();
  const pathname = usePathname();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const tn = useTranslations("navigation");
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

  // The sheet is mobile-only: close it if the viewport grows to the desktop layout.
  useEffect(() => {
    const desktop = window.matchMedia("(min-width: 768px)");
    const onChange = (e: MediaQueryListEvent) => { if (e.matches) setSidebarOpen(false); };
    desktop.addEventListener("change", onChange);
    return () => desktop.removeEventListener("change", onChange);
  }, []);

  if (!mounted || !isAuthenticated) return null;

  return (
    <div
      className="flex min-h-screen"
      style={{ backgroundColor: theme.colors.neutral[100] }}
    >
      <AppSidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="flex-1 flex flex-col min-w-0">
        {/* Each page renders its own AppHeader with a title; a title-less one
            here only produced an empty (a11y-invalid) duplicate <h1>. */}
        <div className="flex items-center gap-3 px-6 lg:px-8 pt-4 pb-0">
          <button
            data-testid="btn-sidebar-toggle"
            onClick={() => setSidebarOpen(true)}
            aria-label={tn("openMenu")}
            aria-expanded={sidebarOpen}
            aria-controls={MOBILE_SIDEBAR_ID}
            className="md:hidden flex items-center justify-center w-9 h-9 rounded-[10px] border border-border/60 text-foreground hover:bg-accent transition-colors duration-fast focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-1"
          >
            <Menu className="h-5 w-5" aria-hidden="true" />
          </button>
          <Breadcrumb />
        </div>
        <main className="flex-1 overflow-y-auto p-6 lg:p-8">
          {/* initial={false}: the first render is not animated (nor are its
              children, e.g. the workflow tracker's pulse). Pages have no exit
              variant and the default "sync" mode is used, so the old page
              unmounts at once and the new one fades in without waiting (#1368). */}
          <AnimatePresence initial={false}>
            <PageTransition key={pathname}>{children}</PageTransition>
          </AnimatePresence>
        </main>
      </div>
    </div>
  );
}
