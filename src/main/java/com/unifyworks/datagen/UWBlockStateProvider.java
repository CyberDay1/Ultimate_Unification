package com.unifyworks.datagen;

import com.unifyworks.UnifyWorks;
import com.unifyworks.data.MaterialsIndex;
import com.unifyworks.registry.UWBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class UWBlockStateProvider extends BlockStateProvider {
    public UWBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, UnifyWorks.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        var snap = MaterialsIndex.loadBootstrap();
        for (String metal : snap.metals) {
            var entry = UWBlocks.STORAGE_BLOCKS.get(metal);
            if (entry != null) {
                var block = entry.get();
                simpleBlockWithItem(block, cubeAll(block));
            }
        }
        for (String gem : snap.gems) {
            var entry = UWBlocks.STORAGE_BLOCKS.get(gem);
            if (entry != null) {
                var block = entry.get();
                simpleBlockWithItem(block, cubeAll(block));
            }
        }
    }
}
