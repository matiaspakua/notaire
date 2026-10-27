"use client";

import { PageHeader } from "@/components/shared/PageHeader";

interface AppHeaderProps {
  title?: string;
  description?: string;
  actions?: React.ReactNode;
}

/** Sticky glass chrome around PageHeader (#1942). */
export function AppHeader({ title, description, actions }: AppHeaderProps) {
  if (!title) {
    return (
      <header className="sticky top-0 z-20 glass border-b border-border/40 px-6 lg:px-10 py-5">
        <div className="max-w-[1600px] mx-auto" />
      </header>
    );
  }

  return (
    <header className="sticky top-0 z-20 glass border-b border-border/40 px-6 lg:px-10 py-5">
      <PageHeader
        title={title}
        description={description}
        actions={actions}
        className="max-w-[1600px] mx-auto"
      />
    </header>
  );
}
