#!/bin/bash
# Script to create GitHub issues for all Use Cases

# Read CSV and create issues for each CU
# Skip header line
tail -n +2 "docs/business/03_CU - Casos de Uso/Progreso Sistema - CASOS DE USO.csv" | while IFS=, read -r cu modulo grado estado progreso doc_sistema progreso_doc auditoria concurrencia observaciones; do
    # Extract CU number from CU field (e.g., "CU01 – Preparar Presupuesto")
    cu_name=$(echo "$cu" | sed 's/–/:/' | tr -d '"')
    
    # Extract CU number
    cu_num=$(echo "$cu_name" | cut -d':' -f1)
    
    # Create title
    title="$cu_name"
    
    # Create body
    body="**Caso de Uso:** $cu_name

**Módulo:** $modulo
**Grado:** $grado
**Estado:** $estado
**Progreso:** $progreso%

**Referencias Cruzadas:** $observaciones

---
*Generated from docs/business/03_CU - Casos de Uso/Progreso Sistema - CASOS DE USO.csv*"
    
    # Create issue
    echo "Creating issue: $title"
    rtk gh issue create --repo matiaspakua/notaire --title "$title" --body "$body" 2>&1
    
    sleep 1
done
