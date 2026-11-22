/** One authored icon set: 20px grid, 1.5px stroke, round joins. */
const PATHS = {
  overview: "M3 10h3l2-5 4 10 2-5h3",
  runs: "M10 17.5a7.5 7.5 0 1 0 0-15 7.5 7.5 0 0 0 0 15ZM8.5 7l4 3-4 3V7Z",
  projects: "M2.5 6.5v8a1.5 1.5 0 0 0 1.5 1.5h12a1.5 1.5 0 0 0 1.5-1.5V8a1.5 1.5 0 0 0-1.5-1.5H10L8.5 4.5H4a1.5 1.5 0 0 0-1.5 1.5Z",
  catalogs: "M10 2.5 2.5 6.25 10 10l7.5-3.75L10 2.5ZM2.5 10 10 13.75 17.5 10M2.5 13.75 10 17.5l7.5-3.75",
  knowledge: "M5 6a2 2 0 1 0 0-4 2 2 0 0 0 0 4Zm10 5a2 2 0 1 0 0-4 2 2 0 0 0 0 4ZM6 18a2 2 0 1 0 0-4 2 2 0 0 0 0 4ZM6.7 5.1l6.6 2.8M13.4 10.6l-6 4.2M5 6l.8 8",
  coach: "M10 17.5a7.5 7.5 0 1 0 0-15 7.5 7.5 0 0 0 0 15Zm2.8-10.3-1.6 4-4 1.6 1.6-4 4-1.6Z",
  check: "m4.5 10.5 3.5 3.5 7.5-8",
  cross: "m5.5 5.5 9 9m0-9-9 9",
  alert: "M10 3 2.5 16.5h15L10 3Zm0 5v3.5m0 2.5v.01",
  clock: "M10 17.5a7.5 7.5 0 1 0 0-15 7.5 7.5 0 0 0 0 15ZM10 6v4l2.5 2.5",
  pause: "M10 17.5a7.5 7.5 0 1 0 0-15 7.5 7.5 0 0 0 0 15ZM8 7.5v5m4-5v5",
  lock: "M5.5 9V6.5a4.5 4.5 0 0 1 9 0V9M4.5 9h11v8h-11V9Z",
  sun: "M10 13.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7ZM10 1.5v2m0 13v2M1.5 10h2m13 0h2M4 4l1.4 1.4m9.2 9.2L16 16M4 16l1.4-1.4m9.2-9.2L16 4",
  moon: "M16.5 12.2A7 7 0 0 1 7.8 3.5a7 7 0 1 0 8.7 8.7Z",
  globe: "M10 17.5a7.5 7.5 0 1 0 0-15 7.5 7.5 0 0 0 0 15ZM2.5 10h15M10 2.5c2 2 3 4.6 3 7.5s-1 5.5-3 7.5c-2-2-3-4.6-3-7.5s1-5.5 3-7.5Z",
  logout: "M8 17H4.5A1.5 1.5 0 0 1 3 15.5v-11A1.5 1.5 0 0 1 4.5 3H8m5 10.5L16.5 10 13 6.5M16.5 10H7.5",
  plus: "M10 4v12M4 10h12",
  refresh: "M16.5 10a6.5 6.5 0 1 1-1.9-4.6M16.5 3.5v3.5H13",
  key: "M12.5 10.5a4 4 0 1 0-3.8-2.7L3 13.5V17h3.5v-2h2v-2h2l.7-.7a4 4 0 0 0 1.3.2ZM13.5 6.5h.01",
  search: "M9 15.5a6.5 6.5 0 1 0 0-13 6.5 6.5 0 0 0 0 13ZM17.5 17.5l-3.8-3.8",
  minus: "M5 10h10",
  dot: "M10 12a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z",
  grid: "M3 3h4v4H3zM8 3h4v4H8zM13 3h4v4h-4zM3 8h4v4H3zM8 8h4v4H8zM13 8h4v4h-4zM3 13h4v4H3zM8 13h4v4H8zM13 13h4v4h-4z",
  metrics: "M2.5 14.5 7 9l3.5 3 7-7.5",
  config: "M4 5.5h12M4 10h12M4 14.5h12M7 4v3M13 8.5v3M9 13v3",
  user: "M10 10a3 3 0 1 0 0-6 3 3 0 0 0 0 6Zm-6 7.5a6 6 0 0 1 12 0",
  inbox: "M2.5 11h4l1.5 2.5h4l1.5-2.5h4M4.5 4h11l2 7v4.5a1.5 1.5 0 0 1-1.5 1.5H4a1.5 1.5 0 0 1-1.5-1.5V11l2-7Z",
  agent: "M10 17.5a7.5 7.5 0 1 0 0-15 7.5 7.5 0 0 0 0 15Zm-2-5.5a2 2 0 1 1 4 0M7.5 14c.8-1.2 2.2-2 2.5-2s1.7.8 2.5 2",
  intent: "M10 2.5 2.5 6.25 10 10l7.5-3.75L10 2.5ZM2.5 10 10 13.75 17.5 10M2.5 13.75 10 17.5l7.5-3.75",
  close: "m5.5 5.5 9 9m0-9-9 9",
} as const;

export type IconName = keyof typeof PATHS;

export function Icon({ name, size = 18, label }: { name: IconName; size?: number; label?: string }) {
  return (
    <svg
      className="icon"
      width={size}
      height={size}
      viewBox="0 0 20 20"
      fill="none"
      stroke="currentColor"
      strokeWidth={1.5}
      strokeLinecap="round"
      strokeLinejoin="round"
      role={label ? "img" : undefined}
      aria-label={label}
      aria-hidden={label ? undefined : true}
      focusable="false"
    >
      <path d={PATHS[name]} />
    </svg>
  );
}
