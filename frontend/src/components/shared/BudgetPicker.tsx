"use client";

/**
 * Searchable budget picker (#1340) on the shared SearchCombobox. An empty query
 * lists the newest budgets; typing searches by budget number or client (name or
 * document) on the server instead of filtering a size=1000 list. The value is a
 * budget id, loaded by id so a form shows it wherever it sits in the table.
 */
import { useState } from "react";
import { useTranslations } from "next-intl";
import { formatCurrency, fullName } from "@/lib/utils";
import { SearchCombobox } from "@/components/shared/SearchCombobox";
import { useBudget, useBudgetSearch, useRecentBudgets } from "@/hooks/useBudgetSearch";
import type { Presupuesto } from "@/types";

export interface BudgetPickerProps {
  /** Selected budget id. */
  value?: number;
  onChange: (budget: Presupuesto | undefined) => void;
  "aria-label": string;
  placeholder?: string;
  allowClear?: boolean;
  disabled?: boolean;
  className?: string;
  "data-testid"?: string;
}

/** "#<number> — <client>": the number first, so it reads the same in es and en. */
export function budgetLabel(b: Presupuesto): string {
  return b.person ? `#${b.idBudget} — ${fullName(b.person)}` : `#${b.idBudget}`;
}

export function BudgetPicker({ value, onChange, allowClear = false, ...rest }: BudgetPickerProps) {
  const t = useTranslations("budgetPicker");
  const [known, setKnown] = useState<Presupuesto | undefined>();

  const knownMatches = known?.idBudget === value ? known : undefined;
  const loaded = useBudget(value, value != null && !knownMatches);
  const current = value == null ? undefined : knownMatches ?? loaded.data;

  function useSource(query: string, open: boolean) {
    const search = useBudgetSearch(query, open);
    const recent = useRecentBudgets(open && !query);
    return {
      options: query ? search.data : recent.data?.content,
      pending: query ? search.isFetching || search.isPlaceholderData : recent.isLoading,
      failed: query ? search.isError : recent.isError,
    };
  }

  return (
    <SearchCombobox<Presupuesto>
      {...rest}
      useSource={useSource}
      getKey={(b) => b.idBudget!}
      getLabel={budgetLabel}
      renderDetail={(b) => (b.propertyAmount != null ? formatCurrency(b.propertyAmount) : undefined)}
      displayValue={current ? budgetLabel(current) : ""}
      selectedKey={value}
      onSelect={(b) => { setKnown(b); onChange(b); }}
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
