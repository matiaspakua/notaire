import { useCallback, useEffect, useMemo, useState } from "react";

import { api, ApiError, type Grant, rememberCsrf, type SessionInfo, type WorkspaceInfo } from "./api.ts";
import { ActionForm, Field } from "./components/Form.tsx";
import { Icon, type IconName } from "./components/Icon.tsx";
import { IntentBar } from "./components/IntentBar.tsx";
import { ErrorNote, Section, Skeleton } from "./components/State.tsx";
import { Current, type WorkspaceContext } from "./context.ts";
import { useHashRoute, useLiveSync } from "./hooks.ts";
import { I18n, initialLang, type Key, type Lang, translator, useT } from "./i18n.ts";
import { Catalogs, KINDS } from "./views/Catalogs.tsx";
import { Coach } from "./views/Coach.tsx";
import { Knowledge } from "./views/Knowledge.tsx";
import { Metrics } from "./views/Metrics.tsx";
import { Overview } from "./views/Overview.tsx";
import { Projects } from "./views/Projects.tsx";
import { Runs } from "./views/Runs.tsx";

interface View {
  id: string;
  icon: IconName;
  label: Key;
  subtitle: Key;
}

const SECTIONS: { label: Key; views: View[] }[] = [
  {
    label: "nav.views",
    views: [
      { id: "overview", icon: "grid", label: "nav.overview", subtitle: "line.hint" },
      { id: "runs", icon: "runs", label: "nav.runs", subtitle: "runs.subtitle" },
      { id: "metrics", icon: "metrics", label: "nav.metrics", subtitle: "metrics.subtitle" },
      { id: "catalogs", icon: "config", label: "nav.catalogs", subtitle: "catalogs.subtitle" },
      { id: "projects", icon: "projects", label: "nav.projects", subtitle: "projects.subtitle" },
    ],
  },
  { label: "nav.insights", views: [{ id: "coach", icon: "coach", label: "nav.coach", subtitle: "coach.subtitle" }] },
  {
    label: "nav.integrations",
    views: [{ id: "knowledge", icon: "knowledge", label: "nav.knowledge", subtitle: "knowledge.subtitle" }],
  },
];
const VIEWS = SECTIONS.flatMap((section) => section.views);

type Theme = "light" | "dark";

function storedTheme(): Theme {
  try {
    const saved = localStorage.getItem("fd.theme");
    if (saved === "light" || saved === "dark") {
      return saved;
    }
  } catch {
    // storage blocked: follow the system
  }
  return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}

function store(key: string, value: string): void {
  try {
    localStorage.setItem(key, value);
  } catch {
    // a blocked storage only loses the preference
  }
}

function SignIn({ workspace, onSignedIn }: { workspace: WorkspaceInfo; onSignedIn: () => void }) {
  const t = useT();
  const [token, setToken] = useState("");
  return (
    <div className="signin">
      <div className="signin-card">
        <Section title={t("signIn.title", { ws: workspace.label })} hint={t("signIn.body")}>
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
        </Section>
      </div>
    </div>
  );
}

const SIDEBAR_KEY = "fd.sidebar";

function storedCollapsed(): boolean {
  try {
    return localStorage.getItem(SIDEBAR_KEY) === "collapsed";
  } catch {
    return false;
  }
}

