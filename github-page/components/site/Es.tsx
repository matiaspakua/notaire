"use client";

import { cloneElement, isValidElement, type ReactNode } from "react";
import { ES } from "@/lib/i18n/es";
import "@/lib/i18n/strings";
import { usePreferences } from "./Preferences";

/** Props whose value is an identifier, not a sentence. */
const SKIP = new Set([
  "className",
  "href",
  "id",
  "htmlFor",
  "style",
  "key",
  "chart",
  "src",
  "d",
  "viewBox",
  "fill",
  "stroke",
  "type",
  "role",
  "xmlns",
  "target",
  "rel",
  "as",
  "lang",
  "dir",
]);

function norm(value: string): string {
  return value.replace(/\s+/g, " ").trim();
}

/** Spanish for an exact English sentence. Whitespace around the node is kept. */
export function trs(value: string): string {
  const key = norm(value);
  const es = key ? ES[key] : undefined;
  if (!es) return value;
  const lead = value.match(/^\s*/)?.[0] ?? "";
  const tail = value.match(/\s*$/)?.[0] ?? "";
  return `${lead}${es}${tail}`;
}

/** Walks the rendered tree and replaces English sentences that have a Spanish entry. */
export function translateNode(node: ReactNode): ReactNode {
  if (typeof node === "string") return trs(node);
  if (typeof node === "number" || node == null || typeof node === "boolean") return node;
  if (Array.isArray(node)) {
    let changed = false;
    const out = node.map((item) => {
      const next = translateNode(item);
      if (next !== item) changed = true;
      return next;
    });
    return changed ? out : node;
  }
  if (!isValidElement(node)) return node;
  const props = node.props as Record<string, unknown>;
  const next: Record<string, unknown> = {};
  let changed = false;
  for (const [key, value] of Object.entries(props)) {
    if (key === "children" || isValidElement(value) || Array.isArray(value)) {
      const child = translateNode(value as ReactNode);
      if (child !== value) {
        next[key] = child;
        changed = true;
      }
    } else if (typeof value === "string" && !SKIP.has(key)) {
      const text = trs(value);
      if (text !== value) {
        next[key] = text;
        changed = true;
      }
    }
  }
  return changed ? cloneElement(node, next) : node;
}

/** Applies the Spanish dictionary to its children when the reader chose Spanish. */
export function Es({ children }: { children: ReactNode }) {
  const { locale } = usePreferences();
  if (locale !== "es") return children;
  return <>{translateNode(children)}</>;
}
