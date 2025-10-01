package com.unifyworks.recipes;

import net.neoforged.fml.common.EventBusSubscriber;
import com.unifyworks.UnifyWorks;

@EventBusSubscriber(modid = UnifyWorks.MODID)
public class RecipeUnifier {
    // TODO: On recipe reload, scan ingredients and widen to tags for known aliases.
    // TODO: Register high-priority replacement recipes for canonical outputs.
}
