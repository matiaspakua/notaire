"use client";

import { cloneElement, isValidElement, type ReactNode } from "react";
import { trs } from "@/lib/i18n/dom";
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

export { trs };

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
