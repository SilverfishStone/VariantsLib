package net.silverfishstone.variantsdata.mixin;

import net.minecraft.client.render.entity.SpiderEntityRenderer;
import net.minecraft.client.render.entity.ZombieBaseEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;
import net.silverfishstone.variantsdata.resourceish.entities.render.CustomTexturedEntity;
import net.silverfishstone.variantsdata.resourceish.entities.render.CustomTexturedRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpiderEntityRenderer.class)
public class SpiderRendererMixin {

    @Inject(method = "updateRenderState*", at = @At("TAIL"))
    private void injectCustomTextureIntoRenderState(SpiderEntity entity, LivingEntityRenderState state, float f, CallbackInfo ci) {
        if (entity instanceof CustomTexturedEntity texturedEntity && state instanceof CustomTexturedRenderState texturedState) {
            texturedState.setCustomTexture(texturedEntity.getvariantId());
        }
    }

    @Inject(at = @At("HEAD"), method = "getTexture", cancellable = true)
    protected void getTexture(LivingEntityRenderState zombieEntityRenderState, CallbackInfoReturnable<Identifier> cir) {
        if (zombieEntityRenderState instanceof CustomTexturedRenderState texturedState) {
            String customTexture = texturedState.getCustomTexture().toString();
            Identifier textureId = Identifier.of(customTexture);
            cir.setReturnValue(textureId);
            cir.cancel();
        }
    }
}
