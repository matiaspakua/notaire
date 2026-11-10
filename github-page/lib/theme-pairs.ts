/** Pairs the dark theme must keep at WCAG AA. The hex values also appear in globals.css. */

export function contrastRatio(fg: string, bg: string): number {
  const channel = (hex: string) => {
    const n = hex.replace("#", "");
    return [0, 2, 4].map((i) => {
      const v = parseInt(n.slice(i, i + 2), 16) / 255;
      return v <= 0.03928 ? v / 12.92 : ((v + 0.055) / 1.055) ** 2.4;
    });
  };
  const luminance = (hex: string) => {
    const [r, g, b] = channel(hex);
    return 0.2126 * r + 0.7152 * g + 0.0722 * b;
  };
  const a = luminance(fg);
  const b = luminance(bg);
  const [hi, lo] = a > b ? [a, b] : [b, a];
  return (hi + 0.05) / (lo + 0.05);
}

export const DARK_PAIRS: { name: string; fg: string; bg: string }[] = [
  { name: "ink on card", fg: "#f8fafc", bg: "#0f172a" },
  { name: "secondary on card", fg: "#cbd5e1", bg: "#0f172a" },
  { name: "muted on canvas", fg: "#94a3b8", bg: "#0b1220" },
  { name: "body on canvas", fg: "#e2e8f0", bg: "#0b1220" },
  { name: "pill", fg: "#e2e8f0", bg: "#334155" },
  { name: "brand text", fg: "#9ecbff", bg: "#0f172a" },
  { name: "factory text", fg: "#ddd6fe", bg: "#0f172a" },
  { name: "amber chip", fg: "#fde68a", bg: "#3b2a0a" },
  { name: "emerald chip", fg: "#a7f3d0", bg: "#064e3b" },
  { name: "rose chip", fg: "#fecdd3", bg: "#4c0519" },
  { name: "white on brand button", fg: "#ffffff", bg: "#0058b0" },
  { name: "white on factory button", fg: "#ffffff", bg: "#6d28d9" },
  { name: "white on brand hover", fg: "#ffffff", bg: "#00468c" },
];
