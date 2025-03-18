package net.silverfishstone.variantsdata.mixin;

import net.minecraft.client.render.entity.AbstractSkeletonEntityRenderer;
import net.minecraft.client.render.entity.state.SkeletonEntityRenderState;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.util.Identifier;
import net.silverfishstone.variantsdata.resourceish.entities.render.CustomTexturedEntity;
import net.silverfishstone.variantsdata.resourceish.entities.render.CustomTexturedRenderState;
import net.silverfishstone.variantsdata.resourceish.entities.variants.SkeletonVariant;
import net.silverfishstone.variantsdata.resourceish.entities.variants.data.LoadVariantsHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeletonEntityRenderer.class)
public class AbstractSkeletonRendererMixin {

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/mob/AbstractSkeletonEntity;Lnet/minecraft/client/render/entity/state/SkeletonEntityRenderState;F)V", at = @At("TAIL"))
    private void injectCustomTextureIntoRenderState(AbstractSkeletonEntity entity, SkeletonEntityRenderState state, float f, CallbackInfo ci) {
        if (entity instanceof CustomTexturedEntity texturedEntity && state instanceof CustomTexturedRenderState texturedState) {
            SkeletonVariant variant = LoadVariantsHelper.getSkeletonVariant(texturedEntity.getvariantId());
            Identifier texture = variant != null ? variant.assetInfo().texturePath() : Identifier.ofVanilla("zombie");
            texturedState.setCustomTexture(texturedEntity.getvariantId());
        }
    }
}
