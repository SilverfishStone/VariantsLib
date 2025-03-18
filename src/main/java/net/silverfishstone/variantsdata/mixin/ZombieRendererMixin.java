package net.silverfishstone.variantsdata.mixin;

import net.minecraft.client.render.entity.ZombieBaseEntityRenderer;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;
import net.silverfishstone.variantsdata.resourceish.entities.render.CustomTexturedEntity;
import net.silverfishstone.variantsdata.resourceish.entities.render.CustomTexturedRenderState;
import net.silverfishstone.variantsdata.resourceish.entities.variants.ZombieVariant;
import net.silverfishstone.variantsdata.resourceish.entities.variants.data.LoadVariantsHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieBaseEntityRenderer.class)
public class ZombieRendererMixin {

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/mob/ZombieEntity;Lnet/minecraft/client/render/entity/state/ZombieEntityRenderState;F)V", at = @At("TAIL"))
    private void injectCustomTextureIntoRenderState(ZombieEntity entity, ZombieEntityRenderState state, float f, CallbackInfo ci) {
        if (entity instanceof CustomTexturedEntity texturedEntity && state instanceof CustomTexturedRenderState texturedState) {
            texturedState.setCustomTexture(texturedEntity.getvariantId());
        }
    }

    @Inject(at = @At("HEAD"), method = "getTexture(Lnet/minecraft/client/render/entity/state/ZombieEntityRenderState;)Lnet/minecraft/util/Identifier;", cancellable = true)
    protected void getTexture(ZombieEntityRenderState zombieEntityRenderState, CallbackInfoReturnable<Identifier> cir) {
        if (zombieEntityRenderState instanceof CustomTexturedRenderState texturedState) {
            String customTexture = texturedState.getCustomTexture().toString();
            Identifier textureId = Identifier.of(customTexture);
            cir.setReturnValue(textureId);
            cir.cancel();
        }
    }
}
