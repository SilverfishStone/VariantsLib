package net.silverfishstone.variantsdata.resourceish.entities.render;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public interface CustomTexturedEntity<T> {
    Identifier getvariantId();
    RegistryEntry<T> getVariant();
    void setCustomTexture(String textureId);
}
