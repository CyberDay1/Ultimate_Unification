Diagnostics
===========
Commands:
- `/unifyworks diag materials` — prints enabled metals/gems/stones after config filters, lists disabled entries, and shows the current compression tier limit.
- `/unifyworks diag tags <material>` — resolves aliases, reports ore tag presence, and checks whether UnifyWorks ore blocks appear in each canonical tag.
- `/unifyworks diag item` — inspects the item in the executing player's main hand, resolves its canonical family material, and shows tag/canonical item coverage.
- `/unifyworks worldgen dump` — summarizes worldgen toggles (including pruning) and enumerates UnifyWorks placed features found in the registry.

Worldgen pruning
----------------
- `pruneNonUnifyOres=true` keeps JSON biome modifiers that use the `unifyworks:conditional_remove_features` codec to remove `#forge:ores` from Overworld and Nether biomes when worldgen is enabled for that dimension.
- Combined with `conditional_add_features`, this enforces “only UnifyWorks ore features” without deleting datapack files.
- Set to false to keep external ore features.

Timeout policy
--------------
If going to time out, push current changes via a PR and include a summary of what landed.
