"use client";

/**
 * Searchable deed picker (#1340) on the shared SearchCombobox. An empty query
 * lists the newest signed deeds; typing a number searches the server instead of
 * filtering a size=1000 list. Only signed deeds are offered (testimonies,
 * registration drafts and folios need one). `exclude` hides deeds a form cannot
 * take, such as a deed already linked to another folio. The value is a deed id,
 * loaded by id so a form shows it wherever it sits in the table.
 */
import { useState } from "react";
import { useTranslations } from "next-intl";
import { SearchCombobox } from "@/components/shared/SearchCombobox";
import { useDeed, useDeedSearch, useRecentSignedDeeds } from "@/hooks/useDeedSearch";
import type { Deed } from "@/types";
import { useFormat } from "@/hooks/useFormat";

export interface DeedPickerProps {
  /** Selected deed id. */
  value?: number;
  onChange: (deed: Deed | undefined) => void;
  /** Deeds to leave out of the options (the current value still shows). */
  exclude?: (deed: Deed) => boolean;
  "aria-label": string;
  placeholder?: string;
  allowClear?: boolean;
  disabled?: boolean;
  className?: string;
  "data-testid"?: string;
}

export function DeedPicker({ value, onChange, exclude, allowClear = false, ...rest }: DeedPickerProps) {
  const t = useTranslations("deedPicker");
  const fmt = useFormat();
  const [known, setKnown] = useState<Deed | undefined>();
  const label = (d: Deed) => t("option", { number: d.number ?? d.idDeed ?? "—" });

  const knownMatches = known?.idDeed === value ? known : undefined;
  const loaded = useDeed(value, value != null && !knownMatches);
  const current = value == null ? undefined : knownMatches ?? loaded.data;

  function useSource(query: string, open: boolean) {
    const search = useDeedSearch(query, open);
    const recent = useRecentSignedDeeds(open && !query);
    const options = query ? search.data : recent.data?.content;
    return {
      options: exclude && options ? options.filter((d) => !exclude(d)) : options,
      pending: query ? search.isFetching || search.isPlaceholderData : recent.isLoading,
      failed: query ? search.isError : recent.isError,
    };
  }

  return (
    <SearchCombobox<Deed>
      {...rest}
      useSource={useSource}
      getKey={(d) => d.idDeed!}
      getLabel={label}
      renderDetail={(d) => (d.dateDeedrecording ? fmt.date(d.dateDeedrecording) : undefined)}
      displayValue={current ? label(current) : ""}
      selectedKey={value}
      onSelect={(d) => { setKnown(d); onChange(d); }}
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
