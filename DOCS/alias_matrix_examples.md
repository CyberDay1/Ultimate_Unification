Alias examples
==============
- aluminum ⇄ aluminium
- lapis_lazuli ⇄ lapis
- refined_glowstone ⇄ refined_glow_stone ⇄ refined glowstone (space variants normalize to snake_case)
- refined_obsidian ⇄ refined_obsidian (Mekanism) — already canonical, but add alias if a pack uses obsidian_refined
- antimony often appears as stibnite for the ore form; keep item aliases to antimony

Guidance:
- Put every alternate spelling in `aliases` for the canonical `name` in materials.json v3.
- Avoid adding new canonical names via aliases; the canonical is always `name`.
