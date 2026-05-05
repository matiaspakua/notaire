"use client";

interface AppHeaderProps {
  title?: string;
  description?: string;
  actions?: React.ReactNode;
}

export function AppHeader({ title, description, actions }: AppHeaderProps) {
  return (
    <header className="sticky top-0 z-10 glass border-b border-border/50 px-6 lg:px-8 py-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold tracking-tight text-foreground">{title}</h1>
          {description && <p className="text-sm text-muted-foreground mt-0.5">{description}</p>}
        </div>
        {actions && <div className="flex items-center gap-2">{actions}</div>}
      </div>
    </header>
  );
}
