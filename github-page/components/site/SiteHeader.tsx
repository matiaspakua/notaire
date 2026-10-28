"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useCallback, useEffect, useRef, useState } from "react";
import { groupsFor, PRIMARY_NAV, REPO_URL, sectionOf, type SectionKey } from "@/lib/site";
import { useFocusTrap } from "./useFocusTrap";
import { SearchDialog } from "./SearchDialog";
import { CloseIcon, GitHubIcon, Logo, MenuIcon, SearchIcon } from "./icons";

const DRAWER_ID = "site-drawer";

function norm(p: string) {
  return p.endsWith("/") ? p : `${p}/`;
}

/** The primary entry that a path belongs to (Releases is its own entry, inside the docs). */
function activeKey(pathname: string): SectionKey {
  return sectionOf(pathname);
}

/**
 * The one site header (#1970), rendered by the root layout on every page.
 * Desktop: logo, the four primary entries, search, GitHub. Below `lg`: search and a menu
 * button that opens a modal drawer with the entries and the current section's pages.
 */
export function SiteHeader() {
  const pathname = norm(usePathname() || "/");
  const section = activeKey(pathname);
  const [menuOpen, setMenuOpen] = useState(false);
  const [searchOpen, setSearchOpen] = useState(false);
  const menuButton = useRef<HTMLButtonElement>(null);
  const searchButton = useRef<HTMLButtonElement>(null);
  const drawer = useRef<HTMLDivElement>(null);

  const closeMenu = useCallback(() => setMenuOpen(false), []);
  const closeSearch = useCallback(() => setSearchOpen(false), []);
  useFocusTrap(drawer, menuOpen, closeMenu, { returnTo: menuButton });

  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      const t = e.target as HTMLElement | null;
      const typing = t && (t.tagName === "INPUT" || t.tagName === "TEXTAREA" || t.isContentEditable);
      if ((e.key === "/" && !typing) || (e.key.toLowerCase() === "k" && (e.metaKey || e.ctrlKey))) {
        e.preventDefault();
        setMenuOpen(false);
        setSearchOpen(true);
      }
    }
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, []);

  const accent = section === "factory" ? "text-factory-700" : "text-brand-700";
  const groups = groupsFor(section);
  const currentLabel = PRIMARY_NAV.find((n) => n.key === (section === "releases" ? "docs" : section))?.label;

  return (
    <>
      <header
        data-site-header
        className="sticky top-0 z-50 h-[var(--header-h)] border-b border-slate-200/80 bg-white/85 backdrop-blur-xl backdrop-saturate-150"
      >
        <div className="mx-auto flex h-full max-w-7xl items-center gap-3 px-4 sm:px-6">
          <Link href="/" className="mr-2 flex shrink-0 items-center gap-2.5 rounded-lg" aria-label="Notaire home">
            <Logo id="logo-header" />
            <span className="text-[15px] font-semibold tracking-tight text-slate-900">Notaire</span>
          </Link>

          <nav aria-label="Primary" className="hidden items-center gap-1 lg:flex">
            {PRIMARY_NAV.map((item) => {
              const active = item.key === section;
              return (
                <Link
                  key={item.key}
                  href={item.href}
                  aria-current={active ? "true" : undefined}
                  className={`relative rounded-full px-3.5 py-2 text-sm font-medium transition-colors ${
                    active ? `${accent} bg-slate-100` : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
                  }`}
                >
                  {item.label}
                </Link>
              );
            })}
          </nav>

          <div className="ml-auto flex items-center gap-1.5">
            <button
              ref={searchButton}
              type="button"
              onClick={() => setSearchOpen(true)}
              aria-label="Search the site"
              aria-haspopup="dialog"
              className="flex h-10 items-center gap-2 rounded-full border border-slate-200 bg-white px-3 text-sm text-slate-600 transition-colors hover:border-slate-300 hover:text-slate-900 sm:w-56"
            >
              <SearchIcon />
              <span className="hidden sm:inline">Search</span>
              <kbd className="ml-auto hidden rounded border border-slate-300 px-1.5 font-mono text-[11px] text-slate-600 sm:inline">/</kbd>
            </button>
            <a
              href={REPO_URL}
              className="hidden h-10 w-10 items-center justify-center rounded-full text-slate-600 transition-colors hover:bg-slate-100 hover:text-slate-900 sm:flex"
              aria-label="Notaire on GitHub"
            >
              <GitHubIcon />
            </a>
            <button
              ref={menuButton}
              type="button"
              onClick={() => setMenuOpen((o) => !o)}
              aria-expanded={menuOpen}
              aria-controls={DRAWER_ID}
              aria-label={menuOpen ? "Close menu" : "Open menu"}
              className="flex h-10 w-10 items-center justify-center rounded-full text-slate-700 transition-colors hover:bg-slate-100 lg:hidden"
            >
              {menuOpen ? <CloseIcon /> : <MenuIcon />}
            </button>
          </div>
        </div>
      </header>

      <div hidden={!menuOpen} className="fixed inset-0 z-[60] lg:hidden">
        <div className="absolute inset-0 bg-slate-950/40 backdrop-blur-sm" onClick={closeMenu} aria-hidden="true" />
        <div
          ref={drawer}
          id={DRAWER_ID}
          role="dialog"
          aria-modal="true"
          aria-label="Site menu"
          tabIndex={-1}
          className="absolute inset-y-0 right-0 flex w-[min(22rem,88vw)] flex-col overflow-y-auto bg-white shadow-2xl motion-safe:animate-[drawer-in_200ms_ease-out]"
        >
          <div className="flex h-[var(--header-h)] shrink-0 items-center justify-between border-b border-slate-200 px-4">
            <span className="text-sm font-semibold text-slate-900">Menu</span>
            <button
              type="button"
              onClick={closeMenu}
              aria-label="Close menu"
              className="flex h-10 w-10 items-center justify-center rounded-full text-slate-700 hover:bg-slate-100"
            >
              <CloseIcon />
            </button>
          </div>

          <nav aria-label="Sections" className="border-b border-slate-200 p-3">
            <ul className="space-y-1">
              {PRIMARY_NAV.map((item) => {
                const active = item.key === section;
                return (
                  <li key={item.key}>
                    <Link
                      href={item.href}
                      onClick={closeMenu}
                      aria-current={active ? "true" : undefined}
                      className={`block rounded-xl px-3 py-2.5 ${active ? "bg-slate-100" : "hover:bg-slate-50"}`}
                    >
                      <span className={`block text-[15px] font-semibold ${active ? accent : "text-slate-900"}`}>{item.label}</span>
                      <span className="block text-[13px] text-slate-600">{item.blurb}</span>
                    </Link>
                  </li>
                );
              })}
            </ul>
          </nav>

          <nav aria-label={`${currentLabel ?? "Section"} pages`} className="flex-1 p-3">
            {groups.map((g) => (
              <div key={g.label} className="mb-4">
                <p className="px-3 pb-1 text-[11px] font-semibold uppercase tracking-wider text-slate-500">{g.label}</p>
                <ul>
                  {g.pages.map((p) => {
                    const current = p.href === pathname;
                    return (
                      <li key={p.href}>
                        <Link
                          href={p.href}
                          onClick={closeMenu}
                          aria-current={current ? "page" : undefined}
                          className={`block rounded-lg px-3 py-2 text-sm ${
                            current ? `${accent} bg-slate-100 font-semibold` : "text-slate-700 hover:bg-slate-50"
                          }`}
                        >
                          {p.title}
                        </Link>
                      </li>
                    );
                  })}
                </ul>
              </div>
            ))}
          </nav>

          <div className="border-t border-slate-200 p-4">
            <a href={REPO_URL} className="flex items-center gap-2 text-sm font-medium text-slate-700 hover:text-slate-900">
              <GitHubIcon className="h-4 w-4" /> matiaspakua/notaire
            </a>
          </div>
        </div>
      </div>

      {searchOpen && <SearchDialog onClose={closeSearch} returnTo={searchButton} />}
    </>
  );
}
