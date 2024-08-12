package net.minecraft.client.renderer.texture.atlas;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.client.renderer.texture.atlas.sources.SourceFilter;
import net.minecraft.client.renderer.texture.atlas.sources.Unstitcher;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpriteSources {
    private static final BiMap<ResourceLocation, SpriteSourceType> TYPES = net.neoforged.neoforge.client.ClientHooks.makeSpriteSourceTypesMap();
    public static final SpriteSourceType SINGLE_FILE = register("single", SingleFile.CODEC);
    public static final SpriteSourceType DIRECTORY = register("directory", DirectoryLister.CODEC);
    public static final SpriteSourceType FILTER = register("filter", SourceFilter.CODEC);
    public static final SpriteSourceType UNSTITCHER = register("unstitch", Unstitcher.CODEC);
    public static final SpriteSourceType PALETTED_PERMUTATIONS = register("paletted_permutations", PalettedPermutations.CODEC);
    public static Codec<SpriteSourceType> TYPE_CODEC = ResourceLocation.CODEC.flatXmap(p_274717_ -> {
        SpriteSourceType spritesourcetype = TYPES.get(p_274717_);
        return spritesourcetype != null ? DataResult.success(spritesourcetype) : DataResult.error(() -> "Unknown type " + p_274717_);
    }, p_274716_ -> {
        ResourceLocation resourcelocation = TYPES.inverse().get(p_274716_);
        return p_274716_ != null ? DataResult.success(resourcelocation) : DataResult.error(() -> "Unknown type " + resourcelocation);
    });
    public static Codec<SpriteSource> CODEC = TYPE_CODEC.dispatch(SpriteSource::type, SpriteSourceType::codec);
    public static Codec<List<SpriteSource>> FILE_CODEC = CODEC.listOf().fieldOf("sources").codec();

    private static SpriteSourceType register(String p_262175_, MapCodec<? extends SpriteSource> p_338536_) {
        SpriteSourceType spritesourcetype = new SpriteSourceType(p_338536_);
        ResourceLocation resourcelocation = new ResourceLocation(p_262175_);
        SpriteSourceType spritesourcetype1 = TYPES.putIfAbsent(resourcelocation, spritesourcetype);
        if (spritesourcetype1 != null) {
            throw new IllegalStateException("Duplicate registration " + resourcelocation);
        } else {
            return spritesourcetype;
        }
    }
}
