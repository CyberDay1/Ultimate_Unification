Data-driven ore drops
=====================
- The drop unifier inspects ore tags from `materials.json` at runtime.
- A reload listener maps every ore tag (including aliases) to the canonical raw chunk or gem item.
- Metals drop `raw_<name>`; gems drop `<name>_gem`. Alias materials inherit the canonical drop.
- Toggle the GLM at runtime via `unifyworks-common.toml` → `enableDropUnifier`.

Validate materials.json
-----------------------
Run `node tools/validate_materials.js` (optionally pass a custom file path).
Wire this into Codex CI before any Gradle tasks.
