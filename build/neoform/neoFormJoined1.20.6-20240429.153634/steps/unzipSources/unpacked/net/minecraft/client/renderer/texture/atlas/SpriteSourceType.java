package net.minecraft.client.renderer.texture.atlas;

import com.mojang.serialization.MapCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record SpriteSourceType(MapCodec<? extends SpriteSource> codec) {
}
