package net.minecraft.world.level.storage.loot.functions;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class FunctionReference extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<FunctionReference> CODEC = RecordCodecBuilder.mapCodec(
        p_335341_ -> commonFields(p_335341_)
                .and(ResourceKey.codec(Registries.ITEM_MODIFIER).fieldOf("name").forGetter(p_335337_ -> p_335337_.name))
                .apply(p_335341_, FunctionReference::new)
    );
    private final ResourceKey<LootItemFunction> name;

    private FunctionReference(List<LootItemCondition> p_298661_, ResourceKey<LootItemFunction> p_335440_) {
        super(p_298661_);
        this.name = p_335440_;
    }

    @Override
    public LootItemFunctionType<FunctionReference> getType() {
        return LootItemFunctions.REFERENCE;
    }

    @Override
    public void validate(ValidationContext p_279281_) {
        if (p_279281_.hasVisitedElement(this.name)) {
            p_279281_.reportProblem("Function " + this.name.location() + " is recursively called");
        } else {
            super.validate(p_279281_);
            p_279281_.resolver()
                .get(Registries.ITEM_MODIFIER, this.name)
                .ifPresentOrElse(
                    p_339571_ -> p_339571_.value().validate(p_279281_.enterElement(".{" + this.name.location() + "}", this.name)),
                    () -> p_279281_.reportProblem("Unknown function table called " + this.name.location())
                );
        }
    }

    @Override
    protected ItemStack run(ItemStack p_279458_, LootContext p_279370_) {
        LootItemFunction lootitemfunction = p_279370_.getResolver().get(Registries.ITEM_MODIFIER, this.name).map(Holder::value).orElse(null);
        if (lootitemfunction == null) {
            LOGGER.warn("Unknown function: {}", this.name.location());
            return p_279458_;
        } else {
            LootContext.VisitedEntry<?> visitedentry = LootContext.createVisitedEntry(lootitemfunction);
            if (p_279370_.pushVisitedElement(visitedentry)) {
                ItemStack itemstack;
                try {
                    itemstack = lootitemfunction.apply(p_279458_, p_279370_);
                } finally {
                    p_279370_.popVisitedElement(visitedentry);
                }

                return itemstack;
            } else {
                LOGGER.warn("Detected infinite loop in loot tables");
                return p_279458_;
            }
        }
    }

    public static LootItemConditionalFunction.Builder<?> functionReference(ResourceKey<LootItemFunction> p_335382_) {
        return simpleBuilder(p_335336_ -> new FunctionReference(p_335336_, p_335382_));
    }
}
