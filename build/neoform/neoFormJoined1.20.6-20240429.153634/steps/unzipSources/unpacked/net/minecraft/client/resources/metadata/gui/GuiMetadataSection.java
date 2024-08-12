package net.minecraft.client.resources.metadata.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record GuiMetadataSection(GuiSpriteScaling scaling) {
    public static final GuiMetadataSection DEFAULT = new GuiMetadataSection(GuiSpriteScaling.DEFAULT);
    public static final Codec<GuiMetadataSection> CODEC = RecordCodecBuilder.create(
        p_337438_ -> p_337438_.group(GuiSpriteScaling.CODEC.optionalFieldOf("scaling", GuiSpriteScaling.DEFAULT).forGetter(GuiMetadataSection::scaling))
                .apply(p_337438_, GuiMetadataSection::new)
    );
    public static final MetadataSectionType<GuiMetadataSection> TYPE = MetadataSectionType.fromCodec("gui", CODEC);
}
