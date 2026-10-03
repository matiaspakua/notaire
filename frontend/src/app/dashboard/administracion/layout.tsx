"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/auth-store";
import { forbiddenDashboardPath } from "@/lib/admin-access";

/**
 * Client-side admin layout guard (issue #1052). Complements edge proxy role-cookie
 * checks when the edge signal is missing/stale but Zustand has a non-admin user.
 */
export default function AdministracionLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { isAuthenticated, isAdmin } = useAuthStore();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- intentional hydration flag
    setMounted(true);
  }, []);

  const admin = isAdmin();

  useEffect(() => {
    if (!mounted) return;
    if (!isAuthenticated) {
      router.replace("/login");
      return;
    }
    if (!admin) {
      router.replace(forbiddenDashboardPath());
    }
  }, [mounted, isAuthenticated, admin, router]);

  if (!mounted || !isAuthenticated || !admin) return null;

  return children;
}
