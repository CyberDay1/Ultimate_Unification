package com.unifyworks.datagen;

import com.unifyworks.UnifyWorks;
import com.unifyworks.data.MaterialsIndex;
import com.unifyworks.registry.UWItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class UWItemModelProvider extends ItemModelProvider {
    public UWItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, UnifyWorks.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        var snap = MaterialsIndex.loadBootstrap();
        for (String metal : snap.metals) {
            var nugget = UWItems.NUGGETS.get(metal);
            var base = UWItems.BASE_ITEMS.get(metal);
            if (nugget != null) {
                basicItem(nugget.get());
            }
            if (base != null) {
                basicItem(base.get());
            }
        }
        for (String gem : snap.gems) {
            var nugget = UWItems.NUGGETS.get(gem);
            var base = UWItems.BASE_ITEMS.get(gem);
            if (nugget != null) {
                basicItem(nugget.get());
            }
            if (base != null) {
                basicItem(base.get());
            }
        }
    }
}
