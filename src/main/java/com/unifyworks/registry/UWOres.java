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
        for (MaterialsIndex.OreEntry ore : snap.ores) {
            String name = ore.name();
            MaterialsIndex.MiningSpec mining = snap.miningFor(name);
            if (ore.stone()) {
                STONE.put(name, BLOCKS.register(name + "_ore", () -> new Block(orePropsFor(mining, MapColor.STONE, SoundType.STONE, 1.0F))));
            }
            if (ore.deepslate()) {
                DEEPSLATE.put(name, BLOCKS.register("deepslate_" + name + "_ore", () -> new Block(orePropsFor(mining, MapColor.DEEPSLATE, SoundType.DEEPSLATE, 1.5F))));
            }
            if (ore.netherrack()) {
                NETHERRACK.put(name, BLOCKS.register("netherrack_" + name + "_ore", () -> new Block(orePropsFor(mining, MapColor.NETHER, SoundType.NETHER_BRICKS, 1.0F))));
            }
        }
    }

    private static BlockBehaviour.Properties orePropsFor(MaterialsIndex.MiningSpec mining, MapColor color, SoundType sound, float multiplier) {
        float hardness = mining.oreHardness() * multiplier;
        float resistance = hardness + 3.0F;
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops()
                .sound(sound);
    }

    private UWOres() {}
}
