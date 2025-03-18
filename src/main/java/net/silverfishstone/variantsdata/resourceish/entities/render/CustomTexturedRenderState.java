package net.silverfishstone.variantsdata.resourceish.entities.render;


import net.minecraft.util.Identifier;

public interface CustomTexturedRenderState {
    Identifier getCustomTexture();
    void setCustomTexture(Identifier textureId);
}
