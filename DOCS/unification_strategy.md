Unification strategy
====================
- Prefer tag-based inputs across the ecosystem.
- We do not replace other mods' JSON. Instead, we provide:
  1) "Any-tag → canonical" shapeless conversions so players can convert foreign items into the unified canonical item.
  2) Forge and c tags that always include the canonical item.
  3) KubeJS scripts to remove non-tag recipes and to force canonical outputs.

Limits
------
- If a third-party recipe hardcodes an item ID (not a tag), our conversions do not change that recipe. Use the provided KubeJS script to replace it pack-side.
