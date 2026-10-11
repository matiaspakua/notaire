"use client";

/**
 * Searchable management (case) picker (#1340) on the shared SearchCombobox. An
 * empty query lists the newest managements; typing searches by case number or
 * client (name or document) on the server instead of filtering a size=1000
 * list. The value is a management id, loaded by id so a form shows it wherever
 * it sits in the table.
 */
import { useState } from "react";
import { useTranslations } from "next-intl";
import { SearchCombobox } from "@/components/shared/SearchCombobox";
import { useManagement, useManagementSearch, useRecentManagements } from "@/hooks/useManagementSearch";
import type { DeedManagement } from "@/types";

export interface ManagementPickerProps {
  /** Selected management id. */
  value?: number;
  onChange: (management: DeedManagement | undefined) => void;
  "aria-label": string;
  placeholder?: string;
  allowClear?: boolean;
  disabled?: boolean;
  className?: string;
  "data-testid"?: string;
}

/** "<number> — <heading>": the number first, so it reads the same in es and en. */
export function managementLabel(g: DeedManagement): string {
  return g.heading ? `${g.number} — ${g.heading}` : String(g.number ?? `#${g.idManagement}`);
}

export function ManagementPicker({ value, onChange, allowClear = false, ...rest }: ManagementPickerProps) {
  const t = useTranslations("managementPicker");
  const [known, setKnown] = useState<DeedManagement | undefined>();

  const knownMatches = known?.idManagement === value ? known : undefined;
  const loaded = useManagement(value, value != null && !knownMatches);
  const current = value == null ? undefined : knownMatches ?? loaded.data;

  function useSource(query: string, open: boolean) {
    const search = useManagementSearch(query, open);
    const recent = useRecentManagements(open && !query);
    return {
      options: query ? search.data : recent.data?.content,
      pending: query ? search.isFetching || search.isPlaceholderData : recent.isLoading,
      failed: query ? search.isError : recent.isError,
    };
  }

  return (
    <SearchCombobox<DeedManagement>
      {...rest}
      useSource={useSource}
      getKey={(g) => g.idManagement!}
      getLabel={managementLabel}
      renderDetail={(g) => g.currentStatus ?? undefined}
      displayValue={current ? managementLabel(current) : ""}
      selectedKey={value}
      onSelect={(g) => { setKnown(g); onChange(g); }}
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
