package net.silverfishstone.variantsdata.datagen;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.entity.VariantSelectorProvider;
import net.minecraft.entity.spawn.BiomeSpawnCondition;
import net.minecraft.entity.spawn.SpawnConditionSelectors;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.silverfishstone.variantsdata.resourceish.entities.variants.*;
import net.silverfishstone.variantsdata.util.VariantRegistryKeys;

import java.util.*;

public abstract class VariantProvider<T> {
    protected final String modId; // Mod ID for Identifier creation
    private final List<RegistryKey<T>> variantKeys = new ArrayList<>();
    public final Map<Identifier, T> variantData = new HashMap<>();// Dynamic list of variant keys
    private static final List<VariantProvider<?>> ALL_PROVIDERS = new ArrayList<>();


    public VariantProvider(String modId) {
        this.modId = modId;
        synchronized (ALL_PROVIDERS) {
            ALL_PROVIDERS.add(this);
        }
    }

    // Abstract methods for subclasses to define
    protected abstract RegistryKey<net.minecraft.registry.Registry<T>> getRegistryKey();
    public abstract T createVariant(Identifier id, AssetInfo assetInfo, SpawnConditionSelectors spawnConditions);

    // Create a RegistryKey with the given ID
    protected RegistryKey<T> of(String id) {
        RegistryKey<T> key = RegistryKey.of(getRegistryKey(), Identifier.of(modId, id));
        variantKeys.add(key); // Add to dynamic list
        return key;
    }

    // Register all variants (called by bootstrap)
    public void bootstrap(Registerable<T> registry) {
        registerDefaultVariants(registry);
        registerAdditionalVariants(registry); // Allow subclasses to add more
    }

    // Subclasses must define their default variants
    protected abstract void registerDefaultVariants(Registerable<T> registry);

    public static List<VariantProvider<?>> getAllProviders() {
        synchronized (ALL_PROVIDERS) {
            return new ArrayList<>(ALL_PROVIDERS);
        }
    }

    // Subclasses can override this to add more variants dynamically
    protected void registerAdditionalVariants(Registerable<T> registry) {
        // Default implementation does nothing; override in subclasses if needed
    }

    // Common registration method with biome key
    protected void register(Registerable<T> registry, RegistryKey<T> key, String assetId, RegistryKey<Biome> requiredBiomes) {
        RegistryEntryList<Biome> registryEntryList = RegistryEntryList.of(registry.getRegistryLookup(RegistryKeys.BIOME).getOrThrow(requiredBiomes));
        register(registry, key, assetId, SpawnConditionSelectors.createSingle(new BiomeSpawnCondition(registryEntryList), 1));
    }

    // Common registration method with biome tag
    protected void register(Registerable<T> registry, RegistryKey<T> key, String assetId, TagKey<Biome> requiredBiomes) {
        RegistryEntryList<Biome> registryEntryList = registry.getRegistryLookup(RegistryKeys.BIOME).getOrThrow(requiredBiomes);
        register(registry, key, assetId, SpawnConditionSelectors.createSingle(new BiomeSpawnCondition(registryEntryList), 1));
    }

    // Common registration method with spawn conditions
    protected void register(Registerable<T> registry, RegistryKey<T> key, String assetId, SpawnConditionSelectors spawnConditions) {
        T variant = createVariant(key.getValue(), new AssetInfo(Identifier.ofVanilla(assetId)), spawnConditions);
        registry.register(key, variant);
        variantData.put(key.getValue(), variant);
    }


    // Method to add a new variant dynamically
    public void addVariant(Registerable<T> registry, String id, String assetId, SpawnConditionSelectors spawnConditions) {
        RegistryKey<T> key = of(id);
        register(registry, key, assetId, spawnConditions);
    }

    // Getter for the list of registered variant keys (optional)
    public List<RegistryKey<T>> getVariantKeys() {
        return new ArrayList<>(variantKeys); // Return a copy to prevent external modification
    }

    public static Optional<RegistryEntry.Reference<SkeletonVariant>> selectSkeleton(Random random, DynamicRegistryManager registries, SpawnContext context) {
        return VariantSelectorProvider.select(registries.getOrThrow(VariantRegistryKeys.SKELETON_VARIANT).streamEntries(), RegistryEntry::value, random, context);
    }
    public static Optional<RegistryEntry.Reference<CreeperVariant>> selectCreeper(Random random, DynamicRegistryManager registries, SpawnContext context) {
        return VariantSelectorProvider.select(registries.getOrThrow(VariantRegistryKeys.CREEPER_VARIANT).streamEntries(), RegistryEntry::value, random, context);
    }
    public static Optional<RegistryEntry.Reference<ZombieVariant>> selectZombie(Random random, DynamicRegistryManager registries, SpawnContext context) {
        return VariantSelectorProvider.select(registries.getOrThrow(VariantRegistryKeys.ZOMBIE_VARIANT).streamEntries(), RegistryEntry::value, random, context);
    }
    public static Optional<RegistryEntry.Reference<SpiderVariant>> selectSpider(Random random, DynamicRegistryManager registries, SpawnContext context) {
        return VariantSelectorProvider.select(registries.getOrThrow(VariantRegistryKeys.SPIDER_VARIANT).streamEntries(), RegistryEntry::value, random, context);
    }

    public static void registerRegistries () {
        DynamicRegistries.register(VariantRegistryKeys.ZOMBIE_VARIANT, ZombieVariant.CODEC);
        DynamicRegistries.register(VariantRegistryKeys.CREEPER_VARIANT, CreeperVariant.CODEC);
        DynamicRegistries.register(VariantRegistryKeys.SKELETON_VARIANT, SkeletonVariant.CODEC);
        DynamicRegistries.register(VariantRegistryKeys.SPIDER_VARIANT, SpiderVariant.CODEC);
    }

    public Map<Identifier, T> getVariantData() {
        return new HashMap<>(variantData); // Return a copy
    }

    public abstract Class<T> getVariantType();
}