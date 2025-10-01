package com.unifyworks.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class UWDataGenBootstrap {
    private UWDataGenBootstrap() {
    }

    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var packOutput = generator.getPackOutput();
        var existingFileHelper = event.getExistingFileHelper();
        var lookupProvider = event.getLookupProvider();

        var blockTags = new UWBlockTagsProvider(packOutput, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new UWItemTagsProvider(packOutput, lookupProvider, blockTags, existingFileHelper));
        generator.addProvider(event.includeServer(), new UWRawTagsProvider(packOutput, lookupProvider, blockTags, existingFileHelper));
        generator.addProvider(event.includeServer(), new UWRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeClient(), new UWBlockStateProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new UWItemModelProvider(packOutput, existingFileHelper));
    }
}
