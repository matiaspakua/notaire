"use client";

interface AppHeaderProps {
  title?: string;
  description?: string;
  actions?: React.ReactNode;
}

export function AppHeader({ title, description, actions }: AppHeaderProps) {
  return (
    <header className="sticky top-0 z-20 glass border-b border-border/40 px-6 lg:px-10 py-5">
      <div className="flex items-center justify-between max-w-[1600px] mx-auto">
        <div className="space-y-0.5">
          <h1 className="text-2xl font-semibold tracking-tight text-foreground">{title}</h1>
          {description && <p className="text-sm text-muted-foreground font-medium">{description}</p>}
        </div>
        {actions && <div className="flex items-center gap-3">{actions}</div>}
      </div>
    </header>
  );
}
