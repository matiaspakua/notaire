#!/usr/bin/env bash
# Verifies the infra module only: build, test, format, lint. Run from any directory.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

python3 infra/tests/test_infra_standalone.py
python3 infra/tests/test_infra_prometheus_hardening.py
python3 infra/tests/test_staging_kustomize.py
python3 infra/tests/test_performance_test_assets.py
python3 infra/tests/test_prod_compose.py
python3 infra/tests/test_dev_stack_isolation.py
python3 infra/tests/test_sonar_analysis.py
python3 infra/tests/test_grafana_status_dashboards.py
python3 infra/tests/test_postgres_exporter_english.py
python3 infra/tests/test_otel_metrics_pipeline.py
python3 infra/tests/test_log_trace_correlation.py
python3 infra/tests/test_tempo_traces.py
python3 infra/tests/test_alertmanager.py
python3 infra/tests/test_homer_hub.py
python3 infra/tests/test_infra_dashboards.py
python3 infra/tests/test_local_tools_no_login.py
