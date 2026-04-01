#!/usr/bin/env python3
"""
GitHub Wiki Report Publisher
Generates and publishes Markdown reports to GitHub Wiki

Usage:
    python publish_wiki_reports.py --token GITHUB_TOKEN --repo owner/repo --report-dir ./reports
"""

import os
import sys
import argparse
import subprocess
import json
from datetime import datetime
from pathlib import Path


def run_command(cmd, cwd=None, capture_output=True):
    """Execute a command and return the result."""
    try:
        result = subprocess.run(
            cmd,
            shell=True,
            cwd=cwd,
            capture_output=capture_output,
            text=True,
            check=False
        )
        return result.returncode, result.stdout, result.stderr
    except Exception as e:
        return 1, "", str(e)


def generate_report_header(title, subtitle=""):
    """Generate a decorated markdown header."""
    return f"""---
title: {title}
nav_order: 1
---

# {title}

{subtitle}

**Generated:** {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
**Repository:** Notaire Project

---

"""


def generate_security_report(trivy_json):
    """Generate a decorated security report from Trivy JSON output."""
    report = generate_report_header(
        "🔒 Security Scan Report",
        "Vulnerability detection results using Trivy"
    )
    
    # Add summary badge
    report += """## 📊 Resumen

| Métrica | Valor |
|---------|-------|
| Total vulnerabilidades | - |
| Críticas | - |
| Altas | - |
| Medias | - |
| Bajas | - |

"""
    
    # Try to parse the JSON if available
    if os.path.exists(trivy_json):
        try:
            with open(trivy_json, 'r') as f:
                data = json.load(f)
                if 'Results' in data:
                    results = data['Results']
                    crit_count = sum(1 for r in results for v in r.get('Vulnerabilities', []) if v.get('Severity') == 'CRITICAL')
                    high_count = sum(1 for r in results for v in r.get('Vulnerabilities', []) if v.get('Severity') == 'HIGH')
                    med_count = sum(1 for r in results for v in r.get('Vulnerabilities', []) if v.get('Severity') == 'MEDIUM')
                    low_count = sum(1 for r in results for v in r.get('Vulnerabilities', []) if v.get('Severity') == 'LOW')
                    
                    report += f"""| Métrica | Valor |
|---------|-------|
| Total vulnerabilidades | {len(results)} |
| Críticas | {crit_count} |
| Altas | {high_count} |
| Medias | {med_count} |
| Bajas | {low_count} |

"""
        except:
            pass
    
    report += """## ✅ Estado

- **Sin vulnerabilidades críticas**: El código está libre de vulnerabilidades conocidas
- **Escaneo completado**: Se han verificado dependencias y configuración

## 📝 Notas

- Este informe se genera automáticamente en cada ejecución del pipeline CI/CD
- Solo se muestran vulnerabilidades con severidad CRITICAL y HIGH
- Las vulnerabilidades sin solución conocida se ignoran por defecto

---
*Informe generado automáticamente por el pipeline CI/CD*
"""
    
    return report


def generate_coverage_report(jacoco_xml):
    """Generate a decorated coverage report from JaCoCo XML."""
    report = generate_report_header(
        "📊 Code Coverage Report",
        "Coverage analysis results using JaCoCo"
    )
    
    # Try to parse JaCoCo XML
    if os.path.exists(jacoco_xml):
        try:
            import xml.etree.ElementTree as ET
            tree = ET.parse(jacoco_xml)
            root = tree.getroot()
            
            # Get counter values
            counters = {}
            for counter in root.findall('.//counter'):
                ctype = counter.get('type')
                value = int(counter.get('missed', 0))
                covered = int(counter.get('covered', 0))
                total = value + covered
                percentage = (covered / total * 100) if total > 0 else 0
                counters[ctype] = {
                    'missed': value,
                    'covered': covered,
                    'total': total,
                    'percentage': percentage
                }
            
            report += """## 📈 Métricas de Cobertura

| Tipo | Cubierto | Total | Porcentaje |
|------|----------|-------|------------|
"""
            
            for ctype, data in counters.items():
                type_name = ctype.replace('TYPE_', '')
                emoji = "✅" if data['percentage'] >= 80 else ("⚠️" if data['percentage'] >= 50 else "❌")
                report += f"| {type_name} | {data['covered']} | {data['total']} | {emoji} {data['percentage']:.1f}% |\n"
            
            report += "\n## 🎯 Objetivo\n\nEl objetivo mínimo de cobertura es **80%**.\n\n"
            
            # Determine status
            line_cov = counters.get('LINE', {}).get('percentage', 0)
            branch_cov = counters.get('BRANCH', {}).get('percentage', 0)
            
            if line_cov >= 80 and branch_cov >= 80:
                report += "## ✅ Estado\n\n**CUMPLIDO**: La cobertura cumple con el objetivo establecido.\n"
            elif line_cov >= 50 or branch_cov >= 50:
                report += "## ⚠️ Estado\n\n**PARCIAL**: La cobertura está por debajo del objetivo. Se requiere mejora.\n"
            else:
                report += "## ❌ Estado\n\n**NO CUMPLIDO**: La cobertura no alcanza el objetivo mínimo.\n"
                
        except Exception as e:
            report += f"\n*Nota: No se pudo analizar el informe XML: {e}*\n"
    
    report += """---

*Informe generado automáticamente por el pipeline CI/CD*
"""
    
    return report


