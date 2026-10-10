import * as React from "react";
import { cn } from "@/lib/utils";
import { useFormFieldAria } from "@/theme/form-field-context";

export type InputProps = React.InputHTMLAttributes<HTMLInputElement>;

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, type, ...rest }, ref) => {
    const props = useFormFieldAria(rest);
    return (
    <input
      type={type}
      className={cn(
        "flex h-12 w-full rounded-[12px] border border-[hsl(var(--border))] bg-white px-4 py-2.5 text-base transition-[color,background-color,border-color,box-shadow] duration-fast ease-standard file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-[hsl(var(--muted-foreground))] apple-focus disabled:cursor-not-allowed disabled:opacity-50 hover:border-[hsl(var(--ring)/0.4)]",
        className
      )}
      ref={ref}
      {...props}
    />
    );
  }
);
Input.displayName = "Input";

export { Input };
