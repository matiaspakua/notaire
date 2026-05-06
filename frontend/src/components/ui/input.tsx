import * as React from "react";
import { cn } from "@/lib/utils";

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, type, ...props }, ref) => (
    <input
      type={type}
      className={cn(
        "flex h-12 w-full rounded-2xl border border-[#d2d2d7] bg-white px-4 py-2.5 text-base transition-all duration-300 file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-[#86868b]/60 apple-focus disabled:cursor-not-allowed disabled:opacity-50 hover:border-[#86868b]/40",
        className
      )}
      ref={ref}
      {...props}
    />
  )
);
Input.displayName = "Input";

export { Input };
