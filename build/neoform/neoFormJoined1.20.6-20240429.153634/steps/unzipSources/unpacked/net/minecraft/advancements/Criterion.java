package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.ExtraCodecs;

public record Criterion<T extends CriterionTriggerInstance>(CriterionTrigger<T> trigger, T triggerInstance) {
    private static final MapCodec<Criterion<?>> MAP_CODEC = ExtraCodecs.dispatchOptionalValue(
        "trigger", "conditions", CriteriaTriggers.CODEC, Criterion::trigger, Criterion::criterionCodec
    );
    public static final Codec<Criterion<?>> CODEC = MAP_CODEC.codec();

    private static <T extends CriterionTriggerInstance> Codec<Criterion<T>> criterionCodec(CriterionTrigger<T> p_312665_) {
        return p_312665_.codec().xmap(p_312166_ -> new Criterion<>(p_312665_, (T)p_312166_), Criterion::triggerInstance);
    }
}
