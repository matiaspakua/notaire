/**
 * Public security story for the portfolio (#2003). Demo labels only.
 * The dated evidence is docs/200-architecture/206-security/ASSESSMENT-2026-11-05.md.
 */
import cadenceFile from "./security-cadence.json";

export type CadenceTrigger = "release" | "release-schedule" | "schedule" | "main" | "local" | "platform";

export type CadenceEntry = {
  id: string;
  tool: string;
  family: string;
  where: string;
  trigger: CadenceTrigger;
  workflow: string | null;
  local: string | null;
};

export const CADENCE = cadenceFile as {
  assessed: string;
  disclaimer: string;
  entries: CadenceEntry[];
};

export const TRIGGER_LABEL: Record<CadenceTrigger, string> = {
  release: "Version tag",
  "release-schedule": "Tag and weekly",
  schedule: "Weekly",
  main: "Push to main",
  local: "Local only",
  platform: "GitHub app",
};

export type StrideItem = { id: string; status: "Mitigated" | "Partial" | "Open" | "Accepted"; summary: string };

export const STRIDE: { letter: string; name: string; question: string; items: StrideItem[] }[] = [
  {
    letter: "S",
    name: "Spoofing",
    question: "Can someone pretend to be a staff member or an API client?",
    items: [
      { id: "SR-12", status: "Partial", summary: "The browser session is an HttpOnly cookie. API clients still receive a bearer token in the login JSON." },
      { id: "AT-1", status: "Partial", summary: "Passwords are BCrypt and login lockout is shared in the database. There is no second factor." },
    ],
  },
  {
    letter: "T",
    name: "Tampering",
    question: "Can someone change a record, a query, or a published image?",
    items: [
      { id: "AT-3", status: "Mitigated", summary: "Persistence uses parameterized JPA. The SQL injection note names the query paths." },
      { id: "SR-13", status: "Partial", summary: "Trivy fails the release job on fixed critical and high findings. That job does not run on a pull request." },
    ],
  },
  {
    letter: "R",
    name: "Repudiation",
    question: "Can someone deny a write, or a read of personal data?",
    items: [
      { id: "SR-07", status: "Mitigated", summary: "Writes are audited from the authenticated identity, not from a client header." },
      { id: "SR-08", status: "Mitigated", summary: "Reads of Person are audited. Other entities opt in." },
    ],
  },
  {
    letter: "I",
    name: "Information disclosure",
    question: "Can personal data or a secret leave the boundary?",
    items: [
      { id: "SR-14", status: "Partial", summary: "A high-confidence secret scan fails the local gate and the release job." },
      { id: "LD-3", status: "Partial", summary: "Request logs keep method, path, status, and duration. They omit query, headers, and body." },
    ],
  },
  {
    letter: "D",
    name: "Denial of service",
    question: "Can an anonymous caller exhaust the API?",
    items: [
      { id: "SR-15", status: "Open", summary: "Only login attempts are throttled. A general quota is still an open requirement." },
    ],
  },
  {
    letter: "E",
    name: "Elevation of privilege",
    question: "Can a signed-in user reach a module or a role they were not granted?",
    items: [
      { id: "SR-16", status: "Mitigated", summary: "A notary (Escribano) receives ROLE_NOTARY, not administrator." },
      { id: "SR-11", status: "Accepted", summary: "Module grants are enforced on the API. Inside one office, staff share the files. A second notary is a second installation." },
    ],
  },
];

export const LINDDUN: { id: string; name: string; status: StrideItem["status"]; summary: string }[] = [
  { id: "LD-1", name: "Linkability is not the issue — identifiability is", status: "Open", summary: "An authenticated caller still receives DNI, email, and name. Field minimization is not built." },
  { id: "LD-2", name: "Linkability", status: "Accepted", summary: "The same person key joins management, deed, and budget. The notarial record needs that." },
  { id: "LD-3", name: "Disclosure", status: "Partial", summary: "Logs skip the body. A path can still contain a record id." },
  { id: "LD-4", name: "Unawareness", status: "Open", summary: "There is no in-product privacy notice." },
  { id: "LD-5", name: "Non-compliance", status: "Open", summary: "Retention of notarial records is indefinite by the architecture note. A statute reading belongs to counsel." },
  { id: "LD-6", name: "Detectability", status: "Partial", summary: "Login failures are a metric. Reads of personal data are an audit row, not a dashboard series." },
];

export const POSTURE: { id: string; title: string; status: StrideItem["status"]; summary: string }[] = [
  { id: "identity", title: "Identity", status: "Partial", summary: "BCrypt, shared login lockout, signed JWT, HttpOnly cookie. No second factor." },
  { id: "grants", title: "Module grants", status: "Mitigated", summary: "After the JWT check, the API denies a path whose module the role does not hold." },
  { id: "office", title: "Inside one office", status: "Accepted", summary: "Staff in the same installation can open the office files. That is the tenancy decision, not a missing check." },
  { id: "supply", title: "Dependencies and images", status: "Partial", summary: "Trivy fails the release job. GitHub does not run that job on the pull request. The local gate does." },
  { id: "dast", title: "Dynamic scan", status: "Partial", summary: "A weekly ZAP baseline fails on alerts outside the allowlist. The version tag does not run it yet." },
  { id: "quota", title: "API quota", status: "Open", summary: "Expensive endpoints have no general limit. Login attempts do." },
];

export const STATUS_STYLE: Record<StrideItem["status"], string> = {
  Mitigated: "bg-emerald-100 text-emerald-900",
  Partial: "bg-amber-100 text-amber-950",
  Open: "bg-rose-100 text-rose-900",
  Accepted: "bg-slate-200 text-slate-800",
};
