package com.unifyworks.registry;

import com.unifyworks.UnifyWorks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryObject;

public final class UWCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UnifyWorks.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.unifyworks"))
                    .icon(() -> UWItems.BASE_ITEMS.values().stream()
                            .findFirst()
                            .map(holder -> new ItemStack(holder.get()))
                            .orElse(ItemStack.EMPTY))
                    .displayItems((params, output) -> {
                        UWItems.NUGGETS.values().forEach(holder -> output.accept(holder.get()));
                        UWItems.BASE_ITEMS.values().forEach(holder -> output.accept(holder.get()));
                        UWBlocks.STORAGE_BLOCKS.values().forEach(holder -> output.accept(holder.get()));
                        UWOres.STONE.values().forEach(holder -> output.accept(holder.get()));
                        UWOres.DEEPSLATE.values().forEach(holder -> output.accept(holder.get()));
                        UWOres.NETHERRACK.values().forEach(holder -> output.accept(holder.get()));
                    })
                    .build());

    private UWCreativeTab() {}
}
