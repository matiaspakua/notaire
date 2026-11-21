import { useCallback, useEffect, useMemo, useState } from "react";

import { api, ApiError, type Grant, rememberCsrf, type SessionInfo, type WorkspaceInfo } from "./api.ts";
import { ActionForm, Field } from "./components/Form.tsx";
import { Icon, type IconName } from "./components/Icon.tsx";
import { ErrorNote, Skeleton } from "./components/State.tsx";
import { Current, type WorkspaceContext } from "./context.ts";
import { useHashRoute } from "./hooks.ts";
import { I18n, initialLang, type Key, type Lang, translator, useT } from "./i18n.ts";
import { Catalogs, KINDS } from "./views/Catalogs.tsx";
import { Coach } from "./views/Coach.tsx";
import { Knowledge } from "./views/Knowledge.tsx";
import { Overview } from "./views/Overview.tsx";
import { Projects } from "./views/Projects.tsx";
import { Runs } from "./views/Runs.tsx";

const VIEWS: { id: string; icon: IconName; label: Key }[] = [
  { id: "overview", icon: "overview", label: "nav.overview" },
  { id: "runs", icon: "runs", label: "nav.runs" },
  { id: "projects", icon: "projects", label: "nav.projects" },
  { id: "catalogs", icon: "catalogs", label: "nav.catalogs" },
  { id: "knowledge", icon: "knowledge", label: "nav.knowledge" },
  { id: "coach", icon: "coach", label: "nav.coach" },
];

type Theme = "light" | "dark" | "system";

function stored<T extends string>(key: string, fallback: T): T {
  try {
    return (localStorage.getItem(key) as T | null) ?? fallback;
  } catch {
    return fallback;
  }
}

function store(key: string, value: string): void {
  try {
    localStorage.setItem(key, value);
  } catch {
    // a blocked storage only loses the preference
  }
}

function isDark(theme: Theme): boolean {
  return theme === "dark" || (theme === "system" && window.matchMedia("(prefers-color-scheme: dark)").matches);
}

function SignIn({ workspace, onSignedIn }: { workspace: WorkspaceInfo; onSignedIn: () => void }) {
  const t = useT();
  const [token, setToken] = useState("");
  return (
    <main className="signin" id="main">
      <div className="signin-desk">
        <h1>{t("signIn.title", { ws: workspace.label })}</h1>
        <p>{t("signIn.body")}</p>
        <pre className="command"><code>aisdlc serve-token add --identity &lt;you&gt; --scope write</code></pre>
        <ActionForm
          submitLabel={t("signIn.submit")}
          busyLabel={t("signIn.busy")}
          onSubmit={async () => {
            await api<Grant>(`/api/workspaces/${encodeURIComponent(workspace.id)}/session`, { method: "POST", body: { token } });
            setToken("");
            onSignedIn();
          }}
        >
          <Field label={t("signIn.token")} hint={t("signIn.kept")}>
            {(id, describedBy) => (
              <input id={id} type="password" required autoComplete="off" spellCheck={false} className="mono"
                value={token} onChange={(e) => setToken(e.target.value)} aria-describedby={describedBy} autoFocus />
            )}
          </Field>
        </ActionForm>
      </div>
    </main>
  );
}

