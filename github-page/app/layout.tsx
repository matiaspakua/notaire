import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Notaire — A 10-Year Modernization Journey",
  description:
    "From a 2014 Java Swing university project to a modern AI-powered notarial management system. The story of Notaire.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body>{children}</body>
    </html>
  );
}
