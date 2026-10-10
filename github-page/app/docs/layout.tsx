import type { ReactNode } from "react";
import { DocsChrome } from "@/components/DocsChrome";

export default function DocsLayout({ children }: { children: ReactNode }) {
  return <DocsChrome>{children}</DocsChrome>;
}
