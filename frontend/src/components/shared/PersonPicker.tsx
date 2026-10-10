"use client";

/**
 * Searchable person picker (#1340 slice 3) on the shared SearchCombobox. An
 * empty query lists the newest people; typing searches the server (debounced)
 * instead of filtering a size=1000 list. The current value is a person id,
 * loaded by id so edit forms show it wherever it sits in the table.
 */
import { useState } from "react";
import { useTranslations } from "next-intl";
import { fullName } from "@/lib/utils";
import { SearchCombobox } from "@/components/shared/SearchCombobox";
import { usePersona, usePersonSearch, useRecentPersonas } from "@/hooks/usePersonSearch";
import type { Persona } from "@/types";

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
  clientsOnly = false,
  allowClear = false,
  ...rest
}: PersonPickerProps) {
  const t = useTranslations("personPicker");
  const [known, setKnown] = useState<Persona | undefined>(selected);

  const knownMatches = (selected?.personId === value ? selected : undefined) ?? (known?.personId === value ? known : undefined);
  const loaded = usePersona(value, value != null && !knownMatches);
  const current = value == null ? undefined : knownMatches ?? loaded.data;

  function useSource(query: string, open: boolean) {
    const search = usePersonSearch(query, open);
    const recent = useRecentPersonas(open && !query);
    const list = query ? search.data : recent.data?.content;
    return {
      options: list?.filter((p) => !clientsOnly || p.isClient),
      pending: query ? search.isFetching || search.isPlaceholderData : recent.isLoading,
      failed: query ? search.isError : recent.isError,
    };
  }

  return (
    <SearchCombobox<Persona>
      {...rest}
      useSource={useSource}
      getKey={(p) => p.personId!}
      getLabel={(p) => fullName(p)}
      renderDetail={(p) => p.identificationNumber}
      displayValue={current ? fullName(current) : ""}
      selectedKey={value}
      onSelect={(p) => { setKnown(p); onChange(p); }}
      onClear={allowClear ? () => { setKnown(undefined); onChange(undefined); } : undefined}
      messages={{
        placeholder: t("placeholder"),
        searching: t("searching"),
        noResults: t("noResults"),
        results: (count) => t("results", { count }),
        recentHint: t("recentHint"),
        clear: t("clear"),
        loadError: t("loadError"),
      }}
    />
  );
}