function Shell({
  workspaces,
  session,
  current,
  view,
  sub,
  lang,
  theme,
  onLang,
  onTheme,
  onSession,
  go,
}: {
  workspaces: WorkspaceInfo[];
  session: SessionInfo;
  current: WorkspaceInfo;
  view: string;
  sub: string | undefined;
  lang: Lang;
  theme: Theme;
  onLang: () => void;
  onTheme: () => void;
  onSession: () => void;
  go: (path: string) => void;
}) {
  const t = useT();
  const grant = session.workspaces[current.id];
  const [signOutError, setSignOutError] = useState<ApiError>();
  const context = useMemo<WorkspaceContext | null>(
    () => grant ? { workspace: current, grant, writable: grant.write_enabled, go, expire: onSession } : null,
    [current, grant, go, onSession],
  );
  const signOut = async () => {
    try {
      await api(`/api/workspaces/${encodeURIComponent(current.id)}/session`, { method: "DELETE" });
      onSession();
    } catch (error) {
      setSignOutError(error as ApiError);
    }
  };
  const viewLabel = VIEWS.find((v) => v.id === view)?.label ?? "nav.overview";
  return (
    <div className="shell">
      <a className="skip" href="#main">Skip to content</a>
      <header className="bar">
        <span className="mark" aria-hidden="true">
          <svg viewBox="0 0 20 20" width="20" height="20"><path d="M3 15V8l4 3V8l4 3V5h6v10H3Z" fill="currentColor" /></svg>
        </span>
        <span className="brand">{t("app.name")}</span>
        <label className="ws">
          <span className="sr-only">{t("ws.label")}</span>
          <select value={current.id} onChange={(e) => go(`${e.target.value}/${view}`)}>
            {workspaces.map((ws) => (
              <option key={ws.id} value={ws.id}>
                {ws.label}{session.workspaces[ws.id] ? "" : ` · ${t("ws.signedOut")}`}
              </option>
            ))}
          </select>
        </label>
        <span className="bar-gap" />
        {grant && (
          <span className="who">
            <span className="who-name">{grant.identity}</span>
            <span className={`tag tag-scope-${grant.scope}`}>{t(`scope.${grant.scope}`)}</span>
          </span>
        )}
        <button type="button" className="btn btn-icon" onClick={onTheme} aria-label={t("theme.toggle")} title={t("theme.toggle")}>
          <Icon name={isDark(theme) ? "sun" : "moon"} />
        </button>
        <button type="button" className="btn btn-icon btn-lang" onClick={onLang} lang={lang === "en" ? "es" : "en"} aria-label={t("lang.toggle")}>
          <Icon name="globe" />
          <span aria-hidden="true">{lang === "en" ? "ES" : "EN"}</span>
        </button>
        {grant && (
          <button type="button" className="btn btn-icon" onClick={signOut} aria-label={t("signOut")} title={t("signOut")}>
            <Icon name="logout" />
          </button>
        )}
      </header>
      {signOutError && <ErrorNote error={signOutError} />}
      {!context ? (
        <SignIn workspace={current} onSignedIn={onSession} />
      ) : (
        <Current.Provider value={context}>
          <div className="body">
            <nav className="rail" aria-label={t("nav.label")}>
              <ul>
                {VIEWS.map((item) => (
                  <li key={item.id}>
                    <a href={`#/${current.id}/${item.id}`} aria-current={item.id === view ? "page" : undefined}>
                      <Icon name={item.icon} />
                      <span>{t(item.label)}</span>
                      {item.id === "coach" && grant.scope !== "owner" && <Icon name="lock" size={14} label={t("error.forbidden")} />}
                    </a>
                  </li>
                ))}
              </ul>
            </nav>
            <main className="main" id="main" tabIndex={-1}>
              <header className="page-head">
                <h1>{t(viewLabel)}</h1>
                {!grant.write_enabled && (
                  <p className="readonly">
                    <Icon name="lock" size={16} />
                    {grant.scope === "read" ? t("readonly", { scope: t(`scope.${grant.scope}`) }) : t("writesOff")}
                  </p>
                )}
              </header>
              {view === "runs" && <Runs />}
              {view === "projects" && <Projects />}
              {view === "catalogs" && <Catalogs kind={sub && (KINDS as readonly string[]).includes(sub) ? sub : "providers"} />}
              {view === "knowledge" && <Knowledge />}
              {view === "coach" && <Coach />}
              {!["runs", "projects", "catalogs", "knowledge", "coach"].includes(view) && <Overview />}
            </main>
          </div>
        </Current.Provider>
      )}
    </div>
  );
}

export function App() {
  const [lang, setLang] = useState<Lang>(initialLang);
  const [theme, setTheme] = useState<Theme>(() => stored<Theme>("fd.theme", "system"));
  const [workspaces, setWorkspaces] = useState<WorkspaceInfo[]>();
  const [session, setSession] = useState<SessionInfo>();
  const [error, setError] = useState<ApiError>();
  const [route, go] = useHashRoute();
  const t = useMemo(() => translator(lang), [lang]);

  const refresh = useCallback(async () => {
    try {
      const [list, view] = await Promise.all([api<WorkspaceInfo[]>("/api/workspaces"), api<SessionInfo>("/api/session")]);
      rememberCsrf(view.csrf);
      setWorkspaces(list);
      setSession(view);
      setError(undefined);
    } catch (failure) {
      setError(failure as ApiError);
    }
  }, []);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  useEffect(() => {
    document.documentElement.lang = lang;
  }, [lang]);

  useEffect(() => {
    const root = document.documentElement;
    if (theme === "system") {
      root.removeAttribute("data-theme");
    } else {
      root.setAttribute("data-theme", theme);
    }
  }, [theme]);

  const toggleTheme = () => {
    const next: Theme = isDark(theme) ? "light" : "dark";
    store("fd.theme", next);
    setTheme(next);
  };
  const toggleLang = () => {
    const next: Lang = lang === "en" ? "es" : "en";
    store("fd.lang", next);
    setLang(next);
  };

  const [wsId, view = "overview", sub] = route;
  const current = workspaces?.find((ws) => ws.id === wsId) ?? workspaces?.[0];
  useEffect(() => {
    if (current && wsId !== current.id) {
      go(`${current.id}/${view}`);
    }
  }, [current, wsId, view, go]);

  useEffect(() => {
    if (current) {
      document.title = `${current.label} · ${t("app.name")}`;
    }
  }, [current, t]);

  return (
    <I18n.Provider value={{ t, lang }}>
      {error && !workspaces ? (
        <main className="signin" id="main"><div className="signin-desk"><ErrorNote error={error} onRetry={refresh} /></div></main>
      ) : !workspaces || !session || !current ? (
        <main className="signin" id="main"><Skeleton rows={4} /></main>
      ) : (
        <Shell
          workspaces={workspaces}
          session={session}
          current={current}
          view={view}
          sub={sub}
          lang={lang}
          theme={theme}
          onLang={toggleLang}
          onTheme={toggleTheme}
          onSession={refresh}
          go={go}
        />
      )}
    </I18n.Provider>
  );
}
