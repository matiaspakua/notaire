"use client";

import * as React from "react";
import { ChevronDown } from "lucide-react";
import { cn } from "@/lib/utils";

interface SelectProps {
  value?: string;
  onValueChange?: (value: string) => void;
  children?: React.ReactNode;
}

interface SelectContextValue {
  value?: string;
  onValueChange?: (value: string) => void;
  open: boolean;
  setOpen: (open: boolean) => void;
}

const SelectContext = React.createContext<SelectContextValue>({
  open: false,
  setOpen: () => {},
});

function Select({ value, onValueChange, children }: SelectProps) {
  const [open, setOpen] = React.useState(false);
  return (
    <SelectContext.Provider value={{ value, onValueChange, open, setOpen }}>
      <div className="relative">{children}</div>
    </SelectContext.Provider>
  );
}

interface SelectTriggerProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children?: React.ReactNode;
}

function SelectTrigger({ className, children, ...props }: SelectTriggerProps) {
  const { open, setOpen } = React.useContext(SelectContext);
  return (
    <button
      type="button"
      role="combobox"
      aria-haspopup="listbox"
      aria-expanded={open}
      onClick={() => setOpen(!open)}
      className={cn(
        "flex h-12 w-full items-center justify-between rounded-[12px] border border-[hsl(var(--border))] bg-white px-4 py-2.5 text-base shadow-sm transition-all duration-200 placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring apple-focus disabled:cursor-not-allowed disabled:opacity-50",
        className
      )}
      {...props}
    >
      {children}
      <ChevronDown className="h-4 w-4 opacity-50" />
    </button>
  );
}

function SelectValue({ placeholder }: { placeholder?: string }) {
  const { value } = React.useContext(SelectContext);
  return <span>{value ?? placeholder ?? ""}</span>;
}

interface SelectContentProps {
  children?: React.ReactNode;
  className?: string;
}

function SelectContent({ children, className }: SelectContentProps) {
  const { open, setOpen } = React.useContext(SelectContext);
  if (!open) return null;
  return (
    <>
      {/* Backdrop */}
      <div className="fixed inset-0 z-40" onClick={() => setOpen(false)} />
      <div
        className={cn(
          "absolute z-50 mt-1 min-w-full overflow-hidden rounded-[16px] border border-[hsl(var(--border))] bg-white text-foreground apple-shadow",
          className
        )}
      >
        <div className="p-1">{children}</div>
      </div>
    </>
  );
}

interface SelectItemProps extends React.HTMLAttributes<HTMLDivElement> {
  value: string;
  children?: React.ReactNode;
}

function SelectItem({ value, children, className, ...props }: SelectItemProps) {
  const { onValueChange, value: selected, setOpen } = React.useContext(SelectContext);
  const isSelected = selected === value;
  return (
    <div
      role="option"
      aria-selected={isSelected}
      onClick={() => {
        onValueChange?.(value);
        setOpen(false);
      }}
      className={cn(
        "relative flex cursor-pointer select-none items-center rounded-[8px] px-3 py-2 text-sm outline-none transition-colors duration-150 hover:bg-accent hover:text-accent-foreground focus-visible:ring-2 focus-visible:ring-ring",
        isSelected && "bg-accent font-medium",
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
}

export { Select, SelectTrigger, SelectValue, SelectContent, SelectItem };
