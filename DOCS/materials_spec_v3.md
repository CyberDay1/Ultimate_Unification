Materials spec v3
=================
Add optional `aliases` per material. Aliases expand tag coverage and add conversions to the canonical UnifyWorks IDs.

Example:
{
  "name": "aluminum",
  "kind": "metal",
  "aliases": ["aluminium"],
  "unify": true,
  "provide_nugget": true,
  "provide_storage_block": true
}

Behavior:
- For each alias A of material N:
  - Tags: include UnifyWorks canonical items/blocks in forge and c tags under both N and A.
  - Conversions: add shapeless recipes that convert any tag inputs under A to the canonical base item of N.
  - Storage block conversion: stonecutting 1→1 from tag storage_blocks/A to canonical storage block of N.
