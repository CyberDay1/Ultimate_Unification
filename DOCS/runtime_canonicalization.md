Runtime canonicalization
=======================
- Hooks ItemCraftedEvent and ItemSmeltedEvent.
- If the produced item belongs to a unification tag family, replace it with the UnifyWorks canonical item.
- Avoids mutating third-party JSON. Works with recipes that use hardcoded inputs.

Limits
------
- Heuristic material extraction from item IDs is best-effort. Future work: drive via materials.json index of tag keys per material.
- Does not intercept outputs from custom machines. Pack authors can add KubeJS conversions for those.

Timeout policy
--------------
If execution is going to time out, push current changes via a PR and note in the summary how far you made it in these tasks.
