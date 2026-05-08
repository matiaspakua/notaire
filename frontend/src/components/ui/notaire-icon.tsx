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
      className={cn("object-contain", className)}
    />
  );
}
