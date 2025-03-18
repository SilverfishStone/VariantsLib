package net.silverfishstone.variantsdata.mixin;

import io.netty.util.internal.UnstableApi;
import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.state.CreeperEntityRenderState;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.Identifier;
import net.silverfishstone.variantsdata.resourceish.entities.render.CustomTexturedEntity;
import net.silverfishstone.variantsdata.resourceish.entities.render.CustomTexturedRenderState;
import net.silverfishstone.variantsdata.resourceish.entities.variants.CreeperVariant;
import net.silverfishstone.variantsdata.resourceish.entities.variants.data.LoadVariantsHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreeperEntityRenderer.class)
public class CreeperRendererMixin {

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/mob/CreeperEntity;Lnet/minecraft/client/render/entity/state/CreeperEntityRenderState;F)V", at = @At("TAIL"))
    private void injectCustomTextureIntoRenderState(CreeperEntity entity, CreeperEntityRenderState state, float f, CallbackInfo ci) {
        if (entity instanceof CustomTexturedEntity texturedEntity && state instanceof CustomTexturedRenderState texturedState) {
            CreeperVariant variant = LoadVariantsHelper.getCreeperVariant(texturedEntity.getvariantId());
            Identifier texture = variant != null ? variant.assetInfo().texturePath() : Identifier.ofVanilla("zombie");
            texturedState.setCustomTexture(texturedEntity.getvariantId());
        }
    }

    @Inject(at = @At("HEAD"), method = "getTexture(Lnet/minecraft/client/render/entity/state/CreeperEntityRenderState;)Lnet/minecraft/util/Identifier;", cancellable = true)
    protected void getTexture(CreeperEntityRenderState creeperEntityRenderState, CallbackInfoReturnable<Identifier> cir) {
        if (creeperEntityRenderState instanceof CustomTexturedRenderState texturedState) {
            String customTexture = texturedState.getCustomTexture().toString();
            Identifier textureId = Identifier.of(customTexture);
            cir.setReturnValue(textureId);
            cir.cancel();
        }
    }
}
