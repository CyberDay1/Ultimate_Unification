package com.unifyworks.registry;

import com.unifyworks.UnifyWorks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Canonical storage blocks for metals and gems. */
public class UWBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(UnifyWorks.MODID);
    public static final Map<String, DeferredBlock<Block>> STORAGE_BLOCKS = new HashMap<>();

    public static void bootstrap(List<String> metals, List<String> gems) {
        STORAGE_BLOCKS.clear();
        for (String m : metals) {
            DeferredBlock<Block> block = BLOCKS.register(m + "_block", () -> new Block(metalStorageProps()));
            STORAGE_BLOCKS.put(m, block);
            UWItems.ITEMS.registerSimpleBlockItem(block);
        }
        for (String g : gems) {
            DeferredBlock<Block> block = BLOCKS.register(g + "_block", () -> new Block(gemStorageProps()));
            STORAGE_BLOCKS.put(g, block);
            UWItems.ITEMS.registerSimpleBlockItem(block);
        }
    }

    private static BlockBehaviour.Properties metalStorageProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 6.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL);
    }

    private static BlockBehaviour.Properties gemStorageProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(5.0F, 6.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.AMETHYST);
    }
}
