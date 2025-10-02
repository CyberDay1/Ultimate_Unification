package com.unifyworks.unify;

import com.unifyworks.UnifyWorks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mod.EventBusSubscriber(modid = UnifyWorks.MODID)
public final class RuntimeCanonicalization {
    private RuntimeCanonicalization() {}

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent evt) {
        canonicalize(evt.getCrafting());
    }

    @SubscribeEvent
    public static void onSmelted(PlayerEvent.ItemSmeltedEvent evt) {
        canonicalize(evt.getSmelting());
    }

    private static void canonicalize(ItemStack stack) {
        if (stack.isEmpty()) return;
        var diagnostics = CanonicalFamilies.diagnose(stack.getItem());
        if (diagnostics.isEmpty()) return;

        Item held = stack.getItem();
        for (CanonicalFamilies.TagCheck tag : diagnostics.get().tags()) {
            if (!tag.present()) continue;
            if (!tag.containsHeld()) continue;
            Item canonical = tag.canonicalItem();
            if (canonical == null || canonical == held) continue;
            int count = stack.getCount();
            stack.setItem(canonical);
            stack.setCount(count);
            return;
        }
    }
}
