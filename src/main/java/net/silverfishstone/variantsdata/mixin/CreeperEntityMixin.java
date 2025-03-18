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
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
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
import net.silverfishstone.variantsdata.resourceish.entities.variants.CreeperVariant;
import net.silverfishstone.variantsdata.resourceish.entities.variants.SkeletonVariant;
import net.silverfishstone.variantsdata.resourceish.entities.variants.data.LoadVariantsHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(CreeperEntity.class)
public class CreeperEntityMixin extends HostileEntity implements CustomTexturedEntity<CreeperVariant> {
    private static final TrackedData<String> VARIANT = DataTracker.registerData(CreeperEntityMixin.class, TrackedDataHandlerRegistry.STRING);

    public CreeperEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }


    @Inject(at = @At("HEAD"), method = "initDataTracker")
    protected void initDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        RegistryKey<CreeperVariant> defaultKey = VariantsGens.CreeperVariantProvider.DEFAULT;
        String defaultVariantId = defaultKey.getValue().toString();
        builder.add(VARIANT, defaultVariantId);
    }

    public void setVariant(RegistryEntry<CreeperVariant> variant) {
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
        CreeperVariant wolfVariant = this.getVariant().value();
        return wolfVariant.assetInfo().texturePath();
    }

    public RegistryEntry<CreeperVariant> getVariant() {
        String variantId = this.getDataTracker().get(VARIANT);
        CreeperVariant variant = LoadVariantsHelper.getCreeperVariant(Identifier.of(variantId));
        if (variant == null) {
            variant = LoadVariantsHelper.getCreeperVariant(Identifier.of("default"));
            if (variant == null) {
                variant = new CreeperVariant(Identifier.of("default"),
                        new AssetInfo(Identifier.ofVanilla("entity/creeper/creeper")),
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
        for (Map.Entry<Identifier, JsonObject> entry : LoadVariantsHelper.CREEPER_VARIANTS_JSON.entrySet()) {
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
            VariantProvider.selectCreeper(this.random, this.getRegistryManager(), SpawnContext.of(world, this.getBlockPos())).ifPresent(this::setVariant);
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
}