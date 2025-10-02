package com.unifyworks.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.unifyworks.UnifyWorks;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Comparator;
import java.util.List;

public final class UWCommands {
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("unifyworks").requires(stack -> stack.hasPermission(2))
                .then(Commands.literal("worldgen")
                        .then(Commands.literal("dump").executes(ctx -> dumpPlacedFeatures(ctx.getSource())))));
    }

    private static int dumpPlacedFeatures(CommandSourceStack source) {
        var registry = source.getServer().registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        List<ResourceLocation> ids = registry.registryKeySet().stream()
                .map(key -> key.location())
                .filter(loc -> loc.getNamespace().equals(UnifyWorks.MODID))
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();

        if (ids.isEmpty()) {
            UnifyWorks.LOGGER.info("[UnifyWorks] No placed features registered under namespace {}.", UnifyWorks.MODID);
            source.sendSuccess(() -> Component.literal("[UnifyWorks] No placed features registered."), false);
            return 0;
        }

        UnifyWorks.LOGGER.info("[UnifyWorks] Dumping {} placed features:", ids.size());
        ids.forEach(id -> UnifyWorks.LOGGER.info(" - {}", id));
        source.sendSuccess(() -> Component.literal("[UnifyWorks] Logged " + ids.size() + " placed features."), false);
        return ids.size();
    }

    private UWCommands() {
    }
}
