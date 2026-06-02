"use client";

import { useEffect, useState } from "react";
import { useRouter, usePathname } from "next/navigation";
import { AppSidebar } from "@/components/layout/AppSidebar";
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
  // Delay auth check until after client hydration so Zustand can read localStorage.
  // Without this, the layout redirects before persist has loaded the stored auth state.
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
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
      <AppSidebar />
      <div className="flex-1 flex flex-col">
        {/* Each page renders its own AppHeader with a title; a title-less one
            here only produced an empty (a11y-invalid) duplicate <h1>. */}
        <main className="flex-1 overflow-y-auto p-6 lg:p-8">
          <AnimatePresence mode="wait" initial={false}>
            <PageTransition key={pathname}>{children}</PageTransition>
          </AnimatePresence>
        </main>
      </div>
    </div>
  );
}
