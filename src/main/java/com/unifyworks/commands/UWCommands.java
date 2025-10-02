package com.unifyworks.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.unifyworks.UnifyWorks;
import com.unifyworks.api.CanonicalAPI;
import com.unifyworks.config.UWConfig;
import com.unifyworks.config.UWWorldgenConfig;
import com.unifyworks.data.MaterialsIndex;
import com.unifyworks.data.UnifyDataReload;
import com.unifyworks.unify.CanonicalFamilies;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class UWCommands {
    private static final String PREFIX = "[UnifyWorks] ";
    private static final int LIST_CHUNK = 10;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("unifyworks").requires(s -> s.hasPermission(2))
                .then(Commands.literal("worldgen")
                        .then(Commands.literal("dump").executes(ctx -> dumpWorldgen(ctx.getSource()))))
                .then(Commands.literal("diag")
                        .then(Commands.literal("materials").executes(ctx -> diagMaterials(ctx.getSource())))
                        .then(Commands.literal("tags")
                                .then(Commands.argument("material", StringArgumentType.word())
                                        .executes(ctx -> diagTags(ctx.getSource(), StringArgumentType.getString(ctx, "material")))))
                        .then(Commands.literal("item")
                                .executes(ctx -> diagItem(ctx.getSource(), ctx.getSource().getPlayerOrException())))));
    }

    private static int diagMaterials(CommandSourceStack src) {
        MaterialsIndex.Snapshot snapshot = UnifyDataReload.snapshot();
        Set<String> denyMaterials = UWConfig.denyMaterials();
        Set<String> denyStones = UWConfig.denyStones();
        MaterialsIndex.Snapshot enabled = snapshot.filtered(denyMaterials, denyStones);

        List<String> metals = new ArrayList<>(enabled.metals);
        metals.sort(String::compareTo);
        List<String> gems = new ArrayList<>(enabled.gems);
        gems.sort(String::compareTo);
        List<String> stones = new ArrayList<>(enabled.stones);
        stones.sort(String::compareTo);

        Set<String> disabledMaterials = new TreeSet<>(snapshot.allMaterials());
        disabledMaterials.removeAll(enabled.allMaterials());
        disabledMaterials.addAll(denyMaterials);
        List<String> disabledMaterialList = new ArrayList<>(disabledMaterials);

        Set<String> allStones = new TreeSet<>(snapshot.stones);
        allStones.removeAll(enabled.stones);
        allStones.addAll(denyStones);
        List<String> disabledStones = new ArrayList<>(allStones);

        int maxTier = UWConfig.compressionEnabled() ? UWConfig.maxTier() : 0;
        sendPrefixed(src, String.format(Locale.ROOT,
                "Materials: metals=%d gems=%d stones=%d maxCompressionTier=%d",
                metals.size(), gems.size(), stones.size(), maxTier));
        sendList(src, "Enabled metals", metals);
        sendList(src, "Enabled gems", gems);
        sendList(src, "Enabled stones", stones);
        sendList(src, "Disabled materials", disabledMaterialList);
        sendList(src, "Disabled stones", disabledStones);
        return 1;
    }

    private static int diagTags(CommandSourceStack src, String materialInput) {
        String query = materialInput.toLowerCase(Locale.ROOT);
        MaterialsIndex.Snapshot snapshot = UnifyDataReload.snapshot();
        MaterialsIndex.MaterialEntry material = snapshot.find(query);
        if (material == null) {
            sendPrefixed(src, "Unknown material: " + query);
            return 0;
        }

        String canonicalName = material.name();
        boolean isAlias = !Objects.equals(canonicalName, query);
        if (isAlias) {
            sendPrefixed(src, String.format(Locale.ROOT, "Alias '%s' resolved to canonical material '%s'.", query, canonicalName));
        } else {
            sendPrefixed(src, "Material: " + canonicalName);
        }
        sendPrefixed(src, String.format(Locale.ROOT, "unify=%s generateOre=%s", material.unify(), material.generateOre()));

        Set<String> denyMaterials = UWConfig.denyMaterials();
        if (denyMaterials.contains(canonicalName)) {
            sendPrefixed(src, "Warning: material is denied via config and will not register ores.");
        }

        MaterialsIndex.AliasEntry aliasEntry = null;
        if (isAlias) {
            aliasEntry = snapshot.oreAliases().stream()
                    .filter(a -> a.name().equals(query))
                    .findFirst()
                    .orElse(null);
        }

        Map<ResourceLocation, List<String>> tagSources = new LinkedHashMap<>();
        material.oreTags().forEach(tag -> tagSources.computeIfAbsent(tag, k -> new ArrayList<>()).add("canonical"));
        if (aliasEntry != null) {
            aliasEntry.oreTags().forEach(tag -> tagSources.computeIfAbsent(tag, k -> new ArrayList<>()).add("alias"));
        }

        if (tagSources.isEmpty()) {
            sendPrefixed(src, "No ore tags recorded for this material.");
        }

        MaterialsIndex.OreEntry oreEntry = snapshot.ores.stream()
                .filter(o -> o.name().equals(canonicalName))
                .findFirst()
                .orElse(null);
        Map<String, ResourceLocation> expectedBlocks = new LinkedHashMap<>();
        if (oreEntry != null) {
            if (oreEntry.stone()) {
                expectedBlocks.put("stone", new ResourceLocation(UnifyWorks.MODID, canonicalName + "_ore"));
            }
            if (oreEntry.deepslate()) {
                expectedBlocks.put("deepslate", new ResourceLocation(UnifyWorks.MODID, "deepslate_" + canonicalName + "_ore"));
            }
            if (oreEntry.netherrack()) {
                expectedBlocks.put("netherrack", new ResourceLocation(UnifyWorks.MODID, "netherrack_" + canonicalName + "_ore"));
            }
        }

        Registry<Block> blockRegistry = src.getLevel().registryAccess().registryOrThrow(Registries.BLOCK);
        for (Map.Entry<ResourceLocation, List<String>> entry : tagSources.entrySet()) {
            ResourceLocation tagId = entry.getKey();
            TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagId);
            var holdersOpt = blockRegistry.getTag(tagKey);
            boolean present = holdersOpt.isPresent();
            int entryCount = 0;
            Map<String, Boolean> variantHits = new LinkedHashMap<>();
            expectedBlocks.forEach((variant, id) -> variantHits.put(variant, false));
            if (present) {
                for (var holder : holdersOpt.get()) {
                    entryCount++;
                    ResourceLocation holderId = blockRegistry.getKey(holder.value());
                    if (holderId == null) continue;
                    for (Map.Entry<String, ResourceLocation> variant : expectedBlocks.entrySet()) {
                        if (variant.getValue().equals(holderId)) {
                            variantHits.put(variant.getKey(), true);
                        }
                    }
                }
            }

            StringBuilder line = new StringBuilder();
            line.append("Tag ").append(tagId).append(": present=").append(present);
            line.append(" entries=").append(entryCount);
            if (!entry.getValue().isEmpty()) {
                line.append(" source=").append(String.join("+", entry.getValue()));
            }
            if (!variantHits.isEmpty()) {
                String variantSummary = variantHits.entrySet().stream()
                        .map(e -> e.getKey() + "=" + (e.getValue() ? "✔" : "✘"))
                        .collect(Collectors.joining(", "));
                line.append(" variants=").append(variantSummary);
            }
            sendPrefixed(src, line.toString());
        }

        if (oreEntry == null) {
            sendPrefixed(src, "No UnifyWorks ore variants are registered for this material.");
        }

        return 1;
    }

    private static int diagItem(CommandSourceStack src, ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            sendPrefixed(src, "Main hand is empty.");
            return 0;
        }

        ResourceLocation heldId = BuiltInRegistries.ITEM.getKey(held.getItem());
        sendPrefixed(src, String.format(Locale.ROOT, "Held %s x%d", heldId, held.getCount()));

        Optional<CanonicalFamilies.FamilyDiagnostics> diagnostics = CanonicalFamilies.diagnose(held.getItem());
        if (diagnostics.isEmpty()) {
            sendPrefixed(src, "No canonical family heuristic matched this item.");
            return 1;
        }

        CanonicalFamilies.FamilyDiagnostics diag = diagnostics.get();
        sendPrefixed(src, String.format(Locale.ROOT, "Family=%s material=%s", diag.family().key(), diag.material()));

        CanonicalAPI.resolve(held).ifPresent(match -> {
            if (match.canonicalMaterialId() != null && !Objects.equals(match.canonicalMaterialId(), match.materialId())) {
                sendPrefixed(src, "Canonical material=" + match.canonicalMaterialId());
            }
            if (match.hasCanonicalItem()) {
                boolean matchesHeld = match.canonicalItem() == held.getItem();
                sendPrefixed(src, String.format(Locale.ROOT, "Canonical item=%s matchesHeld=%s", match.canonicalItemId(), matchesHeld));
            } else {
                sendPrefixed(src, "Canonical item=<unresolved>");
            }
        });

        Item canonicalDrop = UnifyDataReload.resolveDrop(diag.material());
        if (canonicalDrop != null) {
            ResourceLocation dropId = BuiltInRegistries.ITEM.getKey(canonicalDrop);
            sendPrefixed(src, "Canonical drop item=" + dropId);
        }

        for (CanonicalFamilies.TagCheck tag : diag.tags()) {
            StringBuilder line = new StringBuilder();
            line.append("Tag ").append(tag.tagId()).append(": present=").append(tag.present());
            line.append(" entries=").append(tag.entryCount());
            line.append(" containsHeld=").append(tag.containsHeld());
            if (tag.hasCanonical()) {
                line.append(" canonical=").append(tag.canonicalId());
                line.append(" matchesHeld=").append(tag.canonicalMatches(held.getItem()));
            } else {
                line.append(" canonical=<missing>");
            }
            sendPrefixed(src, line.toString());
        }

        return 1;
    }

    private static int dumpWorldgen(CommandSourceStack src) {
        sendPrefixed(src, String.format(Locale.ROOT,
                "Worldgen enabled=%s overworld=%s nether=%s end=%s prune=%s sizePct=%d countPct=%d",
                UWWorldgenConfig.enabled(), UWWorldgenConfig.overworld(), UWWorldgenConfig.nether(),
                UWWorldgenConfig.end(), UWWorldgenConfig.prune(), UWWorldgenConfig.sizePct(),
                UWWorldgenConfig.countPct()));

        Registry<PlacedFeature> placedRegistry = src.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        List<String> unifyFeatures = placedRegistry.entrySet().stream()
                .map(Map.Entry::getKey)
                .map(ResourceKey::location)
                .filter(loc -> loc.getNamespace().equals(UnifyWorks.MODID))
                .map(ResourceLocation::toString)
                .sorted(Comparator.naturalOrder())
                .toList();
        sendList(src, "Placed features (" + unifyFeatures.size() + ")", unifyFeatures);
        return 1;
    }

    private static void sendPrefixed(CommandSourceStack src, String message) {
        src.sendSuccess(() -> Component.literal(PREFIX + message), false);
    }

    private static void sendList(CommandSourceStack src, String label, List<String> values) {
        if (values == null || values.isEmpty()) {
            sendPrefixed(src, label + ": (none)");
            return;
        }
        for (int i = 0; i < values.size(); i += LIST_CHUNK) {
            int end = Math.min(values.size(), i + LIST_CHUNK);
            String joined = String.join(", ", values.subList(i, end));
            String prefix = i == 0 ? label + ": " : "  ";
            sendPrefixed(src, prefix + joined);
        }
    }

    private UWCommands() {}
}
