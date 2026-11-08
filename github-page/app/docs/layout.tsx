import type { ReactNode } from "react";
import { DocShell } from "@/components/site/DocShell";

export default function DocsLayout({ children }: { children: ReactNode }) {
  return <DocShell>{children}</DocShell>;
}
