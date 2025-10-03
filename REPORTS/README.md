Reports
=======
- `qa_scan.json`: output of `tools/scan_assets.py`.
- `placeholder_textures.json`: dry-run report from `tools/gen_placeholder_textures.py` enumerating missing textures.

If `qa_scan.json` contains issues:
- Re-run `tools/gen_static_assets.py` after updating `materials.json`.
- Manually add models/loot/tags for non-standard materials if needed.
- Generate local 1×1 PNG placeholders with `python3 tools/gen_placeholder_textures.py --write` if you need temporary assets, but avoid committing the binaries.

CI guardrails
-------------
- `scripts/assert-no-placeholders.py` fails CI and pre-commit if 1×1 placeholder PNGs or `*_placeholder.png` are present.
- Install the local git hook: `bash scripts/install-git-hooks.sh`.
- CI runs validators and uploads `REPORTS/qa_scan.json` and `REPORTS/placeholder_textures.json` generated in dry-run mode.
