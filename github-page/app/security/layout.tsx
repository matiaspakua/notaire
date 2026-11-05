import type { ReactNode } from "react";
import { DocShell } from "@/components/site/DocShell";

export default function SecurityLayout({ children }: { children: ReactNode }) {
  return <DocShell>{children}</DocShell>;
}
