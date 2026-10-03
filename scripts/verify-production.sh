#!/usr/bin/env bash
set -euo pipefail

forbidden_patterns=(
  'DataSeeder'
  'seedInitialDataIfNeeded'
  'sampleReceipt'
  'Simulasi Analisis'
  'Isi Contoh Data CONTROL'
  'fallbackToDestructiveMigration'
  'content://media/photos/bukti_'
  'disetujuiOleh2 = "Drs. H. Surya Abadi"'
  'dibuatOleh = "Operator"'
)

failed=0

for pattern in "${forbidden_patterns[@]}"; do
  if git grep -n -i -- "$pattern" --     ':!app/src/test/**'     ':!app/src/androidTest/**'     ':!**/build/**'     ':!scripts/verify-production.sh'     ':!.github/workflows/**'; then
    echo "::error::Forbidden production pattern found: $pattern"
    failed=1
  fi
done

exit "$failed"
