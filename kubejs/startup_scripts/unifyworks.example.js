// KubeJS pack-side helpers for UnifyWorks
// Copy to kubejs/startup_scripts/ and rename to remove ".example".

ServerEvents.recipes(event => {
  // 1) Remove specific materials globally
  // Example: remove tin across items and blocks
  const removeMaterials = ['tin']; // fill from pack config
  removeMaterials.forEach(m => {
    event.remove({ output: RegExp(`^unifyworks:(nugget_${m}|${m}_ingot|${m}_gem|${m}_block)$`) })
  });

  // 2) Force canonical outputs for tag families
  const metals = ['iron','copper','gold','silver','lead','tin','nickel','aluminum','uranium','zinc']; // extend
  const gems   = ['diamond','emerald','ruby','sapphire','lapis_lazuli','fluorite','apatite']; // extend

  metals.forEach(m => {
    // Any ingot tag -> canonical ingot
    event.shapeless(Item.of(`unifyworks:${m}_ingot`), [Ingredient.of(`#forge:ingots/${m}`)])
    // Any nugget tag -> canonical ingot (via 9x crafting already provided)
    // Any blocks tag -> canonical block if needed
  });

  gems.forEach(g => {
    event.shapeless(Item.of(`unifyworks:${g}_gem`), [Ingredient.of(`#forge:gems/${g}`)])
  });

  // 3) Replace hardcoded inputs in foreign recipes with tag ingredients
  // Example: replace "othermod:tin_ingot" with "#forge:ingots/tin" across all recipes
  const replacements = [
    { from: 'othermod:tin_ingot', to: '#forge:ingots/tin' },
    { from: 'othermod:ruby', to: '#forge:gems/ruby' },
  ];
  replacements.forEach(r => event.replaceInput({}, r.from, Ingredient.of(r.to)));
});
