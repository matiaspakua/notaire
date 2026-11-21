"""Recursive folder scan: honours .gitignore in git repos, include/exclude globs and size limits."""

from __future__ import annotations

import fnmatch
import functools
import hashlib
import os
import stat
import subprocess
from dataclasses import dataclass
from pathlib import Path

from .config import Config, Source

CODE_LANGS = {
    ".java": "java", ".ts": "typescript", ".mts": "typescript", ".cts": "typescript", ".tsx": "tsx",
    ".js": "javascript", ".mjs": "javascript", ".cjs": "javascript", ".jsx": "javascript",
    ".py": "python", ".go": "go", ".rs": "rust", ".kt": "kotlin", ".kts": "kotlin", ".cs": "c_sharp",
    ".rb": "ruby", ".php": "php", ".scala": "scala", ".swift": "swift", ".c": "c", ".h": "c",
    ".cc": "cpp", ".cpp": "cpp", ".hpp": "cpp", ".cxx": "cpp", ".sh": "bash", ".bash": "bash",
    ".lua": "lua",
}
FAMILIES = {
    "markdown": {".md", ".mdx", ".markdown"},
    "sql": {".sql"},
    "structured": {".json", ".yaml", ".yml", ".toml", ".xml", ".properties", ".ini", ".cfg", ".conf"},
    "tabular": {".csv", ".tsv"},
    "pdf": {".pdf"},
    "binary": {
        ".png", ".jpg", ".jpeg", ".gif", ".ico", ".webp", ".bmp", ".svgz", ".jar", ".war", ".zip",
        ".gz", ".tgz", ".bz2", ".xz", ".7z", ".class", ".so", ".dylib", ".dll", ".exe", ".bin",
        ".woff", ".woff2", ".ttf", ".otf", ".eot", ".mp3", ".mp4", ".mov", ".avi", ".webm", ".wav",
        ".jasper", ".pyc", ".db", ".sqlite", ".parquet", ".onnx", ".pkl", ".npy", ".npz",
        ".docx", ".xlsx", ".pptx", ".odt",
    },
}
SECRET_GLOBS = ["**/.env", "**/.env.*", "**/*.pem", "**/*.key", "**/*.p12", "**/*.jks", "**/id_rsa*"]
SECRET_ALLOW = (".env.example", ".env.sample", ".env.template")


@dataclass(slots=True)
class ScannedFile:
    path: str
    lang: str
    family: str
    size: int
    mtime: int
    digest: str


def detect(path: str) -> tuple[str, str]:
    """Return (lang, family) for a relative path."""
    name = path.rsplit("/", 1)[-1].lower()
    ext = os.path.splitext(name)[1]
    if ext in CODE_LANGS:
        return CODE_LANGS[ext], "code"
    for family, exts in FAMILIES.items():
        if ext in exts:
            return (ext.lstrip(".") or family), family
    if name in ("dockerfile", "makefile", "jenkinsfile", "procfile") or name.startswith("dockerfile."):
        return name.split(".")[0], "text"
    lang = _pack_language(name) if ext not in PLAIN_TEXT else None
    if lang:
        return lang, "code" if _analyzable(lang) else "text"
    return (ext.lstrip(".") or "text"), "text"


# Generic extensions the pack maps to a niche language (`.txt` -> vimdoc, `.inc` -> sourcepawn): plain text.
PLAIN_TEXT = frozenset({".txt", ".text", ".inc", ".log"})
KNOWN_EXTENSIONS = frozenset(CODE_LANGS) | frozenset().union(*FAMILIES.values()) | {
    ".txt", ".html", ".htm", ".css", ".scss", ".less", ".svg", ".sh", ".bat", ".cmd", ".ps1", ".env", ".lock",
    ".gradle", ".tf", ".puml", ".bru", ".adoc", ".rst", ".log", ".vue", ".svelte", ".mdx", ".ipynb", ".j2",
}


