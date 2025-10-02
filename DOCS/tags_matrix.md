Tag matrix
==========
For each unified material N:
- Base item: `forge:{ingots|gems}/N` and `c:{ingots|gems}/N` contain `unifyworks:{N}_ingot|{N}_gem`.
- Nuggets: `forge:nuggets/N` and `c:nuggets/N` contain `unifyworks:{N}_nugget`.
- Raw materials: `forge:raw_materials/N` and `c:raw_materials/N` contain `unifyworks:raw_{N}` (metals only).
- Storage blocks (items+blocks): both namespaces include `unifyworks:{N}_block`.
- Ores: `forge|c:ores/N` bridge to internal helper tags that include UnifyWorks stone/deepslate (+netherrack quartz).

Umbrella tags
-------------
- `#unifyworks:umbrella/{ingots|gems|nuggets|raw_materials|dusts|storage_blocks}` contain both `#forge/*` and `#c/*` trees.

Notes
-----
- Keep values `optional` where needed if your generation tool supports it. External ores might not exist.
- Packs can target a single umbrella tag to hit both ecosystems.
