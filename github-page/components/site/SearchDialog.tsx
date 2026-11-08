"use client";

import Link from "next/link";
import { useCallback, useId, useMemo, useRef, useState } from "react";
import { search, SEARCH_INDEX, type SearchEntry } from "@/lib/site";
import { useFocusTrap } from "./useFocusTrap";
import { SearchIcon } from "./icons";

const SUGGESTED = ["/docs/", "/ai-sdlc/", "/ai-sdlc/lifecycle/", "/docs/architecture/", "/docs/releases/"];

/**
 * Static site search (#1970): the site map is the index, so it works on GitHub Pages
 * with no server. Combobox + listbox pattern; arrows move, Enter opens, Escape closes.
 */
export function SearchDialog({ onClose, returnTo }: { onClose: () => void; returnTo: React.RefObject<HTMLElement | null> }) {
  const dialog = useRef<HTMLDivElement>(null);
  const input = useRef<HTMLInputElement>(null);
  const listId = useId();
  const [query, setQuery] = useState("");
  const [active, setActive] = useState(0);

  const results: SearchEntry[] = useMemo(
    () => (query.trim() ? search(query) : SUGGESTED.map((h) => SEARCH_INDEX.find((e) => e.href === h)!).filter(Boolean)),
    [query],
  );
  const close = useCallback(() => onClose(), [onClose]);
  useFocusTrap(dialog, true, close, { returnTo, initial: input });

  const optionId = (i: number) => `${listId}-opt-${i}`;

  function onKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === "ArrowDown" || e.key === "ArrowUp") {
      e.preventDefault();
      if (results.length === 0) return;
      const step = e.key === "ArrowDown" ? 1 : -1;
      setActive((a) => (a + step + results.length) % results.length);
    } else if (e.key === "Enter" && results[active]) {
      e.preventDefault();
      document.getElementById(optionId(active))?.querySelector("a")?.click();
    }
  }

  return (
    <div className="fixed inset-0 z-[70] flex items-start justify-center px-4 pt-[12vh]">
      <div className="absolute inset-0 bg-slate-950/40 backdrop-blur-sm" onClick={close} aria-hidden="true" />
      <div
        ref={dialog}
        role="dialog"
        aria-modal="true"
        aria-label="Search the site"
        className="relative w-full max-w-xl overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-2xl"
      >
        <div className="flex items-center gap-3 border-b border-slate-200 px-4">
          <SearchIcon className="h-5 w-5 shrink-0 text-slate-500" />
          <input
            ref={input}
            type="text"
            role="combobox"
            aria-expanded={results.length > 0}
            aria-controls={listId}
            aria-autocomplete="list"
            aria-activedescendant={results.length > 0 ? optionId(active) : undefined}
            aria-label="Search pages"
            placeholder="Search the docs and the AI Factory…"
            className="h-14 w-full bg-transparent text-base text-slate-900 placeholder:text-slate-500 focus:outline-none"
            value={query}
            onChange={(e) => {
              setQuery(e.target.value);
              setActive(0);
            }}
            onKeyDown={onKeyDown}
          />
          <kbd className="hidden rounded border border-slate-300 px-1.5 py-0.5 font-mono text-[11px] text-slate-600 sm:block">Esc</kbd>
        </div>
        {query.trim() === "" && <p className="px-4 pt-3 text-xs font-semibold uppercase tracking-wider text-slate-500">Start here</p>}
        <ul id={listId} role="listbox" aria-label="Results" className="max-h-[50vh] overflow-y-auto p-2">
          {results.map((r, i) => (
            <li
              key={r.href}
              id={optionId(i)}
              role="option"
              aria-selected={i === active}
              onMouseEnter={() => setActive(i)}
              className={`rounded-xl ${i === active ? "bg-slate-100" : ""}`}
            >
              <Link href={r.href} tabIndex={-1} onClick={close} className="block px-3 py-2.5">
                <span className="flex items-baseline justify-between gap-3">
                  <span className="font-medium text-slate-900">{r.title}</span>
                  <span className="shrink-0 text-[11px] font-medium uppercase tracking-wider text-slate-500">{r.section}</span>
                </span>
                <span className="mt-0.5 block text-sm text-slate-600">{r.summary}</span>
              </Link>
            </li>
          ))}
        </ul>
        {results.length === 0 && (
          <p className="px-5 pb-6 pt-2 text-sm text-slate-600" role="status">
            No page matches “{query}”. Try “architecture”, “guardrails” or “releases”.
          </p>
        )}
      </div>
    </div>
  );
}