@functools.lru_cache(maxsize=65536)
def is_file_name(name: str) -> bool:
    """A name that reads as a file: a known extension, or one the tree-sitter pack recognizes."""
    ext = os.path.splitext(name.lower())[1]
    return ext in KNOWN_EXTENSIONS or _pack_language(name.lower()) is not None


@functools.lru_cache(maxsize=4096)
def _pack_language(name: str) -> str | None:
    """The tree-sitter pack's language for a file name (371 grammars in pack 1.14), or None."""
    try:
        from tree_sitter_language_pack import detect_language_from_path

        return detect_language_from_path(name)
    except Exception:  # noqa: BLE001 - no pack: the extension is the language
        return None


def _analyzable(lang: str) -> bool:
    from .extractors.code import analyzable

    return analyzable(lang)


def _matches(path: str, patterns: list[str]) -> bool:
    slashed = "/" + path
    return any(fnmatch.fnmatch(path, p) or fnmatch.fnmatch(slashed, p) for p in patterns)


def _is_secret(path: str) -> bool:
    if path.endswith(SECRET_ALLOW):
        return False
    return _matches(path, SECRET_GLOBS)


def _git_files(root: Path) -> list[str] | None:
    try:
        out = subprocess.run(
            ["git", "-C", str(root), "ls-files", "-co", "--exclude-standard", "-z"],
            capture_output=True, check=True, timeout=120,
        ).stdout
    except (OSError, subprocess.SubprocessError):
        return None
    return [p for p in out.decode("utf-8", "surrogateescape").split("\0") if p]


def _walk_files(root: Path, excludes: list[str]) -> list[str]:
    files: list[str] = []
    for dirpath, dirnames, filenames in os.walk(root):
        rel_dir = os.path.relpath(dirpath, root)
        rel_dir = "" if rel_dir == "." else rel_dir.replace(os.sep, "/") + "/"
        dirnames[:] = [d for d in dirnames if not _matches(f"{rel_dir}{d}/x", excludes)]
        files.extend(f"{rel_dir}{f}" for f in filenames)
    return files


def is_git_repo(root: Path) -> bool:
    return (root / ".git").exists()


def list_files(source: Source, cfg: Config) -> list[str]:
    root = source.path
    excludes = cfg.exclude + source.exclude
    candidates = _git_files(root) if is_git_repo(root) else None
    if candidates is None:
        candidates = _walk_files(root, excludes)
    out = []
    for rel in candidates:
        if _matches(rel, excludes) or _is_secret(rel):
            continue
        if source.include and not _matches(rel, source.include):
            continue
        out.append(rel)
    return sorted(out)


def scan(source: Source, cfg: Config, known: dict[str, tuple[str, int, int]] | None = None) -> list[ScannedFile]:
    """List files with their content digest. When `known` holds a file's (hash, size, mtime) and the stat
    signature still matches, the stored hash is reused and the file is not read."""
    root = source.path
    known = known or {}
    result: list[ScannedFile] = []
    for rel in list_files(source, cfg):
        full = root / rel
        try:
            if full.is_symlink():
                continue
            st = full.stat()
        except OSError:
            continue
        if not stat.S_ISREG(st.st_mode):
            continue
        lang, family = detect(rel)
        limit = cfg.max_pdf_bytes if family == "pdf" else cfg.max_file_bytes
        if st.st_size > limit:
            family = "binary"
        prev = known.get(rel)
        if prev is not None and prev[1] == st.st_size and prev[2] == st.st_mtime_ns:
            digest = prev[0]
        else:
            digest = _digest(full) if family != "binary" else f"size:{st.st_size}"
        result.append(ScannedFile(rel, lang, family, st.st_size, st.st_mtime_ns, digest))
    return result


def _digest(path: Path) -> str:
    h = hashlib.blake2b(digest_size=16)
    with open(path, "rb") as fh:
        for block in iter(lambda: fh.read(1 << 20), b""):
            h.update(block)
    return h.hexdigest()
