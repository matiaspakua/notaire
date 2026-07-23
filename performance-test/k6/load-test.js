import http from 'k6/http';
import { check, sleep } from 'k6';

// Minimal load test for the highest-traffic read endpoints (issue #594).
// Run manually with:
//   k6 run -e BASE_URL=http://localhost:8080 \
//           -e ADMIN_USER=admin -e ADMIN_PASSWORD=admin \
//           performance-test/k6/load-test.js
// Wired into .github/workflows/performance-test.yml as a scheduled (not per-PR) CI job.

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ADMIN_USER = __ENV.ADMIN_USER || 'admin';
const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || 'admin';

export const options = {
  stages: [
    { duration: '30s', target: 10 },  // Ramp up to 10 users
    { duration: '1m', target: 10 },   // Stay at 10 users
    { duration: '30s', target: 0 },   // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

export function setup() {
  const loginRes = http.post(
      `${BASE_URL}/api/v1/usuarios/login`,
      JSON.stringify({ nombre: ADMIN_USER, contrasenia: ADMIN_PASSWORD }),
      { headers: { 'Content-Type': 'application/json' } },
  );

  check(loginRes, {
    'login succeeded': (r) => r.status === 200 && r.json('token') !== undefined,
  });

  return { token: loginRes.json('token') };
}

export default function (data) {
  const params = {
    headers: { Authorization: `Bearer ${data.token}` },
  };

  const endpoints = [
    { name: 'gestiones', url: `${BASE_URL}/api/v1/gestiones` },
    { name: 'presupuestos', url: `${BASE_URL}/api/v1/presupuestos` },
    { name: 'tramites', url: `${BASE_URL}/api/v1/tramites` },
  ];

  for (const endpoint of endpoints) {
    const res = http.get(endpoint.url, params);
    check(res, {
      [`${endpoint.name}: status is 200`]: (r) => r.status === 200,
    });
  }

  sleep(1);
}
