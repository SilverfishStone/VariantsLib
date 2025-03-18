package net.silverfishstone.variantsdata.resourceish.entities.variants;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.VariantSelectorProvider;
import net.minecraft.entity.spawn.SpawnCondition;
import net.minecraft.entity.spawn.SpawnConditionSelectors;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.silverfishstone.variantsdata.util.VariantRegistryKeys;

import java.util.List;

public record SpiderVariant(Identifier id, AssetInfo assetInfo, SpawnConditionSelectors spawnConditions) implements VariantSelectorProvider<SpawnContext, SpawnCondition> {
    public static final Codec<SpiderVariant> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Identifier.CODEC.fieldOf("id").forGetter(SpiderVariant::id),
                            AssetInfo.MAP_CODEC.forGetter(SpiderVariant::assetInfo),
                            SpawnConditionSelectors.CODEC.fieldOf("spawn_conditions").forGetter(SpiderVariant::spawnConditions)
                    )
                    .apply(instance, SpiderVariant::new)
    );
    public static final Codec<SpiderVariant> NETWORK_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(Identifier.CODEC.fieldOf("id").forGetter(SpiderVariant::id),
                    AssetInfo.MAP_CODEC.forGetter(SpiderVariant::assetInfo)).apply(instance, SpiderVariant::new)
    );
    public static final Codec<RegistryEntry<SpiderVariant>> ENTRY_CODEC = RegistryFixedCodec.of(VariantRegistryKeys.SPIDER_VARIANT);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<SpiderVariant>> PACKET_CODEC = PacketCodecs.registryEntry(VariantRegistryKeys.SPIDER_VARIANT);

    private SpiderVariant(Identifier id, AssetInfo assetInfo) {
        this(id, assetInfo, SpawnConditionSelectors.EMPTY);
    }

    @Override
    public List<Selector<SpawnContext, SpawnCondition>> getSelectors() {
        return this.spawnConditions.selectors();
    }

    public static SpiderVariant fromJson(JsonObject json) {
        Identifier assetId = Identifier.of(JsonHelper.getString(json, "asset_id"));
        Identifier id = Identifier.of(JsonHelper.getString(json, "id"));
        JsonArray conditionsJson = JsonHelper.getArray(json, "spawn_conditions");
        SpawnConditionSelectors conditions = ZombieVariant.parseSpawnConditions(conditionsJson);
        return new SpiderVariant(id, new AssetInfo(assetId), conditions);
    }
}

