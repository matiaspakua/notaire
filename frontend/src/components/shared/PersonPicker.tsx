"use client";

/**
 * Searchable person picker (#1340 slice 3): an ARIA 1.2 combobox with a listbox
 * popup. An empty query lists the newest people; typing searches the server
 * (debounced) instead of filtering a size=1000 list. The current value is a
 * person id, loaded by id so edit forms show it wherever it sits in the table.
 */
import { useEffect, useId, useRef, useState } from "react";
import { useTranslations } from "next-intl";
import { Search, X } from "lucide-react";
import { cn, fullName } from "@/lib/utils";
import { useDebouncedValue } from "@/hooks/useDebouncedValue";
import { usePersona, usePersonSearch, useRecentPersonas } from "@/hooks/usePersonSearch";
import type { Persona } from "@/types";

const MAX_OPTIONS = 50;

export interface PersonPickerProps {
  /** Selected person id. */
  value?: number;
  /** The selected person when the form already has it (skips the by-id load). */
  selected?: Persona;
  onChange: (person: Persona | undefined) => void;
  "aria-label": string;
  placeholder?: string;
  /** Only list people marked as clients. */
  clientsOnly?: boolean;
  /** Show a button that clears the selection. */
  allowClear?: boolean;
  disabled?: boolean;
  className?: string;
  "data-testid"?: string;
}

