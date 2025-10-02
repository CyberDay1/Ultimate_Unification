Datapack filters and overrides
==============================

UnifyWorks datapacks can disable materials or tweak their properties without editing the bundled `materials.json`.
Two resource locations are inspected during the reload process:

- `data/<namespace>/unify/filters.json` — global deny-lists and compression caps.
- `data/<namespace>/unify/overrides/*.json` — per-material field overrides and additive aliases.

Merge order
-----------
1. Base `materials.json` (v4 format).
2. Every `filters.json`, processed by namespace and path in alphabetical order.
3. Every override file, also processed by namespace/path order.

Inside a single key the *last* datapack wins. Deny-lists are merged additively.
`aliases_add` entries are appended uniquely; duplicates are ignored.

Effects
-------
- `deny_materials` removes the material from the merged snapshot and all downstream systems.
- `deny_stones` removes stone entries (used by worldgen + compression) without touching metals or gems.
- `max_compression_tier` clamps the effective compression tier (min/max 1-9) after mod config is applied.
- Material overrides can toggle booleans such as `unify`, `generate_ore`, `provide_nugget`, `provide_storage_block`,
  add aliases, adjust mining properties, or flip ore variants.

Diagnostics
-----------
- `/unifyworks diag materials` reports totals after filters/overrides and shows the effective compression tier cap.
- `/unifyworks diag material <name>` prints the merged entry for a specific material, including aliases, tags,
  ore variants, mining data, datapack override status, and canonical drop information.
- `/unifyworks diag tags <name>` continues to show tag coverage for the merged snapshot.
