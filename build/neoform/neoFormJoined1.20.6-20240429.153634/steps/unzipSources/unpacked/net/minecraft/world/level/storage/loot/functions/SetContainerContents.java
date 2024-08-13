package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetContainerContents extends LootItemConditionalFunction {
    public static final MapCodec<SetContainerContents> CODEC = RecordCodecBuilder.mapCodec(
        p_340801_ -> commonFields(p_340801_)
                .and(
                    p_340801_.group(
                        ContainerComponentManipulators.CODEC.fieldOf("component").forGetter(p_340802_ -> p_340802_.component),
                        LootPoolEntries.CODEC.listOf().fieldOf("entries").forGetter(p_298103_ -> p_298103_.entries)
                    )
                )
                .apply(p_340801_, SetContainerContents::new)
    );
    private final ContainerComponentManipulator<?> component;
    private final List<LootPoolEntryContainer> entries;

    SetContainerContents(List<LootItemCondition> p_193035_, ContainerComponentManipulator<?> p_340836_, List<LootPoolEntryContainer> p_298300_) {
        super(p_193035_);
        this.component = p_340836_;
        this.entries = List.copyOf(p_298300_);
    }

    @Override
    public LootItemFunctionType<SetContainerContents> getType() {
        return LootItemFunctions.SET_CONTENTS;
    }

    @Override
    public ItemStack run(ItemStack p_80911_, LootContext p_80912_) {
        if (p_80911_.isEmpty()) {
            return p_80911_;
        } else {
            Stream.Builder<ItemStack> builder = Stream.builder();
            this.entries
                .forEach(
                    p_80916_ -> p_80916_.expand(
                            p_80912_, p_287573_ -> p_287573_.createItemStack(LootTable.createStackSplitter(p_80912_.getLevel(), builder::add), p_80912_)
                        )
                );
            this.component.setContents(p_80911_, builder.build());
            return p_80911_;
        }
    }

    @Override
    public void validate(ValidationContext p_80918_) {
        super.validate(p_80918_);

        for (int i = 0; i < this.entries.size(); i++) {
            this.entries.get(i).validate(p_80918_.forChild(".entry[" + i + "]"));
        }
    }

    public static SetContainerContents.Builder setContents(ContainerComponentManipulator<?> p_341284_) {
        return new SetContainerContents.Builder(p_341284_);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<SetContainerContents.Builder> {
        private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();
        private final ContainerComponentManipulator<?> component;

        public Builder(ContainerComponentManipulator<?> p_341204_) {
            this.component = p_341204_;
        }

        protected SetContainerContents.Builder getThis() {
            return this;
        }

        public SetContainerContents.Builder withEntry(LootPoolEntryContainer.Builder<?> p_80931_) {
            this.entries.add(p_80931_.build());
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetContainerContents(this.getConditions(), this.component, this.entries.build());
        }
    }
}