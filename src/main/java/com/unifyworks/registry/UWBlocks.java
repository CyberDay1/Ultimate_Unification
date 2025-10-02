package com.unifyworks.registry;

import com.unifyworks.UnifyWorks;
import com.unifyworks.data.MaterialsIndex;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

/** Canonical storage blocks for metals and gems. */
public class UWBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(UnifyWorks.MODID);
    public static final Map<String, DeferredBlock<Block>> STORAGE_BLOCKS = new LinkedHashMap<>();

    public static void bootstrap(MaterialsIndex.Snapshot snap) {
        STORAGE_BLOCKS.clear();
        for (String metal : snap.metals) {
            registerStorageBlock(snap, metal, true);
        }
        for (String gem : snap.gems) {
            registerStorageBlock(snap, gem, false);
        }
    }

    private static void registerStorageBlock(MaterialsIndex.Snapshot snap, String name, boolean metal) {
        DeferredBlock<Block> block = BLOCKS.register(name + "_block",
                () -> new Block(storageProps(snap.miningFor(name), metal)));
        STORAGE_BLOCKS.put(name, block);
        UWItems.ITEMS.registerSimpleBlockItem(block);
    }

    private static BlockBehaviour.Properties storageProps(MaterialsIndex.MiningSpec mining, boolean metal) {
        float hardness = mining.blockHardness();
        float resistance = hardness + 1.0F;
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of()
                .mapColor(metal ? MapColor.METAL : MapColor.COLOR_LIGHT_BLUE)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops();
        return metal ? props.sound(SoundType.METAL) : props.sound(SoundType.AMETHYST);
    }
}
