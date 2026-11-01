import { Architecture } from "@/components/Architecture";
import { InfraStack } from "@/components/InfraStack";
import { pageMetadata } from "@/lib/site";
import { services, versions } from "@/lib/tech-facts";

export const metadata = pageMetadata("/product/platform/");

export default function ProductPlatformPage() {
  const s = services();
  return (
    <>
      <Architecture versions={versions()} />
      <InfraStack serviceCount={s.app.length + s.observability.length} />
    </>
  );
}
