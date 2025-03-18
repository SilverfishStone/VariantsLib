package net.silverfishstone.variantsdata.resourceish.entities.variants;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.VariantSelectorProvider;
import net.minecraft.entity.spawn.BiomeSpawnCondition;
import net.minecraft.entity.spawn.SpawnCondition;
import net.minecraft.entity.spawn.SpawnConditionSelectors;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.biome.Biome;
import net.silverfishstone.variantsdata.VariantsAndVariety;
import net.silverfishstone.variantsdata.util.VariantRegistryKeys;

import java.util.ArrayList;
import java.util.List;


public record ZombieVariant(Identifier id, AssetInfo assetInfo, SpawnConditionSelectors spawnConditions) implements VariantSelectorProvider<SpawnContext, SpawnCondition> {
    public static final Codec<ZombieVariant> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Identifier.CODEC.fieldOf("id").forGetter(ZombieVariant::id),
                            AssetInfo.MAP_CODEC.forGetter(ZombieVariant::assetInfo),
                            SpawnConditionSelectors.CODEC.fieldOf("spawn_conditions").forGetter(ZombieVariant::spawnConditions)
                    )
                    .apply(instance, ZombieVariant::new)
    );
    public static final Codec<ZombieVariant> NETWORK_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(Identifier.CODEC.fieldOf("id").forGetter(ZombieVariant::id),
                    AssetInfo.MAP_CODEC.forGetter(ZombieVariant::assetInfo)).apply(instance, ZombieVariant::new)
    );
    public static final Codec<RegistryEntry<ZombieVariant>> ENTRY_CODEC = RegistryFixedCodec.of(VariantRegistryKeys.ZOMBIE_VARIANT);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<ZombieVariant>> PACKET_CODEC = PacketCodecs.registryEntry(VariantRegistryKeys.ZOMBIE_VARIANT);

    private ZombieVariant(Identifier id, AssetInfo assetInfo) {
        this(id, assetInfo, SpawnConditionSelectors.EMPTY);
    }

    @Override
    public List<Selector<SpawnContext, SpawnCondition>> getSelectors() {
        return this.spawnConditions.selectors();
    }

    public static ZombieVariant fromJson(JsonObject json) {
        Identifier assetId = Identifier.of(JsonHelper.getString(json, "asset_id"));
        Identifier id = Identifier.of(JsonHelper.getString(json, "id"));
        JsonArray conditionsJson = JsonHelper.getArray(json, "spawn_conditions");
        SpawnConditionSelectors conditions = parseSpawnConditions(conditionsJson);
        return new ZombieVariant(id, new AssetInfo(assetId), conditions);
    }

    // Parse spawn_conditions array using SpawnConditionSelectors factory methods
    public static SpawnConditionSelectors parseSpawnConditions(JsonArray array) {
        if (array.isEmpty()) {
            return SpawnConditionSelectors.EMPTY;
        }

        List<Selector<SpawnContext, SpawnCondition>> selectors = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JsonObject conditionJson = array.get(i).getAsJsonObject();
            int priority = JsonHelper.getInt(conditionJson, "priority", 0);


            // Check if a specific condition type is provided (e.g., biome)
            if (conditionJson.has("condition")) {
                JsonObject conditionB = JsonHelper.getObject(conditionJson, "condition");
                String type = JsonHelper.getString(conditionB, "type");

                if (type.equals("minecraft:biome") && VariantsAndVariety.registryLookup != null) {
                    JsonElement biomesElement = conditionB.get("biomes");
                    List<RegistryKey<Biome>> biomeIds = new ArrayList<>();

                    if (biomesElement.isJsonArray()) {
                        // Handle array of biomes
                        for (JsonElement biome : biomesElement.getAsJsonArray()) {
                            biomeIds.add(RegistryKey.of(RegistryKeys.BIOME, Identifier.of(biome.getAsString())));
                        }
                    } else {
                        // Handle single biome
                        biomeIds.add(RegistryKey.of(RegistryKeys.BIOME, Identifier.of(biomesElement.getAsString())));
                    }

                    RegistryEntryList<Biome> biomeEntries = createBiomeEntryList(biomeIds);
                    BiomeSpawnCondition condition = new BiomeSpawnCondition(biomeEntries);
                    selectors.addAll(SpawnConditionSelectors.createSingle(condition, priority).selectors());
                } else {
                    // Add other condition types here if needed
                    selectors.addAll(SpawnConditionSelectors.createFallback(priority).selectors());
                }
            } else {
                // Default to fallback if no specific condition type is specified
                selectors.addAll(SpawnConditionSelectors.createFallback(priority).selectors());
            }
        }
        return new SpawnConditionSelectors(selectors);
    }

    private static RegistryEntryList<Biome> createBiomeEntryList(List<RegistryKey<Biome>> biomeIds) {
        RegistryEntryLookup<Biome> biomeRegistry = VariantsAndVariety.registryLookup;
        List<RegistryEntry<Biome>> entries = biomeIds.stream()
                .map(key -> (RegistryEntry<Biome>) biomeRegistry.getOrThrow(key)) // Cast Reference<Biome> to RegistryEntry<Biome>
                .toList();
        return RegistryEntryList.of(entries);
    }
}
