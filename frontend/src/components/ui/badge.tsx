import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const badgeVariants = cva(
  "inline-flex items-center rounded-lg border px-2.5 py-0.5 text-xs font-semibold transition-colors",
  {
    variants: {
      variant: {
        default: "border-transparent bg-[#0071e3] text-white",
        secondary: "border-transparent bg-[#F5F5F7] text-[#1d1d1f]",
        destructive: "border-transparent bg-red-100 text-red-600",
        outline: "text-[#1d1d1f] border-border",
        success: "border-transparent bg-green-100 text-green-700",
        warning: "border-transparent bg-orange-100 text-orange-700",
        info: "border-transparent bg-blue-50 text-blue-600",
      },
    },
    defaultVariants: { variant: "default" },
  }
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, ...props }: BadgeProps) {
  return <div className={cn(badgeVariants({ variant }), className)} {...props} />;
}

export { Badge, badgeVariants };
