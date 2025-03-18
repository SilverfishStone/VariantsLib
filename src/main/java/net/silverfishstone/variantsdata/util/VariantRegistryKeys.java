package net.silverfishstone.variantsdata.util;

import net.minecraft.registry.*;
import net.minecraft.util.Identifier;
import net.silverfishstone.variantsdata.resourceish.entities.variants.*;

public class VariantRegistryKeys {
    public static final RegistryKey<Registry<ZombieVariant>> ZOMBIE_VARIANT = RegistryKey.ofRegistry(Identifier.of("zombie_variant"));
    public static final RegistryKey<Registry<CreeperVariant>> CREEPER_VARIANT = RegistryKey.ofRegistry(Identifier.of("creeper_variant"));
    public static final RegistryKey<Registry<SkeletonVariant>> SKELETON_VARIANT = RegistryKey.ofRegistry(Identifier.of("skeleton_variant"));
    public static final RegistryKey<Registry<SpiderVariant>> SPIDER_VARIANT = RegistryKey.ofRegistry(Identifier.of("spider_variant"));
}

