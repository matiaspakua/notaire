/**
 * Spanish for the public site (#2202). The English sentence is the key.
 * A missing key is a test failure for every string the translated routes render.
 */
import type { Locale } from "../preferences";

export const ES: Record<string, string> = {};

export function pair(en: string, es: string): string {
  if (!es.trim()) throw new Error(`empty Spanish for: ${en}`);
  ES[en] = es;
  return en;
}

export function tr(locale: Locale, value: string): string {
  if (locale !== "es") return value;
  return ES[value] ?? value;
}

/** Routes whose visible copy must have an ES entry. Deeper /docs/ pages stay English on purpose. */
export const TRANSLATED_ROUTES = [
  "/",
  "/about/",
  "/product/",
  "/product/anatomy/",
  "/product/story/",
  "/product/platform/",
  "/product/numbers/",
  "/security/",
  "/security/architecture/",
  "/security/threats/",
  "/security/toolchain/",
  "/security/posture/",
  "/ai-sdlc/",
  "/ai-sdlc/case-study/",
  "/ai-sdlc/anatomy/",
  "/ai-sdlc/lifecycle/",
  "/ai-sdlc/openspec/",
  "/ai-sdlc/agents/",
  "/ai-sdlc/guardrails/",
  "/ai-sdlc/quality-gates/",
  "/ai-sdlc/security-gates/",
  "/ai-sdlc/governance/",
  "/ai-sdlc/lessons/",
  "/ai-sdlc/results/",
  "/ai-sdlc/framework/",
  "/ai-sdlc/playbook/",
  "/docs/",
  "/docs/artifacts/",
  "/docs/architecture/",
  "/docs/engineering/",
  "/docs/testing/",
  "/docs/releases/",
] as const;
