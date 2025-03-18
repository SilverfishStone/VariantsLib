package net.silverfishstone.variantsdata;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.minecraft.client.session.telemetry.WorldLoadedEvent;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.silverfishstone.variantsdata.datagen.VariantProvider;
import net.silverfishstone.variantsdata.datagen.VariantsGens;
import net.silverfishstone.variantsdata.resourceish.entities.variants.data.LoadVariantsHelper;
import net.silverfishstone.variantsdata.util.VariantDataComponentTypes;
import net.silverfishstone.variantsdata.util.VariantRegistryKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariantsAndVariety implements ModInitializer {
	public static final Random random = Random.create();
	public static final String MOD_ID = "variantsdata";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static RegistryEntryLookup<Biome> registryLookup;

	@Override
	public void onInitialize() {
		VariantProvider.registerRegistries();
		VariantDataComponentTypes.register();
		LoadVariantsHelper.registerJsonVariants();
	}

	private void setRegistryLookup (MinecraftServer serverWorld) {
		registryLookup = serverWorld.getReloadableRegistries().createRegistryLookup().getOrThrow(RegistryKeys.BIOME);
		LOGGER.info(serverWorld.getReloadableRegistries().toString());
	}
}