function Sidebar({ current, view, grant, theme, lang, liveState, collapsed, onCollapse, onTheme, onLang }: {
  current: WorkspaceInfo;
  collapsed: boolean;
  onCollapse: () => void;
  view: string;
  grant: Grant | undefined;
  theme: Theme;
  lang: Lang;
  liveState: string;
  onTheme: () => void;
  onLang: () => void;
}) {
  const t = useT();
  const liveLabel = liveState === "live" ? t("live.connected") : liveState === "polling" ? t("live.polling") : t("live.connecting");
  const liveDot = liveState === "live" ? "connected" : liveState === "polling" ? "down" : "";
  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <span className="sidebar-brand-icon" aria-hidden="true">F</span>
        <span className="sidebar-brand-text">{t("app.name")}</span>
        <button type="button" className="sidebar-collapse" onClick={onCollapse} aria-expanded={!collapsed}
          aria-controls="sidebar-nav" aria-label={collapsed ? t("nav.expand") : t("nav.collapse")}
          title={collapsed ? t("nav.expand") : t("nav.collapse")}>
          <Icon name="panel" />
        </button>
      </div>
      {grant && (
        <nav id="sidebar-nav" className="sidebar-nav" aria-label={t("nav.label")}>
          {SECTIONS.map((section) => (
            <div key={section.label} className="sidebar-section">
              <div className="sidebar-section-label" aria-hidden="true">{t(section.label)}</div>
              <ul>
                {section.views.map((item) => (
                  <li key={item.id}>
                    <a className="sidebar-nav-item" href={`#/${current.id}/${item.id}`}
                      aria-current={item.id === view ? "page" : undefined} title={collapsed ? t(item.label) : undefined}>
                      <Icon name={item.icon} />
                      <span className="sidebar-nav-text">{t(item.label)}</span>
                      {item.id === "coach" && grant.scope !== "owner" && (
                        <span className="lock"><Icon name="lock" size={14} label={t("error.forbidden")} /></span>
                      )}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </nav>
      )}
      <div className="sidebar-footer">
        <button type="button" className="sidebar-toggle" onClick={onTheme} aria-label={t("theme.toggle")}
          title={collapsed ? t("theme.toggle") : undefined}>
          <Icon name={theme === "dark" ? "sun" : "moon"} />
          <span className="toggle-text">{theme === "dark" ? t("theme.light") : t("theme.dark")}</span>
        </button>
        <button type="button" className="sidebar-toggle" onClick={onLang} lang={lang === "en" ? "es" : "en"}
          title={collapsed ? t("lang.toggle") : undefined}>
          <Icon name="globe" />
          <span className="toggle-text">{t("lang.toggle")}</span>
        </button>
        <p className="sidebar-status" role="status" data-testid="live-indicator" title={collapsed ? liveLabel : undefined}>
          <span className={`status-dot ${liveDot}`} aria-hidden="true" />
          <span className="toggle-text">{liveLabel}</span>
        </p>
      </div>
    </aside>
  );
}

function Shell({ workspaces, session, current, view, sub, lang, theme, onLang, onTheme, onSession, go }: {
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
  const [version, setVersion] = useState(0);
  const bumpVersion = useCallback(() => setVersion((v) => v + 1), []);
  const liveState = useLiveSync(current.id, Boolean(grant), bumpVersion);
  const [signOutError, setSignOutError] = useState<ApiError>();
  const [collapsed, setCollapsed] = useState(storedCollapsed);
  const toggleCollapsed = () => {
    store(SIDEBAR_KEY, collapsed ? "expanded" : "collapsed");
    setCollapsed(!collapsed);
  };
  const context = useMemo<WorkspaceContext | null>(
    () => (grant ? { workspace: current, grant, writable: grant.write_enabled, go, expire: onSession, version, bumpVersion } : null),
    [current, grant, go, onSession, version, bumpVersion],
  );
  const signOut = async () => {
    try {
      await api(`/api/workspaces/${encodeURIComponent(current.id)}/session`, { method: "DELETE" });
      onSession();
    } catch (error) {
      setSignOutError(error as ApiError);
    }
  };
  const active = VIEWS.find((item) => item.id === view) ?? VIEWS[0];
  return (
    <div className="app-shell" data-sidebar={collapsed ? "collapsed" : "expanded"}>
      <a className="skip" href="#main">{t("skip")}</a>
      <Sidebar current={current} view={active.id} grant={grant} theme={theme} lang={lang} liveState={liveState}
        collapsed={collapsed} onCollapse={toggleCollapsed} onTheme={onTheme} onLang={onLang} />
      <div className="main-content">
        <header className="top-bar">
          <span className="source-badge">aisdlc</span>
          <label>
            <span className="sr-only">{t("ws.label")}</span>
            <select className="ws-select" value={current.id} onChange={(e) => go(`${e.target.value}/${active.id}`)}>
              {workspaces.map((ws) => (
                <option key={ws.id} value={ws.id}>
                  {ws.label}
                  {!ws.reachable ? ` · ${t("ws.unreachable")}` : session.workspaces[ws.id] ? "" : ` · ${t("ws.signedOut")}`}
                </option>
              ))}
            </select>
          </label>
          <span className="ws-id">{current.id}</span>
          <span className="top-bar-gap" />
          {grant && (
            <>
              <span className="who">
                <span className="who-name">{grant.identity}</span>
                <span className={`tag scope-${grant.scope}`}>{t(`scope.${grant.scope}`)}</span>
              </span>
              <button type="button" className="btn btn-icon" onClick={signOut} aria-label={t("signOut")} title={t("signOut")}>
                <Icon name="logout" />
              </button>
            </>
          )}
        </header>
        {signOutError && <ErrorNote error={signOutError} />}
        <main className="view-container" id="main" tabIndex={-1}>
          {!context ? (
            <SignIn workspace={current} onSignedIn={onSession} />
          ) : (
            <Current.Provider value={context}>
              <IntentBar />
              <header className="view-header">
                <div>
                  <h1 className="view-title">{t(active.label)}</h1>
                  <p className="view-subtitle">{t(active.subtitle)}</p>
                </div>
                {!grant.write_enabled && (
                  <p className="readonly">
                    <Icon name="lock" size={14} />
                    {grant.scope === "read" ? t("readonly", { scope: t(`scope.${grant.scope}`) }) : t("writesOff")}
                  </p>
                )}
              </header>
              {active.id === "overview" && <Overview />}
              {active.id === "runs" && <Runs />}
              {active.id === "metrics" && <Metrics />}
              {active.id === "catalogs" && <Catalogs kind={sub && (KINDS as readonly string[]).includes(sub) ? sub : "providers"} />}
              {active.id === "projects" && <Projects />}
              {active.id === "coach" && <Coach />}
              {active.id === "knowledge" && <Knowledge />}
            </Current.Provider>
          )}
        </main>
      </div>
    </div>
  );
}

export function App() {
  const [lang, setLang] = useState<Lang>(initialLang);
  const [theme, setTheme] = useState<Theme>(storedTheme);
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
    document.documentElement.setAttribute("data-theme", theme);
  }, [theme]);

  const toggleTheme = () => {
    const next: Theme = theme === "dark" ? "light" : "dark";
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
        <main className="signin" id="main"><div className="signin-card"><ErrorNote error={error} onRetry={refresh} /></div></main>
      ) : !workspaces || !session || !current ? (
        <main className="signin" id="main"><div className="signin-card"><Skeleton rows={4} /></div></main>
      ) : (
        <Shell workspaces={workspaces} session={session} current={current} view={view} sub={sub} lang={lang} theme={theme}
          onLang={toggleLang} onTheme={toggleTheme} onSession={refresh} go={go} />
      )}
    </I18n.Provider>
  );
}
