"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useTranslations } from "next-intl";

function camelCase(segment: string): string {
  return segment.replace(/-([a-z])/g, (_, c: string) => c.toUpperCase());
}

/**
 * Message key for a route segment (#1354). Administration sub-routes have their
 * own labels ("documentos" there means document types); record ids have none.
 */
export function breadcrumbKey(segment: string, parent?: string): string {
  const group = parent === "administracion" ? "admin" : "segments";
  return `breadcrumb.${group}.${camelCase(segment)}`;
}

export function Breadcrumb() {
  const pathname = usePathname();
  const t = useTranslations();
  const segments = pathname.split("/").filter(Boolean);

  if (segments.length <= 1) return null;

  const crumbs = segments.map((seg, i) => {
    const key = breadcrumbKey(seg, segments[i - 1]);
    return {
      // Ids and unknown segments are shown as is; the unit test requires a key
      // for every static route, so no Spanish fallback is needed.
      label: t.has(key) ? t(key) : decodeURIComponent(seg),
      href: "/" + segments.slice(0, i + 1).join("/"),
      isLast: i === segments.length - 1,
    };
  });

  return (
    <nav aria-label={t("breadcrumb.label")} data-testid="breadcrumb">
      <ol className="flex items-center gap-1 text-sm text-muted-foreground flex-wrap">
        {crumbs.map((crumb, i) => (
          <li key={crumb.href} className="flex items-center gap-1">
            {i > 0 && <span aria-hidden="true" className="text-muted-foreground select-none">/</span>}
            {crumb.isLast ? (
              <span className="font-medium text-foreground">{crumb.label}</span>
            ) : (
              <Link
                href={crumb.href}
                className="hover:text-foreground transition-colors"
              >
                {crumb.label}
              </Link>
            )}
          </li>
        ))}
      </ol>
    </nav>
  );
}
