Reports
=======
- `qa_scan.json`: output of `tools/scan_assets.py`.
- `placeholder_textures.json`: dry-run report from `tools/gen_placeholder_textures.py` enumerating missing textures.

If `qa_scan.json` contains issues:
- Re-run `tools/gen_static_assets.py` after updating `materials.json`.
- Manually add models/loot/tags for non-standard materials if needed.
- Generate local 1×1 PNG placeholders with `python3 tools/gen_placeholder_textures.py --write` if you need temporary assets, but avoid committing the binaries.
