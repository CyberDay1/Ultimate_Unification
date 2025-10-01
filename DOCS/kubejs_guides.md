KubeJS guides
=============

Disable a material entirely
---------------------------
Edit `kubejs/startup_scripts/unifyworks.example.js`:
- Add the material ID (lowercase) to `removeMaterials`.
- This removes the UnifyWorks canonical outputs at load.

Replace hardcoded inputs with tags
----------------------------------
Populate the `replacements` array with `{ from: '<modid:item>', to: '#forge:ingots/<name>' }`, etc.
This fixes third-party recipes that do not use tags.

Force canonical outputs
-----------------------
The sample adds shapeless recipes that convert any tagged item to the canonical item.
Adjust the metal/gem arrays to match your pack.
