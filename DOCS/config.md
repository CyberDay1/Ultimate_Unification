Config gates
============
- `general.enableDropUnifier`: toggle the GLM for ore drops.
- `compression.*`: master and per-family toggles for registering compressed items/blocks. `maxTier` caps tiers (1..9).
- `filters.denyMaterials`: blacklist unified materials (metals or gems) by id.
- `filters.denyStones`: blacklist stones by id.

Behavior
--------
- Filters apply at registration time. Disabled materials are not registered, and their recipes/tags should be omitted by your static resource process.
- `maxTier` limits both item and block compressed tiers.

Pack guidance
-------------
- Use KubeJS or datapacks for fine-grained recipe removals. Use the config for coarse control over registries.
- If going to time out in Codex, push current changes via a PR and note completed steps and TODOs in the summary.
