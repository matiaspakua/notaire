"use client";

/**
 * Shared server-search combobox for pickers over large tables (#1340): an ARIA
 * 1.2 combobox with a listbox popup. The caller supplies the options for the
 * typed (debounced) query; this component owns the popup, keyboard and
 * announcements. Like the APG combobox, focus alone does not open the popup (a
 * dialog that autofocuses the field stays readable); a click, typing or
 * ArrowDown does. Used by PersonPicker and BudgetPicker.
 */
import { useEffect, useId, useRef, useState, type ReactNode } from "react";
import { Search, X } from "lucide-react";
import { cn } from "@/lib/utils";
import { useDebouncedValue } from "@/hooks/useDebouncedValue";

const MAX_OPTIONS = 50;

export interface ComboboxSource<T> {
  /** Options to list, or undefined while they are not known yet. */
  options: T[] | undefined;
  pending: boolean;
  failed: boolean;
}

export interface SearchComboboxMessages {
  placeholder: string;
  searching: string;
  noResults: string;
  results: (count: number) => string;
  recentHint: string;
  clear: string;
  loadError: string;
}

export interface SearchComboboxProps<T> {
  /** Options for a query: `query` is the settled (debounced) text, "" before typing. */
  useSource: (query: string, open: boolean) => ComboboxSource<T> & { settled?: boolean };
  getKey: (option: T) => number;
  getLabel: (option: T) => string;
  /** Secondary text, visual only (the label is the accessible name). */
  renderDetail?: (option: T) => ReactNode;
  /** Text of the current selection, shown while the popup is closed. */
  displayValue: string;
  selectedKey?: number;
  onSelect: (option: T) => void;
  onClear?: () => void;
  messages: SearchComboboxMessages;
  "aria-label": string;
  placeholder?: string;
  disabled?: boolean;
  className?: string;
  "data-testid"?: string;
}

export function SearchCombobox<T>({
  useSource,
  getKey,
  getLabel,
  renderDetail,
  displayValue,
  selectedKey,
  onSelect,
  onClear,
  messages,
  "aria-label": ariaLabel,
  placeholder,
  disabled = false,
  className,
  "data-testid": testId,
}: SearchComboboxProps<T>) {
  const listId = useId();
  const inputRef = useRef<HTMLInputElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [active, setActive] = useState(-1);

  const debounced = useDebouncedValue(query, 250);
  const searching = query.trim().length > 0;
  const source = useSource(searching ? debounced.trim() : "", open);
  // While typing, list only the results of the text actually typed: never the
  // recent list or an older query, so Enter cannot pick a stale option.
  const settled = !searching || (query === debounced && source.settled !== false && !source.pending);
  const options = settled ? (source.options ?? []).slice(0, MAX_OPTIONS) : [];
  const pending = !settled || source.pending;

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

  function choose(option: T) {
    onSelect(option);
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

  const optionId = (o: T) => `${listId}-opt-${getKey(o)}`;
  const activeOption = open && active >= 0 ? options[active] : undefined;
  const activeKey = activeOption ? getKey(activeOption) : undefined;

  useEffect(() => {
    if (activeOption) document.getElementById(optionId(activeOption))?.scrollIntoView?.({ block: "nearest" });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeKey]);

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

  const status = source.failed
    ? messages.loadError
    : pending
      ? messages.searching
      : options.length === 0
        ? messages.noResults
        : searching
          ? messages.results(options.length)
          : messages.recentHint;

  const canClear = onClear && selectedKey != null && !disabled;

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
        placeholder={placeholder ?? messages.placeholder}
        value={open ? query : displayValue}
        onClick={() => { if (!open) openList(); }}
        onChange={(e) => { setQuery(e.target.value); setOpen(true); setActive(-1); }}
        onKeyDown={onKeyDown}
        onBlur={(e) => { if (!containerRef.current?.contains(e.relatedTarget as Node | null)) close(); }}
        className={cn(
          "flex h-12 w-full rounded-[12px] border border-[hsl(var(--border))] bg-white pl-10 py-2.5 text-base transition-all duration-200 placeholder:text-[hsl(var(--muted-foreground))] apple-focus disabled:cursor-not-allowed disabled:opacity-50 hover:border-[hsl(var(--ring)/0.4)]",
          canClear ? "pr-11" : "pr-4",
        )}
      />
      {canClear && (
        <button
          type="button"
          aria-label={messages.clear}
          onMouseDown={(e) => e.preventDefault()}
          onClick={() => { onClear!(); close(); }}
          className="absolute right-2 top-1/2 -translate-y-1/2 rounded-full p-2 text-[hsl(var(--muted-foreground))] hover:bg-black/5 apple-focus"
        >
          <X aria-hidden="true" className="h-4 w-4" />
        </button>
      )}
      {open && (
        <div
          onClick={(e) => e.preventDefault()}
          className="absolute left-0 right-0 top-full z-50 mt-1 overflow-hidden rounded-[12px] border border-[hsl(var(--border))] bg-white shadow-lg animate-in fade-in slide-in-from-top-1 duration-150 motion-reduce:animate-none"
        >
          <ul id={listId} role="listbox" aria-label={ariaLabel} className="max-h-64 overflow-y-auto p-1">
            {options.map((o, i) => {
              const detail = renderDetail?.(o);
              return (
                <li
                  key={getKey(o)}
                  id={optionId(o)}
                  role="option"
                  aria-selected={i === active}
                  aria-describedby={detail != null && detail !== "" ? `${optionId(o)}-detail` : undefined}
                  data-key={getKey(o)}
                  onMouseDown={(e) => e.preventDefault()}
                  onMouseEnter={() => setActive(i)}
                  onClick={(e) => {
                    // Forms wrap fields in <label>: without this the click would
                    // be forwarded to the input and reopen the list.
                    e.preventDefault();
                    choose(o);
                  }}
                  className={cn(
                    "flex cursor-pointer select-none items-center justify-between gap-3 rounded-[8px] px-3 py-2 text-sm transition-colors duration-150",
                    i === active ? "bg-[hsl(var(--ring)/0.14)]" : "hover:bg-black/5",
                    getKey(o) === selectedKey && "font-semibold",
                  )}
                >
                  <span className="truncate">{getLabel(o)}</span>
                  {detail != null && detail !== "" && (
                    <span id={`${optionId(o)}-detail`} aria-hidden="true" className="shrink-0 text-xs text-[hsl(var(--muted-foreground))]">
                      {detail}
                    </span>
                  )}
                </li>
              );
            })}
          </ul>
          <div role="status" aria-live="polite" className="border-t border-[hsl(var(--border))] px-3 py-2 text-xs text-[hsl(var(--muted-foreground))]">
            {status}
          </div>
        </div>
      )}
    </div>
  );
}
