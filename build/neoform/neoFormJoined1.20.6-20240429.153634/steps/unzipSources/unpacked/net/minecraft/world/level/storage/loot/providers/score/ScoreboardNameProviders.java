package net.minecraft.world.level.storage.loot.providers.score;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class ScoreboardNameProviders {
    private static final Codec<ScoreboardNameProvider> TYPED_CODEC = BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE
        .byNameCodec()
        .dispatch(ScoreboardNameProvider::getType, LootScoreProviderType::codec);
    public static final Codec<ScoreboardNameProvider> CODEC = Codec.lazyInitialized(
        () -> Codec.either(ContextScoreboardNameProvider.INLINE_CODEC, TYPED_CODEC)
                .xmap(
                    Either::unwrap,
                    p_299226_ -> p_299226_ instanceof ContextScoreboardNameProvider contextscoreboardnameprovider
                            ? Either.left(contextscoreboardnameprovider)
                            : Either.right(p_299226_)
                )
    );
    public static final LootScoreProviderType FIXED = register("fixed", FixedScoreboardNameProvider.CODEC);
    public static final LootScoreProviderType CONTEXT = register("context", ContextScoreboardNameProvider.CODEC);

    private static LootScoreProviderType register(String p_165874_, MapCodec<? extends ScoreboardNameProvider> p_338825_) {
        return Registry.register(BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE, new ResourceLocation(p_165874_), new LootScoreProviderType(p_338825_));
    }
}
