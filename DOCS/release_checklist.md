Release checklist
=================
- Validate specs: `tools/validate_all.sh`
- Lint JSON: ensure no trailing commas and valid UTF-8.
- Ensure no binary artifacts in VCS: wrapper JAR, caches.
- Confirm lang keys exist for every registered item/block.
- Verify umbrella tags and alias tags exist for all materials.
- Confirm worldgen toggles and prune flag behave via diagnostics commands.
- Build locally with offline Gradle if deps are cached; otherwise skip CI compile.

CI snippets
-----------
# Node + Python validators only
- run: node tools/validate_materials.js
- run: node tools/validate_filters_overrides.js
- run: python3 tools/scan_assets.py

Timeout rule
------------
If going to time out, push current changes via a PR and include which checks passed.
