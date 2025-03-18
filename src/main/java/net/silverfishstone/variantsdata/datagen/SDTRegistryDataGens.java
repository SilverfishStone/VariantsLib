package net.silverfishstone.variantsdata.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.RegistryWrapper;
import net.silverfishstone.variantsdata.util.VariantRegistryKeys;

import java.util.concurrent.CompletableFuture;

public class SDTRegistryDataGens extends FabricDynamicRegistryProvider {
    public SDTRegistryDataGens(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);

    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        entries.addAll(registries.getOrThrow(VariantRegistryKeys.ZOMBIE_VARIANT));
        entries.addAll(registries.getOrThrow(VariantRegistryKeys.CREEPER_VARIANT));
        entries.addAll(registries.getOrThrow(VariantRegistryKeys.SKELETON_VARIANT));
        entries.addAll(registries.getOrThrow(VariantRegistryKeys.SPIDER_VARIANT));
    }

    @Override
    public String getName() {
        return "";
    }
}