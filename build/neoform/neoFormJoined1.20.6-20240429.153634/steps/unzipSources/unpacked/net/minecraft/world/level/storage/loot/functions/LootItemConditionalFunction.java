package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public abstract class LootItemConditionalFunction implements LootItemFunction {
    protected final List<LootItemCondition> predicates;
    private final Predicate<LootContext> compositePredicates;

    protected LootItemConditionalFunction(List<LootItemCondition> p_298706_) {
        this.predicates = p_298706_;
        this.compositePredicates = Util.allOf(p_298706_);
    }

    @Override
    public abstract LootItemFunctionType<? extends LootItemConditionalFunction> getType();

    protected static <T extends LootItemConditionalFunction> P1<Mu<T>, List<LootItemCondition>> commonFields(Instance<T> p_298453_) {
        return p_298453_.group(LootItemConditions.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(p_299114_ -> p_299114_.predicates));
    }

    public final ItemStack apply(ItemStack p_80689_, LootContext p_80690_) {
        return this.compositePredicates.test(p_80690_) ? this.run(p_80689_, p_80690_) : p_80689_;
    }

    protected abstract ItemStack run(ItemStack p_80679_, LootContext p_80680_);

    @Override
    public void validate(ValidationContext p_80682_) {
        LootItemFunction.super.validate(p_80682_);

        for (int i = 0; i < this.predicates.size(); i++) {
            this.predicates.get(i).validate(p_80682_.forChild(".conditions[" + i + "]"));
        }
    }

    protected static LootItemConditionalFunction.Builder<?> simpleBuilder(Function<List<LootItemCondition>, LootItemFunction> p_80684_) {
        return new LootItemConditionalFunction.DummyBuilder(p_80684_);
    }

    public abstract static class Builder<T extends LootItemConditionalFunction.Builder<T>> implements LootItemFunction.Builder, ConditionUserBuilder<T> {
        private final ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();

        public T when(LootItemCondition.Builder p_80694_) {
            this.conditions.add(p_80694_.build());
            return this.getThis();
        }

        public final T unwrap() {
            return this.getThis();
        }

        protected abstract T getThis();

        protected List<LootItemCondition> getConditions() {
            return this.conditions.build();
        }
    }

    static final class DummyBuilder extends LootItemConditionalFunction.Builder<LootItemConditionalFunction.DummyBuilder> {
        private final Function<List<LootItemCondition>, LootItemFunction> constructor;

        public DummyBuilder(Function<List<LootItemCondition>, LootItemFunction> p_80702_) {
            this.constructor = p_80702_;
        }

        protected LootItemConditionalFunction.DummyBuilder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return this.constructor.apply(this.getConditions());
        }
    }
}
