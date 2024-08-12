package net.minecraft.world.entity.animal;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record FrogVariant(ResourceLocation texture) {
    public static final ResourceKey<FrogVariant> TEMPERATE = createKey("temperate");
    public static final ResourceKey<FrogVariant> WARM = createKey("warm");
    public static final ResourceKey<FrogVariant> COLD = createKey("cold");

    private static ResourceKey<FrogVariant> createKey(String p_335735_) {
        return ResourceKey.create(Registries.FROG_VARIANT, new ResourceLocation(p_335735_));
    }

    public static FrogVariant bootstrap(Registry<FrogVariant> p_336150_) {
        register(p_336150_, TEMPERATE, "textures/entity/frog/temperate_frog.png");
        register(p_336150_, WARM, "textures/entity/frog/warm_frog.png");
        return register(p_336150_, COLD, "textures/entity/frog/cold_frog.png");
    }

    private static FrogVariant register(Registry<FrogVariant> p_336004_, ResourceKey<FrogVariant> p_335508_, String p_218194_) {
        return Registry.register(p_336004_, p_335508_, new FrogVariant(new ResourceLocation(p_218194_)));
    }
}
