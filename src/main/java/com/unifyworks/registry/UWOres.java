package com.unifyworks.registry;

import com.unifyworks.UnifyWorks;
import com.unifyworks.data.MaterialsIndex;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public final class UWOres {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(NeoForgeRegistries.BLOCKS, UnifyWorks.MODID);

    public static final Map<String, RegistryObject<Block>> STONE = new HashMap<>();
    public static final Map<String, RegistryObject<Block>> DEEPSLATE = new HashMap<>();
    public static final Map<String, RegistryObject<Block>> NETHERRACK = new HashMap<>(); // e.g., quartz

    public static void bootstrap(MaterialsIndex.Snapshot snap) {
        BlockBehaviour.Properties stoneProps = BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE).strength(3.0F, 3.0F).requiresCorrectToolForDrops().sound(SoundType.STONE);
        BlockBehaviour.Properties deepslateProps = BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE).strength(4.5F, 3.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE);
        BlockBehaviour.Properties netherProps = BlockBehaviour.Properties.of()
                .mapColor(MapColor.NETHER).strength(3.0F, 3.0F).requiresCorrectToolForDrops().sound(SoundType.NETHER_BRICKS);

        for (MaterialsIndex.OreEntry ore : snap.ores) {
            String name = ore.name();
            if (ore.stone()) {
                STONE.put(name, BLOCKS.register(name + "_ore", () -> new Block(stoneProps)));
            }
            if (ore.deepslate()) {
                DEEPSLATE.put(name, BLOCKS.register("deepslate_" + name + "_ore", () -> new Block(deepslateProps)));
            }
            if (ore.netherrack()) {
                NETHERRACK.put(name, BLOCKS.register("netherrack_" + name + "_ore", () -> new Block(netherProps)));
            }
        }
    }

    private UWOres() {}
}
