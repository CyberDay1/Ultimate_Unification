package com.unifyworks.registry;

import com.unifyworks.UnifyWorks;
import com.unifyworks.data.MaterialsIndex;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public final class UWCompressed {
    public static final DeferredRegister<Item>  ITEMS  = DeferredRegister.create(NeoForgeRegistries.ITEMS,  UnifyWorks.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(NeoForgeRegistries.BLOCKS, UnifyWorks.MODID);

    public static final Map<String, Map<Integer, RegistryObject<Item>>>  COMPRESSED_ITEMS  = new HashMap<>();
    public static final Map<String, Map<Integer, RegistryObject<Block>>> COMPRESSED_BLOCKS = new HashMap<>();
    public static final Map<String, Map<Integer, RegistryObject<Item>>>  COMPRESSED_BLOCK_ITEMS = new HashMap<>();

    public static void bootstrap(MaterialsIndex.Snapshot snap, int maxTier) {
        BlockBehaviour.Properties metalProps = BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.METAL);
        BlockBehaviour.Properties stoneProps = BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE).strength(1.5F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.STONE);

        for (String m : snap.metals) {
            for (int t = 1; t <= maxTier; t++) {
                final int tier = t;
                String itemId = m + "_compressed_item_x" + tier;
                String blockId = m + "_compressed_block_x" + tier;

                COMPRESSED_ITEMS.computeIfAbsent(m, k -> new HashMap<>()).put(tier,
                        ITEMS.register(itemId, () -> new Item(new Item.Properties())));

                RegistryObject<Block> bro = BLOCKS.register(blockId, () -> new Block(metalProps));
                COMPRESSED_BLOCKS.computeIfAbsent(m, k -> new HashMap<>()).put(tier, bro);
                COMPRESSED_BLOCK_ITEMS.computeIfAbsent(m, k -> new HashMap<>()).put(tier,
                        ITEMS.register(blockId, () -> new BlockItem(bro.get(), new Item.Properties())));
            }
        }
        for (String g : snap.gems) {
            for (int t = 1; t <= maxTier; t++) {
                final int tier = t;
                String itemId = g + "_compressed_item_x" + tier;
                String blockId = g + "_compressed_block_x" + tier;

                COMPRESSED_ITEMS.computeIfAbsent(g, k -> new HashMap<>()).put(tier,
                        ITEMS.register(itemId, () -> new Item(new Item.Properties())));

                RegistryObject<Block> bro = BLOCKS.register(blockId, () -> new Block(metalProps));
                COMPRESSED_BLOCKS.computeIfAbsent(g, k -> new HashMap<>()).put(tier, bro);
                COMPRESSED_BLOCK_ITEMS.computeIfAbsent(g, k -> new HashMap<>()).put(tier,
                        ITEMS.register(blockId, () -> new BlockItem(bro.get(), new Item.Properties())));
            }
        }
        for (String s : snap.stones) {
            for (int t = 1; t <= maxTier; t++) {
                final int tier = t;
                String blockId = s + "_compressed_block_x" + tier;
                RegistryObject<Block> bro = BLOCKS.register(blockId, () -> new Block(stoneProps));
                COMPRESSED_BLOCKS.computeIfAbsent(s, k -> new HashMap<>()).put(tier, bro);
                COMPRESSED_BLOCK_ITEMS.computeIfAbsent(s, k -> new HashMap<>()).put(tier,
                        ITEMS.register(blockId, () -> new BlockItem(bro.get(), new Item.Properties())));
            }
        }
    }

    private UWCompressed() {}
}
