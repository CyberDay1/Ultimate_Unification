// KubeJS examples for UnifyWorks
// Deny materials at runtime (works alongside config). Remove copper + lapis.
ServerEvents.tags('item', event => {
  // Redirect selected outputs to canonical tags if needed
  // Example: treat all gold ingots as tags
  event.add('forge:ingots/gold', 'unifyworks:gold_ingot')
  event.add('c:ingots/gold', 'unifyworks:gold_ingot')
})

ServerEvents.highPriorityData(event => {
  // Remove problematic recipes by id or type if needed
  // event.remove({ id: 'some_mod:some_recipe' })
})

ServerEvents.recipes(event => {
  // Force canonical outputs for a third-party recipe by replacing result
  // event.replaceOutput({ output: /.*:copper_ingot/ }, 'unifyworks:copper_ingot')
})
