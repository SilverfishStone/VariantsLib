package net.silverfishstone.variantsdata.mixin;

import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.client.render.entity.state.SkeletonEntityRenderState;
import net.minecraft.util.Identifier;
import net.silverfishstone.variantsdata.resourceish.entities.render.CustomTexturedRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkeletonEntityRenderer.class)
public class SkeletonRendererMixin {


    @Inject(at = @At("HEAD"), method = "getTexture(Lnet/minecraft/client/render/entity/state/SkeletonEntityRenderState;)Lnet/minecraft/util/Identifier;", cancellable = true)
    protected void getTexture(SkeletonEntityRenderState skeletonEntityRenderState, CallbackInfoReturnable<Identifier> cir) {
        if (skeletonEntityRenderState instanceof CustomTexturedRenderState texturedState) {
            String customTexture = texturedState.getCustomTexture().toString();
            Identifier textureId = Identifier.of(customTexture);
            cir.setReturnValue(textureId);
            cir.cancel();
        }
    }
}