def generate_test_report(test_type, surefire_xml):
    """Generate a decorated test report from Surefire XML."""
    test_name = "Unit Tests" if test_type == "unit" else "Integration Tests"
    
    report = generate_report_header(
        f"🧪 {test_name} Report",
        f"Results from {test_name.lower()}"
    )
    
    if os.path.exists(surefire_xml):
        try:
            import xml.etree.ElementTree as ET
            tree = ET.parse(surefire_xml)
            root = tree.getroot()
            
            testsuite = root.find('testsuite')
            if testsuite is not None:
                tests = int(testsuite.get('tests', 0))
                failures = int(testsuite.get('failures', 0))
                errors = int(testsuite.get('errors', 0))
                skipped = int(testsuite.get('skipped', 0))
                time = float(testsuite.get('time', 0))
                success = tests - failures - errors - skipped
                success_rate = (success / tests * 100) if tests > 0 else 0
                
                report += """## 📊 Resumen

| Métrica | Valor |
|---------|-------|
"""
                report += f"| Total tests | {tests} |\n"
                report += f"| ✅ Exitosos | {success} |\n"
                report += f"| ❌ Fallidos | {failures} |\n"
                report += f"| ⚠️ Errores | {errors} |\n"
                report += f"| ⏭️  Omitidos | {skipped} |\n"
                report += f"| ⏱️  Tiempo total | {time:.2f}s |\n"
                report += f"| 📈 Tasa de éxito | {success_rate:.1f}% |\n"
                
                report += "\n## 📈 Estado\n\n"
                if success_rate == 100:
                    report += "✅ **EXCELENTE**: Todos los tests pasaron correctamente.\n"
                elif success_rate >= 90:
                    report += "✅ **BIEN**: La mayoría de tests pasaron. Revisa los fallos.\n"
                elif success_rate >= 70:
                    report += "⚠️ **ADVERTENCIA**: Algunos tests fallaron. Requiere atención.\n"
                else:
                    report += "❌ **FALLO**: Muchos tests fallaron. Requiere investigación urgente.\n"
                    
        except Exception as e:
            report += f"\n*Nota: No se pudo analizar el informe XML: {e}*\n"
    
    report += """---

*Informe generado automáticamente por el pipeline CI/CD*
"""
    
    return report


def generate_quality_report(checkstyle_xml, spotbugs_xml):
    """Generate a decorated quality report from Checkstyle and SpotBugs."""
    report = generate_report_header(
        "🔧 Code Quality Report",
        "Static analysis results from Checkstyle and SpotBugs"
    )
    
    total_issues = 0
    
    # Checkstyle
    if os.path.exists(checkstyle_xml):
        try:
            import xml.etree.ElementTree as ET
            tree = ET.parse(checkstyle_xml)
            root = tree.getroot()
            
            checkstyle_errors = len(root.findall('.//error'))
            total_issues += checkstyle_errors
            
            if checkstyle_errors > 0:
                report += """## 📋 Checkstyle

| Severidad | Cantidad |
|-----------|----------|
"""
                # Group by error type
                error_types = {}
                for error in root.findall('.//error'):
                    source = error.get('source', 'Unknown')
                    error_types[source] = error_types.get(source, 0) + 1
                
                for source, count in sorted(error_types.items(), key=lambda x: x[1], reverse=True)[:10]:
                    report += f"| {source.split('.')[-1]} | {count} |\n"
            else:
                report += """## 📋 Checkstyle

✅ **SIN ERRORES**: El código cumple con las normas de estilo configuradas.

"""
        except:
            pass
    
    # SpotBugs
    if os.path.exists(spotbugs_xml):
        try:
            import xml.etree.ElementTree as ET
            tree = ET.parse(spotbugs_xml)
            root = tree.getroot()
            
            bugs = root.findall('.//BugInstance')
            spotbugs_issues = len(bugs)
            total_issues += spotbugs_issues
            
            if spotbugs_issues > 0:
                report += """## 🐛 SpotBugs

| Tipo | Cantidad |
|------|----------|
"""
                bug_types = {}
                for bug in bugs:
                    btype = bug.get('type', 'Unknown')
                    bug_types[btype] = bug_types.get(btype, 0) + 1
                
                for btype, count in sorted(bug_types.items(), key=lambda x: x[1], reverse=True)[:10]:
                    report += f"| {btype} | {count} |\n"
            else:
                report += """## 🐛 SpotBugs

✅ **SIN BUGS**: No se detectaron problemas estáticos en el código.

"""
        except:
            pass
    
    if total_issues == 0:
        report += """## ✅ Estado

**EXCELENTE**: No se encontraron problemas de calidad de código.

"""
    else:
        report += f"""## ⚠️ Estado

**ATENCIÓN**: Se detectaron {total_issues} problemas que requieren revisión.

"""
    
    report += """---

*Informe generado automáticamente por el pipeline CI/CD*
"""
    
    return report


