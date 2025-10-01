Static recipes and tags
=======================

This repository includes code registries for nuggets, base items, and storage blocks.
To avoid `runData` in restricted CI, we emit static recipes and tag JSON directly into `src/main/resources`.

- Recipes are generated from `data/unifyworks/unify/materials.json` (version 2).
- Outputs target our canonical registry IDs.
- If CI cannot fetch Mojang assets, skip any build that compiles game code. Commit only resource changes.

Manual local run (preferred for full verification):
- `./gradlew runData --args="--mod unifyworks --all --output src/generated/resources --existing src/main/resources"`
- Review diffs then copy generated JSON into `src/main/resources` if needed.

Timeout policy:
- If a timeout is imminent, commit current changes to a new branch, open a PR, and add a summary note with progress and remaining steps.
