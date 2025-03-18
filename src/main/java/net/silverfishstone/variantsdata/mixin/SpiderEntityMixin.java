package net.silverfishstone.variantsdata.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.spawn.SpawnConditionSelectors;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
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
import net.silverfishstone.variantsdata.resourceish.entities.variants.SpiderVariant;
import net.silverfishstone.variantsdata.resourceish.entities.variants.ZombieVariant;
import net.silverfishstone.variantsdata.resourceish.entities.variants.data.LoadVariantsHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(SpiderEntity.class)
public class SpiderEntityMixin extends HostileEntity implements CustomTexturedEntity<SpiderVariant> {
    private static final TrackedData<String> VARIANT = DataTracker.registerData(SpiderEntityMixin.class, TrackedDataHandlerRegistry.STRING);

    public SpiderEntityMixin(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }


    @Inject(at = @At("HEAD"), method = "initDataTracker")
    protected void initDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        RegistryKey<SpiderVariant> defaultKey = VariantsGens.SpiderVariantProvider.DEFAULT;
        String defaultVariantId = defaultKey.getValue().toString();
        builder.add(VARIANT, defaultVariantId);
    }

    public void setVariant(RegistryEntry<SpiderVariant> variant) {
        this.dataTracker.set(VARIANT, variant.getIdAsString());
    }
    public void setVariant(Identifier variant) {
        this.dataTracker.set(VARIANT, variant.toString());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("Variant", this.dataTracker.get(VARIANT));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Variant")) {
            this.setVariant(Identifier.of(nbt.getString("Variant").orElse("template")));
        }
    }



    @Override
    public Identifier getvariantId() {
        SpiderVariant wolfVariant = this.getVariant().value();
        return wolfVariant.assetInfo().texturePath();
    }

    @Inject(method = "initialize(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/world/LocalDifficulty;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/entity/EntityData;)Lnet/minecraft/entity/EntityData;", at = @At("HEAD"))
    private void initializeVariantFromBiome(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, net.minecraft.entity.EntityData entityData, CallbackInfoReturnable<net.minecraft.entity.EntityData> cir) {
        String biomeAtPos = world.getBiome(this.getBlockPos()).getIdAsString();
        VariantsAndVariety.LOGGER.info("Spawn biome: " + biomeAtPos);

        // Find the first matching variant and stop
        boolean variantSet = false;
        for (Map.Entry<Identifier, JsonObject> entry : LoadVariantsHelper.SPIDER_VARIANTS_JSON.entrySet()) {
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
            VariantProvider.selectSpider(this.random, this.getRegistryManager(), SpawnContext.of(world, this.getBlockPos())).ifPresent(this::setVariant);
        }
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


    public RegistryEntry<SpiderVariant> getVariant() {
        String variantId = this.getDataTracker().get(VARIANT);
        SpiderVariant variant = LoadVariantsHelper.getSpiderVariant(Identifier.of(variantId));
        if (variant == null) {
            variant = LoadVariantsHelper.getSpiderVariant(Identifier.of("default"));
            if (variant == null) {
                variant = new SpiderVariant(Identifier.of("default"),
                        new AssetInfo(Identifier.ofVanilla("entity/spider/spider")),
                        SpawnConditionSelectors.createFallback(0)
                );
            }
        }
        return RegistryEntry.of(variant);
    }
}