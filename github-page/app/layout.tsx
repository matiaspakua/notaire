import type { Metadata, Viewport } from "next";
import "@fontsource-variable/fraunces";
import "@fontsource-variable/jetbrains-mono";
import "./globals.css";
import { SiteHeader } from "@/components/site/SiteHeader";
import { SiteFooter } from "@/components/site/SiteFooter";
import { loadReleaseHistory } from "@/lib/releases";

export const metadata: Metadata = {
  title: {
    default: "Notaire — the product, its docs, and the AI factory that builds it",
    template: "%s · Notaire",
  },
  description:
    "From a 2014 Java Swing university project to a modern notarial management system, built by an AI-assisted software factory. The story, the technical docs and the AI SDLC.",
};

export const viewport: Viewport = { themeColor: "#ffffff", width: "device-width", initialScale: 1 };

export default function RootLayout({ children }: { children: React.ReactNode }) {
  const { productVersion } = loadReleaseHistory();
  return (
    <html lang="en" suppressHydrationWarning>
      <body className="min-h-screen bg-white text-slate-900">
        <a href="#main" className="skip-link">
          Skip to content
        </a>
        <SiteHeader />
        <main id="main" tabIndex={-1} className="focus:outline-none">
          {children}
        </main>
        <SiteFooter version={productVersion} />
      </body>
    </html>
  );
}
