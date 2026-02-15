#!/usr/bin/env python3
import argparse
import subprocess
import sys
import tempfile
from pathlib import Path

import pypandoc


def convert_with_pandoc(input_path: Path, output_path: Path, input_format: str) -> None:
    output = pypandoc.convert_file(
        str(input_path),
        to="gfm",                 # GitHub Flavored Markdown
        format=input_format,
        extra_args=["--wrap=none"],
    )
    output_path.write_text(output, encoding="utf-8")


def convert_doc_to_docx(doc_path: Path, tmp_dir: Path) -> Path:
    """
    Convierte un .doc a .docx usando LibreOffice en modo headless.
    Devuelve la ruta al .docx generado.
    """
    if doc_path.suffix.lower() != ".doc":
        raise ValueError("convert_doc_to_docx solo acepta archivos .doc")

    # LibreOffice generará el .docx en tmp_dir con el mismo nombre base
    cmd = [
        "libreoffice",
        "--headless",
        "--convert-to",
        "docx",
        "--outdir",
        str(tmp_dir),
        str(doc_path),
    ]
    subprocess.run(cmd, check=True)

    docx_path = tmp_dir / (doc_path.stem + ".docx")
    if not docx_path.exists():
        raise RuntimeError(f"No se generó el archivo DOCX esperado: {docx_path}")
    return docx_path


def convert_word_to_md(input_path: Path, output_path: Path) -> None:
    suffix = input_path.suffix.lower()

    if suffix == ".docx":
        # Soportado directamente por Pandoc
        convert_with_pandoc(input_path, output_path, input_format="docx")
    elif suffix == ".doc":
        # Convertir primero a .docx usando LibreOffice, luego Pandoc
        with tempfile.TemporaryDirectory() as tmp:
            tmp_dir = Path(tmp)
            docx_path = convert_doc_to_docx(input_path, tmp_dir)
            convert_with_pandoc(docx_path, output_path, input_format="docx")
    else:
        raise ValueError("El archivo de entrada debe ser .doc o .docx")


def process_directory(input_dir: Path) -> None:
    word_files = list(input_dir.glob("*.doc")) + list(input_dir.glob("*.docx"))

    if not word_files:
        print(f"No se encontraron archivos .doc ni .docx en {input_dir}")
        return

    for word_file in word_files:
        md_file = word_file.with_suffix(".md")
        try:
            print(f"Convirtiendo: {word_file.name} -> {md_file.name}")
            convert_word_to_md(word_file, md_file)
        except Exception as exc:
            print(f"[ERROR] Falló la conversión de {word_file}: {exc}", file=sys.stderr)


def process_file(input_file: Path) -> None:
    if not input_file.is_file():
        print(f"El archivo no existe: {input_file}", file=sys.stderr)
        sys.exit(1)

    if input_file.suffix.lower() not in (".doc", ".docx"):
        print("El archivo de entrada debe tener extensión .doc o .docx", file=sys.stderr)
        sys.exit(1)

    md_file = input_file.with_suffix(".md")
    print(f"Convirtiendo: {input_file.name} -> {md_file.name}")
    convert_word_to_md(input_file, md_file)


def main() -> None:
    parser = argparse.ArgumentParser(
        description=(
            "Convierte archivos de Word (.doc y .docx) que describen casos de uso "
            "a archivos Markdown (.md) con el mismo nombre."
        )
    )
    parser.add_argument(
        "path",
        help=(
            "Ruta a un archivo .doc/.docx o a un directorio que contenga archivos "
            ".doc/.docx. Ejemplo: ./casos_uso"
        ),
    )

    args = parser.parse_args()
    path = Path(args.path)

    if not path.exists():
        print(f"La ruta no existe: {path}", file=sys.stderr)
        sys.exit(1)

    if path.is_dir():
        process_directory(path)
    else:
        process_file(path)


if __name__ == "__main__":
    main()

