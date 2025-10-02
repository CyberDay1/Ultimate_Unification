#!/usr/bin/env bash
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"
node tools/validate_materials.js
node tools/validate_filters_overrides.js
python3 tools/scan_assets.py
echo "All validators passed."