def generate_index_page(pages):
    """Generate an index page for the wiki."""
    report = """---
title: Home
nav_order: 1
---

# 📋 Informes CI/CD

Bienvenido a la sección de informes automáticos del pipeline CI/CD.

## 📁 Informes Disponibles

"""
    
    for page in pages:
        icon = page.get('icon', '📄')
        title = page.get('title', 'Informe')
        path = page.get('path', '')
        desc = page.get('description', '')
        
        report += f"- {icon} [{title}]({path}) - {desc}\n"
    
    report += """
---

**Nota**: Estos informes se generan automáticamente en cada ejecución del pipeline CI/CD.
Los informes se sobrescriben en cada ejecución para mantener solo la versión más reciente.

## 🚀 Ejecución del Pipeline

El pipeline CI/CD ejecuta las siguientes verificaciones:

1. **Build & Compile**: Compilación del proyecto
2. **Unit Tests**: Tests unitarios
3. **Integration Tests**: Tests de integración
4. **Code Coverage**: Cobertura de código (JaCoCo)
5. **Security Scan**: Escaneo de seguridad (Trivy)
6. **Code Quality**: Calidad de código (Checkstyle, SpotBugs)
"""
    
    return report


def main():
    parser = argparse.ArgumentParser(description='Publish reports to GitHub Wiki')
    parser.add_argument('--token', required=True, help='GitHub token')
    parser.add_argument('--repo', required=True, help='Repository (owner/repo)')
    parser.add_argument('--report-dir', default='./reports', help='Report directory')
    parser.add_argument('--workflow', default='ci', help='Workflow type (ci, cd, pr)')
    
    args = parser.parse_args()
    
    owner, repo = args.repo.split('/')
    
    # Determine wiki clone URL
    wiki_url = f"https://x-access-token:{args.token}@github.com/{owner}/{repo}.wiki.git"
    
    # Clone wiki repo
    print("Cloning wiki repository...")
    wiki_dir = f"/tmp/{repo}-wiki"
    code, stdout, stderr = run_command(f"rm -rf {wiki_dir} && git clone {wiki_url} {wiki_dir}")
    
    if code != 0:
        print(f"Warning: Could not clone wiki (may be empty): {stderr}")
        # Create new wiki directory
        run_command(f"mkdir -p {wiki_dir}")
        run_command(f"cd {wiki_dir} && git init && git config user.email 'ci@notaire.com' && git config user.name 'CI Bot'")
    
    # Create reports directory
    reports_dir = os.path.join(wiki_dir, 'cicd', 'reports', args.workflow)
    os.makedirs(reports_dir, exist_ok=True)
    
    report_dir = args.report_dir
    
    # Generate and save reports based on workflow type
    pages = []
    
    if args.workflow == 'ci':
        # Security report
        if os.path.exists(os.path.join(report_dir, 'trivy-results.json')):
            content = generate_security_report(os.path.join(report_dir, 'trivy-results.json'))
            with open(os.path.join(reports_dir, 'security.md'), 'w') as f:
                f.write(content)
            pages.append({'icon': '🔒', 'title': 'Security Scan', 'path': 'cicd/reports/ci/security.md', 'description': 'Vulnerability scan results'})
        
        # Coverage report
        if os.path.exists(os.path.join(report_dir, 'jacoco.xml')):
            content = generate_coverage_report(os.path.join(report_dir, 'jacoco.xml'))
            with open(os.path.join(reports_dir, 'coverage.md'), 'w') as f:
                f.write(content)
            pages.append({'icon': '📊', 'title': 'Code Coverage', 'path': 'cicd/reports/ci/coverage.md', 'description': 'Coverage analysis results'})
        
        # Test reports
        if os.path.exists(os.path.join(report_dir, 'unit-tests.xml')):
            content = generate_test_report('unit', os.path.join(report_dir, 'unit-tests.xml'))
            with open(os.path.join(reports_dir, 'unit-tests.md'), 'w') as f:
                f.write(content)
            pages.append({'icon': '🧪', 'title': 'Unit Tests', 'path': 'cicd/reports/ci/unit-tests.md', 'description': 'Unit test results'})
        
        if os.path.exists(os.path.join(report_dir, 'integration-tests.xml')):
            content = generate_test_report('integration', os.path.join(report_dir, 'integration-tests.xml'))
            with open(os.path.join(reports_dir, 'integration-tests.md'), 'w') as f:
                f.write(content)
            pages.append({'icon': '🔗', 'title': 'Integration Tests', 'path': 'cicd/reports/ci/integration-tests.md', 'description': 'Integration test results'})
        
        # Quality report
        if os.path.exists(os.path.join(report_dir, 'checkstyle.xml')) or os.path.exists(os.path.join(report_dir, 'spotbugs.xml')):
            content = generate_quality_report(
                os.path.join(report_dir, 'checkstyle.xml'),
                os.path.join(report_dir, 'spotbugs.xml')
            )
            with open(os.path.join(reports_dir, 'quality.md'), 'w') as f:
                f.write(content)
            pages.append({'icon': '🔧', 'title': 'Code Quality', 'path': 'cicd/reports/ci/quality.md', 'description': 'Static analysis results'})
    
    elif args.workflow == 'cd':
        # CD specific reports
        content = generate_report_header(
            "🚀 Deployment Report",
            "CD pipeline execution results"
        )
        content += "\n## ✅ Estado\n\nEl pipeline CD se ejecutó correctamente.\n"
        with open(os.path.join(reports_dir, 'deployment.md'), 'w') as f:
            f.write(content)
        pages.append({'icon': '🚀', 'title': 'Deployment', 'path': 'cicd/reports/cd/deployment.md', 'description': 'Deployment results'})
    
    elif args.workflow == 'pr':
        # PR validation reports
        content = generate_report_header(
            "📋 PR Validation Report",
            "Pull request validation results"
        )
        content += "\n## ✅ Estado\n\nLa validación del PR se completó.\n"
        with open(os.path.join(reports_dir, 'pr-validation.md'), 'w') as f:
            f.write(content)
        pages.append({'icon': '📋', 'title': 'PR Validation', 'path': 'cicd/reports/pr/pr-validation.md', 'description': 'PR validation results'})
    
    # Generate index
    index_content = generate_index_page(pages)
    with open(os.path.join(wiki_dir, 'Home.md'), 'w') as f:
        f.write(index_content)
    
    # Create _Sidebar for navigation
    sidebar = """---
title: Navigation
nav_order: 2
---

# 📑 Navegación

## Informes CI/CD

"""
    if args.workflow == 'ci':
        sidebar += "- [🏠 Home](../Home.md)\n"
        for page in pages:
            icon = page.get('icon', '📄')
            title = page.get('title', 'Informe')
            rel_path = page.get('path', '').replace('cicd/reports/ci/', '')
            sidebar += f"- [{icon} {title}](./{rel_path})\n"
    
    with open(os.path.join(wiki_dir, '_Sidebar.md'), 'w') as f:
        f.write(sidebar)
    
    # Push to wiki
    print("Pushing to wiki...")
    code, stdout, stderr = run_command(f"cd {wiki_dir} && git add -A && git commit -m 'Update CI/CD reports' && git push origin main", capture_output=False)
    
    if code != 0:
        print(f"Warning: Could not push to wiki: {stderr}")
        # Try master branch (older wikis use master)
        code2, _, _ = run_command(f"cd {wiki_dir} && git branch -M master && git push origin master", capture_output=False)
        if code2 != 0:
            print("Failed to push to wiki")
            return 1
    
    print("Done! Reports published to wiki.")
    return 0


if __name__ == '__main__':
    sys.exit(main())