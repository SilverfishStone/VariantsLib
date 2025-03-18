package net.silverfishstone.variantsdata.resourceish.entities.variants.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.VariantSelectorProvider;
import net.minecraft.entity.spawn.SpawnConditionSelectors;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.silverfishstone.variantsdata.VariantsAndVariety;
import net.silverfishstone.variantsdata.datagen.VariantProvider;
import net.silverfishstone.variantsdata.datagen.VariantsGens;
import net.silverfishstone.variantsdata.resourceish.entities.variants.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class LoadVariantsHelper {
    public static final Map<Identifier, ZombieVariant> ZOMBIE_VARIANTS = new HashMap<>();
    public static final Map<Identifier, CreeperVariant> CREEPER_VARIANTS = new HashMap<>();
    public static final Map<Identifier, SkeletonVariant> SKELETON_VARIANTS = new HashMap<>();
    public static final Map<Identifier, SpiderVariant> SPIDER_VARIANTS = new HashMap<>();

    public static final Map<Identifier, JsonObject> ZOMBIE_VARIANT_JSON = new HashMap<>();
    public static final Map<Identifier, JsonObject> CREEPER_VARIANTS_JSON = new HashMap<>();
    public static final Map<Identifier, JsonObject> SKELETON_VARIANTS_JSON = new HashMap<>();
    public static final Map<Identifier, JsonObject> SPIDER_VARIANTS_JSON = new HashMap<>();

    private static final Map<Class<?>, Map<Identifier, ?>> VARIANT_MAPS = new HashMap<>();

    static {
        VARIANT_MAPS.put(ZombieVariant.class, ZOMBIE_VARIANTS);
        VARIANT_MAPS.put(CreeperVariant.class, CREEPER_VARIANTS);
        VARIANT_MAPS.put(SkeletonVariant.class, SKELETON_VARIANTS);
        VARIANT_MAPS.put(SpiderVariant.class, SPIDER_VARIANTS);
    }

    public static <T extends VariantSelectorProvider<SpawnContext, T> & VariantSelectorProvider.SelectorCondition<SpawnContext>> void registerVariant(RegistryKey<T> key, VariantProvider<T> provider, String assetId, SpawnConditionSelectors spawnConditions) {
        Class<T> variantType = provider.getVariantType();
        @SuppressWarnings("unchecked")
        Map<Identifier, T> variantMap = (Map<Identifier, T>) VARIANT_MAPS.computeIfAbsent(variantType, k -> new HashMap<>());
        T variant = provider.createVariant(key.getValue(), new AssetInfo(Identifier.ofVanilla(assetId)), spawnConditions);
        variantMap.put(key.getValue(), variant);
    }

    public static void registerJsonVariants() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.of(VariantsAndVariety.MOD_ID, "variant_loader");
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        // Clear existing variants
                        ZOMBIE_VARIANTS.clear();
                        CREEPER_VARIANTS.clear();
                        SKELETON_VARIANTS.clear();
                        SPIDER_VARIANTS.clear();

                        // Load variants for each entity type
                        try {
                            loadZVariants(manager, ZombieVariant::fromJson);
                        } catch (IOException e) {
                            throw new RuntimeException(e.getMessage());
                        }
                        try {
                            loadCVariants(manager, CreeperVariant::fromJson);
                        } catch (IOException e) {
                            VariantsAndVariety.LOGGER.error("Error in loadVariants");
                            throw new RuntimeException(e);

                        }
                        try {
                            loadSVariants(manager, SkeletonVariant::fromJson);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            loadKVariants(manager, SpiderVariant::fromJson);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    private static void loadZVariants(ResourceManager manager, Function<JsonObject, ZombieVariant> fromJson) throws IOException {
                        String path = "zombie_variant";
                        for (Identifier resourceId : manager.findResources(path, id -> id.getPath().endsWith(".json")).keySet()) {
                            Resource resource = manager.getResource(resourceId).orElseThrow();
                            InputStreamReader reader = new InputStreamReader(resource.getInputStream());
                            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                            Identifier variantId = Identifier.of(json.get("id").getAsString());
                            ZombieVariant variant = fromJson.apply(json);
                            ZOMBIE_VARIANTS.put(variantId, variant);
                            ZOMBIE_VARIANT_JSON.put(variantId, json);
                            VariantsAndVariety.LOGGER.info("put {} id and {} variant", variantId, variant);
                        }
                    }
                    private static void loadCVariants(ResourceManager manager, Function<JsonObject, CreeperVariant> fromJson) throws IOException {
                        String path = "creeper_variant";
                        for (Identifier resourceId : manager.findResources(path, id -> id.getPath().endsWith(".json")).keySet()) {
                            Resource resource = manager.getResource(resourceId).orElseThrow();
                            InputStreamReader reader = new InputStreamReader(resource.getInputStream());
                            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                            Identifier variantId = Identifier.of(json.get("id").getAsString());
                            CreeperVariant variant = fromJson.apply(json);
                            CREEPER_VARIANTS.put(variantId, variant);
                            CREEPER_VARIANTS_JSON.put(variantId, json);
                        }
                    }
                    private static void loadSVariants(ResourceManager manager, Function<JsonObject, SkeletonVariant> fromJson) throws IOException {
                        String path = "skeleton_variant";
                        for (Identifier resourceId : manager.findResources(path, id -> id.getPath().endsWith(".json")).keySet()) {
                            Resource resource = manager.getResource(resourceId).orElseThrow();
                            InputStreamReader reader = new InputStreamReader(resource.getInputStream());
                            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                            Identifier variantId = Identifier.of(json.get("id").getAsString());
                            SkeletonVariant variant = fromJson.apply(json);
                            SKELETON_VARIANTS.put(variantId, variant);
                            SKELETON_VARIANTS_JSON.put(variantId, json);
                        }
                    }
                    private static void loadKVariants(ResourceManager manager, Function<JsonObject, SpiderVariant> fromJson) throws IOException {
                        String path = "spider_variant";
                        for (Identifier resourceId : manager.findResources(path, id -> id.getPath().endsWith(".json")).keySet()) {
                            Resource resource = manager.getResource(resourceId).orElseThrow();
                            InputStreamReader reader = new InputStreamReader(resource.getInputStream());
                            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                            Identifier variantId = Identifier.of(json.get("id").getAsString());
                            SpiderVariant variant = fromJson.apply(json);
                            SPIDER_VARIANTS.put(variantId, variant);
                            SPIDER_VARIANTS_JSON.put(variantId, json);
                        }
                    }
                }
        );
    }

    public static CreeperVariant getCreeperVariant(Identifier variantId) {
        return CREEPER_VARIANTS.get(variantId);
    }

    public static ZombieVariant getZombieVariant(Identifier variantId) {
        return ZOMBIE_VARIANTS.get(variantId);
    }
    public static SkeletonVariant getSkeletonVariant(Identifier variantId) {
        return SKELETON_VARIANTS.get(variantId);
    }
    public static SpiderVariant getSpiderVariant(Identifier variantId) {
        return SPIDER_VARIANTS.get(variantId);
    }
}
