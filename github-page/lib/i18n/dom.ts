/**
 * Swaps rendered English for Spanish in a subtree (#2202), and puts it back. Server components render
 * English; this runs in the browser. #2475: it also covers accessibility attributes, and an observer
 * keeps translating what renders later (Mermaid diagrams, interactive figures, re-rendered tables).
 */
import type { Locale } from "../preferences";
import { ES } from "./es";

const ATTRIBUTES = ["aria-label", "title", "alt", "placeholder"] as const;
const TABLE = "Table: ";
const ORIGINAL = "data-i18n-en-";

type Tracked = Text & { __en?: string };

function norm(value: string): string {
  return value.replace(/\s+/g, " ").trim();
}

/** Spanish for an exact English sentence, keeping the whitespace around it. */
export function trs(value: string): string {
  const key = norm(value);
  const es = key ? ES[key] : undefined;
  if (!es) return value;
  const lead = value.match(/^\s*/)?.[0] ?? "";
  const tail = value.match(/\s*$/)?.[0] ?? "";
  return `${lead}${es}${tail}`;
}

function translateAttribute(value: string): string {
  if (!value.startsWith(TABLE)) return trs(value);
  return `Tabla: ${value.slice(TABLE.length).split(", ").map(trs).join(", ")}`;
}

function localizeText(root: Element, locale: Locale): void {
  const walker = root.ownerDocument.createTreeWalker(root, NodeFilter.SHOW_TEXT);
  for (let node = walker.nextNode() as Tracked | null; node; node = walker.nextNode() as Tracked | null) {
    const parent = node.parentElement;
    if (!parent || parent.closest("script, style, pre")) continue;
    if (locale === "es") {
      const source = node.__en ?? node.nodeValue ?? "";
      const next = trs(source);
      if (next !== source && node.nodeValue !== next) {
        node.__en = source;
        node.nodeValue = next;
      }
    } else if (node.__en !== undefined) {
      node.nodeValue = node.__en;
      delete node.__en;
    }
  }
}

function localizeAttributes(root: Element, locale: Locale): void {
  const selector = ATTRIBUTES.map((name) => `[${name}]`).join(",");
  const elements = [root, ...Array.from(root.querySelectorAll(selector))];
  for (const element of elements) {
    for (const name of ATTRIBUTES) {
      const stored = element.getAttribute(ORIGINAL + name);
      const current = element.getAttribute(name);
      if (locale === "es") {
        const source = stored ?? current;
        if (!source) continue;
        const next = translateAttribute(source);
        if (next !== source && current !== next) {
          element.setAttribute(ORIGINAL + name, source);
          element.setAttribute(name, next);
        }
      } else if (stored !== null) {
        element.setAttribute(name, stored);
        element.removeAttribute(ORIGINAL + name);
      }
    }
  }
}

/** One pass over the subtree: text nodes and accessibility attributes. */
export function localizeTree(root: Element, locale: Locale): void {
  localizeText(root, locale);
  localizeAttributes(root, locale);
}

/** Localises now and again whenever the subtree changes; returns the function that stops observing. */
export function observeLocalized(root: Element, locale: Locale): () => void {
  localizeTree(root, locale);
  if (locale !== "es" || typeof MutationObserver === "undefined") return () => {};
  let queued = false;
  const observer = new MutationObserver(() => {
    if (queued) return;
    queued = true;
    queueMicrotask(() => {
      queued = false;
      observer.disconnect();
      localizeTree(root, locale);
      observer.observe(root, OPTIONS);
    });
  });
  observer.observe(root, OPTIONS);
  return () => observer.disconnect();
}

const OPTIONS: MutationObserverInit = { childList: true, subtree: true, characterData: true };
