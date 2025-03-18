package net.silverfishstone.variantsdata.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.spawn.SpawnConditionSelectors;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.silverfishstone.variantsdata.VariantsAndVariety;
import net.silverfishstone.variantsdata.datagen.VariantProvider;
import net.silverfishstone.variantsdata.datagen.VariantsGens;
import net.silverfishstone.variantsdata.resourceish.entities.render.CustomTexturedEntity;
import net.silverfishstone.variantsdata.resourceish.entities.variants.SkeletonVariant;
import net.silverfishstone.variantsdata.resourceish.entities.variants.ZombieVariant;
import net.silverfishstone.variantsdata.resourceish.entities.variants.data.LoadVariantsHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(SkeletonEntity.class)
public class SkeletonEntityMixin extends HostileEntity implements CustomTexturedEntity<SkeletonVariant> {
    private static final TrackedData<String> VARIANT = DataTracker.registerData(SkeletonEntityMixin.class, TrackedDataHandlerRegistry.STRING);

    public SkeletonEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }


    @Inject(at = @At("HEAD"), method = "initDataTracker")
    protected void initDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        RegistryKey<SkeletonVariant> defaultKey = VariantsGens.SkeletonVariantProvider.DEFAULT;
        String defaultVariantId = defaultKey.getValue().toString();
        builder.add(VARIANT, defaultVariantId);
    }

    public void setVariant(RegistryEntry<SkeletonVariant> variant) {
        this.dataTracker.set(VARIANT, variant.getIdAsString());
    }
    public void setVariant(Identifier variant) {
        this.dataTracker.set(VARIANT, variant.toString());
    }

    @Inject(at = @At("HEAD"), method = "writeCustomDataToNbt")
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putString("Variant", this.dataTracker.get(VARIANT));
    }

    @Inject(at = @At("HEAD"), method = "readCustomDataFromNbt")
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Variant")) {
            this.setVariant(Identifier.of(nbt.getString("Variant").orElse("template")));
        }
    }



    @Override
    public Identifier getvariantId() {
        SkeletonVariant wolfVariant = this.getVariant().value();
        return wolfVariant.assetInfo().texturePath();
    }

    public RegistryEntry<SkeletonVariant> getVariant() {
        String variantId = this.getDataTracker().get(VARIANT);
        SkeletonVariant variant = LoadVariantsHelper.getSkeletonVariant(Identifier.of(variantId));
        if (variant == null) {
            variant = LoadVariantsHelper.getSkeletonVariant(Identifier.of("default"));
            if (variant == null) {
                variant = new SkeletonVariant(Identifier.of("default"),
                        new AssetInfo(Identifier.ofVanilla( "entity/skeleton/skeleton")),
                        SpawnConditionSelectors.createFallback(0)
                );
            }
        }
        return RegistryEntry.of(variant);
    }

    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        String biomeAtPos = world.getBiome(this.getBlockPos()).getIdAsString();

        // Find the first matching variant and stop
        boolean variantSet = false;
        for (Map.Entry<Identifier, JsonObject> entry : LoadVariantsHelper.SKELETON_VARIANTS_JSON.entrySet()) {
            Identifier variantId = Identifier.of(JsonHelper.getString(entry.getValue(), "id"));
            List<String> biomes = parseBiomes(JsonHelper.getArray(entry.getValue(), "spawn_conditions"));
            if (biomes.contains(biomeAtPos)) {
                this.setVariant(variantId);
                variantSet = true;
                break; // Stop after first match
            }
        }

        // If no match found, keep the default (set in initDataTracker)
        if (!variantSet) {
            VariantProvider.selectSkeleton(this.random, this.getRegistryManager(), SpawnContext.of(world, this.getBlockPos())).ifPresent(this::setVariant);
        }
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Unique
    private static List<String> parseBiomes(JsonArray array) {
        List<String> list = new ArrayList<>();
        if (array.isEmpty()) {
            return list;
        }
        for (int i = 0; i < array.size(); i++) {
            JsonObject conditionJson = array.get(i).getAsJsonObject();
            if (conditionJson.has("condition")) {
                JsonObject conditionB = JsonHelper.getObject(conditionJson, "condition");
                String type = JsonHelper.getString(conditionB, "type");
                if (type.equals("minecraft:biome")) {
                    JsonElement biomesElement = conditionB.get("biomes");
                    if (biomesElement.isJsonArray()) {
                        for (JsonElement biome : biomesElement.getAsJsonArray()) {
                            list.add(biome.getAsString());
                        }
                    } else {
                        list.add(biomesElement.getAsString());
                    }
                }
            }
        }
        return list;
    }

    @Override
    public void setCustomTexture(String textureId) {
        this.getDataTracker().set(VARIANT, textureId);
    }
















    public String getTexture() {
        String string = "";
        // Example: Read the JSON file when the mod initializes
        try {
            String filePath = "src/main/resources/data/springdropstweaks/zombie_variant/template.json";
            // Read the contents of the JSON file
            String jsonData = new String(Files.readAllBytes(Paths.get(filePath)));

            JsonObject jsonObject = (JsonObject) JsonParser.parseReader(new FileReader(filePath));

            if (jsonObject != null) {
                // Access a specific value, e.g., "speed"
                VariantsAndVariety.LOGGER.info("Zombie variant asset: texture");
                return jsonObject.get("asset_id").getAsString();
            }
        } catch (Exception e) {
            VariantsAndVariety.LOGGER.error("Failed to read JSON file: {}", e.getMessage());
        }
        return string;
    }



    private JsonObject readJsonFromFile(String path) {
        // Create an Identifier for the JSON file
        Identifier resourceId = Identifier.of(VariantsAndVariety.MOD_ID, path);

        // Get the ResourceManager (this requires a server context, see note below)
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        try {
            // Load the resource
            Resource resource = resourceManager.getResource(resourceId).orElseThrow(() -> new IllegalStateException("Resource not found: " + resourceId));
            try (InputStream stream = resource.getInputStream();
                 InputStreamReader reader = new InputStreamReader(stream)) {
                // Parse the JSON content
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (Exception e) {
            VariantsAndVariety.LOGGER.error("Error reading JSON file {}: {}", resourceId, e.getMessage());
            return null;
        }
    }

}