import { buildApp } from "./app.ts";
import { ConfigError, loadConfig } from "./config.ts";

try {
  const config = loadConfig();
  const app = buildApp({ config });
  await app.listen({ host: config.host, port: config.port });
  const names = config.workspaces.map((ws) => ws.label).join(", ");
  console.log(`Factory Control on http://${config.host}:${config.port} (workspaces: ${names})`);
} catch (error) {
  if (error instanceof ConfigError) {
    console.error(`factory-dashboard: ${error.message}`);
    process.exit(2);
  }
  throw error;
}
