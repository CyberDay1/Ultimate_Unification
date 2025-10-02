Worldgen config gates
=====================
- `enableWorldgen`: master switch for UnifyWorks ore features.
- `enable{Overworld,Nether,End}Ores`: per-dimension toggles.
- `sizeMultiplierPct`, `countMultiplierPct`: global multipliers applied by conditional biome modifiers (future enhancement).

Implementation
--------------
- Biome modifiers use `unifyworks:conditional_add_features` which checks config at runtime.
- JSON is static; switches require no datapack edits.

Command
-------
- `/unifyworks worldgen dump` logs the list of UnifyWorks placed features to the server console.

Timeout rule
------------
If going to time out, push current changes via a PR and note completed parts and TODOs.
