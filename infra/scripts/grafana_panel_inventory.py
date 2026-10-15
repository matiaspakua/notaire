"""Shared inventory of Grafana panel PromQL checks for #1735 / CU77.

Wiring tests and the live verify script import this module so dashboard JSON
and runtime asserts stay aligned.
"""
from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class PanelCheck:
    dashboard_uid: str
    label: str
    expr: str
    # When True, an empty Prometheus result is SKIP (optional services).
    optional: bool = False


# Key panels that must show useful series after traffic (Mac #1740 + #1746).
# PromQL must appear verbatim in the matching provisioned dashboard JSON.
PANEL_CHECKS: tuple[PanelCheck, ...] = (
    PanelCheck(
        "notaire-status",
        "Backend probe",
        'max(probe_success{service="backend"})',
    ),
    PanelCheck(
        "notaire-status",
        "Product version",
        'max by (version) (jvm_memory_used_bytes{application="notaire-backend", area="heap", version!=""})',
    ),
    PanelCheck(
        "notaire-postgres",
        "Active connections",
        "pg_stat_database_numbackends{datname='notaire'}",
    ),
    PanelCheck(
        "notaire-postgres",
        "Connections by state",
        "pg_connections_total",
    ),
    PanelCheck(
        "notaire-postgres",
        "Cache hit ratio",
        "(sum(pg_stat_database_blks_hit{datname='notaire'}) / clamp_min(sum(pg_stat_database_blks_hit{datname='notaire'}) + sum(pg_stat_database_blks_read{datname='notaire'}), 1)) * 100",
    ),
    PanelCheck(
        "notaire-backend",
        "HTTP request rate",
        "rate(http_server_requests_seconds_count{application='notaire-backend'}[1m])",
    ),
    PanelCheck(
        "notaire-backend",
        "JVM heap",
        "jvm_memory_used_bytes{application='notaire-backend', area='heap'}",
    ),
    PanelCheck(
        "notaire-quality",
        "Product version",
        'max by (version) (jvm_memory_used_bytes{application="notaire-backend", area="heap", version!=""})',
    ),
    PanelCheck(
        "notaire-quality",
        "SonarQube probe",
        'max(probe_success{service="sonarqube"}) or max(up{job="probe-sonarqube"}) or vector(0)',
        optional=True,
    ),
)

REQUIRED_DASHBOARD_UIDS = frozenset(c.dashboard_uid for c in PANEL_CHECKS)
