package net.silverfishstone.variantsdata.datagen;

import net.minecraft.entity.spawn.SpawnConditionSelectors;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
import net.silverfishstone.variantsdata.VariantsAndVariety;
import net.silverfishstone.variantsdata.resourceish.entities.variants.*;
import net.silverfishstone.variantsdata.util.VariantRegistryKeys;

public class VariantsGens {
    public static class ZombieVariantProvider extends VariantProvider<ZombieVariant> {
        public static final RegistryKey<ZombieVariant> DEFAULT = new ZombieVariantProvider(VariantsAndVariety.MOD_ID).of("default");

        public ZombieVariantProvider(String modId) {
            super(modId);
        }

        @Override
        protected RegistryKey<net.minecraft.registry.Registry<ZombieVariant>> getRegistryKey() {
            return VariantRegistryKeys.ZOMBIE_VARIANT;
        }

        @Override
        public ZombieVariant createVariant(Identifier id, AssetInfo assetInfo, SpawnConditionSelectors spawnConditions) {
            return new ZombieVariant(id, assetInfo, spawnConditions);
        }

        @Override
        protected void registerDefaultVariants(Registerable<ZombieVariant> registry) {
            register(registry, DEFAULT, "entity/zombie/zombie", SpawnConditionSelectors.createFallback(0));
        }

        @Override
        public Class<ZombieVariant> getVariantType() {
            return ZombieVariant.class;
        }
    }

    public static class CreeperVariantProvider extends VariantProvider<CreeperVariant> {
        public static final RegistryKey<CreeperVariant> DEFAULT = new CreeperVariantProvider(VariantsAndVariety.MOD_ID).of("default");

        public CreeperVariantProvider(String modId) {
            super(modId);
        }

        @Override
        protected RegistryKey<net.minecraft.registry.Registry<CreeperVariant>> getRegistryKey() {
            return VariantRegistryKeys.CREEPER_VARIANT;
        }

        @Override
        public CreeperVariant createVariant(Identifier id, AssetInfo assetInfo, SpawnConditionSelectors spawnConditions) {
            return new CreeperVariant(id, assetInfo, spawnConditions);
        }

        @Override
        protected void registerDefaultVariants(Registerable<CreeperVariant> registry) {
            register(registry, DEFAULT, "entity/creeper/creeper", SpawnConditionSelectors.createFallback(0));
        }

        @Override
        public Class<CreeperVariant> getVariantType() {
            return CreeperVariant.class;
        }

    }

    public static class SkeletonVariantProvider extends VariantProvider<SkeletonVariant> {
        public static final RegistryKey<SkeletonVariant> DEFAULT = new SkeletonVariantProvider(VariantsAndVariety.MOD_ID).of("default");

        public SkeletonVariantProvider(String modId) {
            super(modId);
        }

        @Override
        protected RegistryKey<net.minecraft.registry.Registry<SkeletonVariant>> getRegistryKey() {
            return VariantRegistryKeys.SKELETON_VARIANT;
        }

        @Override
        public SkeletonVariant createVariant(Identifier id, AssetInfo assetInfo, SpawnConditionSelectors spawnConditions) {
            return new SkeletonVariant(id, assetInfo, spawnConditions);
        }

        @Override
        protected void registerDefaultVariants(Registerable<SkeletonVariant> registry) {
            register(registry, DEFAULT, "entity/skeleton/skeleton", SpawnConditionSelectors.createFallback(0));
        }

        @Override
        public Class<SkeletonVariant> getVariantType() {
            return SkeletonVariant.class;
        }

    }

    public static class SpiderVariantProvider extends VariantProvider<SpiderVariant> {
        public static final RegistryKey<SpiderVariant> DEFAULT = new SpiderVariantProvider(VariantsAndVariety.MOD_ID).of("default");

        public SpiderVariantProvider(String modId) {
            super(modId);
        }

        @Override
        protected RegistryKey<Registry<SpiderVariant>> getRegistryKey() {
            return VariantRegistryKeys.SPIDER_VARIANT;
        }

        @Override
        public SpiderVariant createVariant(Identifier id, AssetInfo assetInfo, SpawnConditionSelectors spawnConditions) {
            return new SpiderVariant(id, assetInfo, spawnConditions);
        }

        @Override
        protected void registerDefaultVariants(Registerable<SpiderVariant> registry) {
            register(registry, DEFAULT, "entity/spider/spider", SpawnConditionSelectors.createFallback(0));
        }

        @Override
        public Class<SpiderVariant> getVariantType() {
            return SpiderVariant.class;
        }
    }
}
