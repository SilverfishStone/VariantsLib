package net.silverfishstone.variantsdata;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.*;
import net.silverfishstone.variantsdata.datagen.*;
import net.silverfishstone.variantsdata.util.VariantRegistryKeys;

public class VariantsAndVarietyDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(SDTRegistryDataGens::new);
	}

	@Override
	public void buildRegistry(RegistryBuilder registryBuilder) {
		DataGeneratorEntrypoint.super.buildRegistry(registryBuilder);
		registryBuilder.addRegistry(VariantRegistryKeys.ZOMBIE_VARIANT, new VariantsGens.ZombieVariantProvider(VariantsAndVariety.MOD_ID)::bootstrap);
		registryBuilder.addRegistry(VariantRegistryKeys.CREEPER_VARIANT, new VariantsGens.CreeperVariantProvider(VariantsAndVariety.MOD_ID)::bootstrap);
		registryBuilder.addRegistry(VariantRegistryKeys.SKELETON_VARIANT, new VariantsGens.SkeletonVariantProvider(VariantsAndVariety.MOD_ID)::bootstrap);
		registryBuilder.addRegistry(VariantRegistryKeys.SPIDER_VARIANT, new VariantsGens.SpiderVariantProvider(VariantsAndVariety.MOD_ID)::bootstrap);


	}
}