export function PersonPicker({
  value,
  selected,
  onChange,
  "aria-label": ariaLabel,
  placeholder,
  clientsOnly = false,
  allowClear = false,
  disabled = false,
  className,
  "data-testid": testId,
}: PersonPickerProps) {
  const t = useTranslations("personPicker");
  const listId = useId();
  const inputRef = useRef<HTMLInputElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [active, setActive] = useState(-1);
  const [known, setKnown] = useState<Persona | undefined>(selected);

  const debounced = useDebouncedValue(query, 250);
  const searching = debounced.trim().length > 0;
  const search = usePersonSearch(debounced, open);
  const recent = useRecentPersonas(open && !searching);

  const knownMatches = (selected?.personId === value ? selected : undefined) ?? (known?.personId === value ? known : undefined);
  const loaded = usePersona(value, value != null && !knownMatches);
  const current = value == null ? undefined : knownMatches ?? loaded.data;

  const source = searching ? search.data : recent.data?.content;
  const options = (source ?? []).filter((p) => !clientsOnly || p.isClient).slice(0, MAX_OPTIONS);
  const pending = query !== debounced || (searching ? search.isFetching : recent.isLoading);
  const failed = searching ? search.isError : recent.isError;

  function close() {
    setOpen(false);
    setQuery("");
    setActive(-1);
  }

  function openList() {
    if (disabled) return;
    setOpen(true);
    setActive(-1);
  }

  function choose(person: Persona) {
    setKnown(person);
    onChange(person);
    close();
  }

  // Escape closes the popup, not the dialog around it: Radix listens on the
  // document in the capture phase, so intercept on window, which runs first.
  useEffect(() => {
    if (!open) return;
    function onKey(e: KeyboardEvent) {
      if (e.key === "Escape" && e.target === inputRef.current) {
        e.preventDefault();
        e.stopPropagation();
        close();
      }
    }
    window.addEventListener("keydown", onKey, true);
    return () => window.removeEventListener("keydown", onKey, true);
  }, [open]);

  const optionId = (p: Persona) => `${listId}-opt-${p.personId}`;
  const activeOption = open && active >= 0 ? options[active] : undefined;

  useEffect(() => {
    if (activeOption) document.getElementById(optionId(activeOption))?.scrollIntoView?.({ block: "nearest" });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeOption?.personId]);

  function onKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    switch (e.key) {
      case "ArrowDown":
        e.preventDefault();
        if (!open) openList();
        else setActive((a) => Math.min(options.length - 1, a + 1));
        break;
      case "ArrowUp":
        e.preventDefault();
        if (open) setActive((a) => Math.max(0, a - 1));
        break;
      case "Home":
        if (open && options.length) { e.preventDefault(); setActive(0); }
        break;
      case "End":
        if (open && options.length) { e.preventDefault(); setActive(options.length - 1); }
        break;
      case "Enter":
        if (activeOption) { e.preventDefault(); choose(activeOption); }
        break;
      case "Tab":
        if (open) close();
        break;
    }
  }

  const status = failed
    ? t("loadError")
    : pending
      ? t("searching")
      : options.length === 0
        ? t("noResults")
        : searching
          ? t("results", { count: options.length })
          : t("recentHint");

  return (
    <div ref={containerRef} className={cn("relative w-full", className)}>
      <Search aria-hidden="true" className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-[hsl(var(--muted-foreground))]" />
      <input
        ref={inputRef}
        type="text"
        role="combobox"
        aria-label={ariaLabel}
        aria-expanded={open}
        aria-controls={listId}
        aria-autocomplete="list"
        aria-activedescendant={activeOption ? optionId(activeOption) : undefined}
        autoComplete="off"
        spellCheck={false}
        disabled={disabled}
        data-testid={testId}
        placeholder={placeholder ?? t("placeholder")}
        value={open ? query : current ? fullName(current) : ""}
        onFocus={openList}
        onClick={() => { if (!open) openList(); }}
        onChange={(e) => { setQuery(e.target.value); setOpen(true); setActive(-1); }}
        onKeyDown={onKeyDown}
        onBlur={(e) => { if (!containerRef.current?.contains(e.relatedTarget as Node | null)) close(); }}
        className={cn(
          "flex h-12 w-full rounded-[12px] border border-[hsl(var(--border))] bg-white pl-10 py-2.5 text-base transition-all duration-200 placeholder:text-[hsl(var(--muted-foreground))] apple-focus disabled:cursor-not-allowed disabled:opacity-50 hover:border-[hsl(var(--ring)/0.4)]",
          allowClear && value != null ? "pr-11" : "pr-4",
        )}
      />
      {allowClear && value != null && !disabled && (
        <button
          type="button"
          aria-label={t("clear")}
          onMouseDown={(e) => e.preventDefault()}
          onClick={() => { setKnown(undefined); onChange(undefined); close(); }}
          className="absolute right-2 top-1/2 -translate-y-1/2 rounded-full p-2 text-[hsl(var(--muted-foreground))] hover:bg-black/5 apple-focus"
        >
          <X aria-hidden="true" className="h-4 w-4" />
        </button>
      )}
      {open && (
        <div
          className="absolute left-0 right-0 top-full z-50 mt-1 overflow-hidden rounded-[12px] border border-[hsl(var(--border))] bg-white shadow-lg animate-in fade-in slide-in-from-top-1 duration-150 motion-reduce:animate-none"
        >
          <ul id={listId} role="listbox" aria-label={ariaLabel} className="max-h-64 overflow-y-auto p-1">
            {options.map((p, i) => (
              <li
                key={p.personId}
                id={optionId(p)}
                role="option"
                aria-selected={i === active}
                aria-describedby={p.identificationNumber ? `${optionId(p)}-doc` : undefined}
                data-person-id={p.personId}
                onMouseDown={(e) => e.preventDefault()}
                onMouseEnter={() => setActive(i)}
                onClick={() => choose(p)}
                className={cn(
                  "flex cursor-pointer select-none items-center justify-between gap-3 rounded-[8px] px-3 py-2 text-sm transition-colors duration-150",
                  i === active ? "bg-accent text-accent-foreground" : "hover:bg-secondary/40",
                  p.personId === value && "font-semibold",
                )}
              >
                <span className="truncate">{fullName(p)}</span>
                {p.identificationNumber && (
                  <span id={`${optionId(p)}-doc`} aria-hidden="true" className="shrink-0 text-xs text-[hsl(var(--muted-foreground))]">
                    {p.identificationNumber}
                  </span>
                )}
              </li>
            ))}
          </ul>
          <div role="status" aria-live="polite" className="border-t border-[hsl(var(--border))] px-3 py-2 text-xs text-[hsl(var(--muted-foreground))]">
            {status}
          </div>
        </div>
      )}
    </div>
  );
}
