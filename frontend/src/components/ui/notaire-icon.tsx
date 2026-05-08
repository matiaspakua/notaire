import Image from "next/image";
import { cn } from "@/lib/utils";

interface NotaireIconProps {
  src: string;
  alt: string;
  size?: number;
  className?: string;
}

export function NotaireIcon({ src, alt, size = 24, className }: NotaireIconProps) {
  return (
    <Image
      src={src}
      alt={alt}
      width={size}
      height={size}
      className={cn("object-contain", className)}
    />
  );
}
