Canonical recipe serializers
============================
UnifyWorks exposes canonical crafting recipe serializers that automatically swap outputs to the mod's unified items.

Serializers
-----------
- `unifyworks:canonical_shaped` — drop-in replacement for the vanilla shaped serializer.
- `unifyworks:canonical_shapeless` — drop-in replacement for the vanilla shapeless serializer.

Each serializer reads the same JSON payload as the vanilla equivalent. On craft and in the recipe book preview, results are
replaced with the canonical item from the relevant tag family when available.

Example
-------
```json
{
  "type": "unifyworks:canonical_shaped",
  "pattern": [
    "##",
    "##"
  ],
  "key": {
    "#": {
      "tag": "forge:ingots/copper"
    }
  },
  "result": {
    "item": "some_mod:copper_plate"
  }
}
```

At runtime the recipe output is automatically replaced with `unifyworks:copper_ingot` when the copper ingot family is available.

Datagen toggle
--------------
Pass `-Dunifyworks.datagen.canonicalSerializers=true` when running data generation to emit UnifyWorks' own conversions with the
canonical serializers instead of vanilla recipes.

API helpers
-----------
Use `CanonicalAPI` to resolve canonical metadata for items at runtime. The helper supports querying canonical material ids,
canonical item ids, and replacing stacks with their unified form.
