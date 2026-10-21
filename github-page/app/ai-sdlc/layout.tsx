import type { Metadata } from "next";
import type { ReactNode } from "react";
import { FactoryChrome } from "@/components/FactoryChrome";

export const metadata: Metadata = {
  title: "AI Factory — the AI SDLC that builds Notaire",
  description:
    "Constitution, OpenSpec, agents, skills, guardrails and quality gates: how an AI-assisted SDLC builds production-ready software.",
};

export default function FactoryLayout({ children }: { children: ReactNode }) {
  return <FactoryChrome>{children}</FactoryChrome>;
}
