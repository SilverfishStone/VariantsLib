package net.silverfishstone.variantsdata.mixin;

import net.minecraft.client.render.entity.state.CreeperEntityRenderState;
import net.minecraft.util.Identifier;
import net.silverfishstone.variantsdata.resourceish.entities.render.CustomTexturedRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CreeperEntityRenderState.class)
public class CreeperEntityRenderStateMixin implements CustomTexturedRenderState {
    @Unique
    private Identifier customTexture;

    @Override
    public Identifier getCustomTexture() {
        return this.customTexture;
    }

    @Override
    public void setCustomTexture(Identifier textureId) {
        this.customTexture = textureId;
    }
}
