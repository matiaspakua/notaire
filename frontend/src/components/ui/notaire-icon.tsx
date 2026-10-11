import Image from "next/image";
import { cn } from "@/lib/utils";

interface NotaireIconProps {
  src: string;
  alt: string;
  size?: number;
  className?: string;
}

export function NotaireIcon({ src, alt, size = 24, className }: NotaireIconProps) {
  if (!src) {
    return null;
  }
  return (
    <Image
      src={src}
      alt={alt}
      width={size}
      height={size}
      unoptimized
      // shrink-0 + max-w-none: preflight's img max-width:100% let a flex button in a
      // narrow column shrink to its padding and draw the icon 0px wide (#1525).
      className={cn("shrink-0 max-w-none object-contain", className)}
    />
  );
}
