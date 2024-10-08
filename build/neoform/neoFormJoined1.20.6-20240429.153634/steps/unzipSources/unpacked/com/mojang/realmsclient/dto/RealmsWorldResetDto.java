package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Set;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsWorldResetDto extends ValueObject implements ReflectionBasedSerialization {
    @SerializedName("seed")
    private final String seed;
    @SerializedName("worldTemplateId")
    private final long worldTemplateId;
    @SerializedName("levelType")
    private final int levelType;
    @SerializedName("generateStructures")
    private final boolean generateStructures;
    @SerializedName("experiments")
    private final Set<String> experiments;

    public RealmsWorldResetDto(String p_87643_, long p_87644_, int p_87645_, boolean p_87646_, Set<String> p_305885_) {
        this.seed = p_87643_;
        this.worldTemplateId = p_87644_;
        this.levelType = p_87645_;
        this.generateStructures = p_87646_;
        this.experiments = p_305885_;
    }
}
