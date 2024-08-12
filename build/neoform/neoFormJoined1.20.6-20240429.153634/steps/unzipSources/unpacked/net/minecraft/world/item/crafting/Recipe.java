package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public interface Recipe<C extends Container> {
    Codec<Recipe<?>> CODEC = BuiltInRegistries.RECIPE_SERIALIZER.byNameCodec().dispatch(Recipe::getSerializer, RecipeSerializer::codec);
    Codec<java.util.Optional<net.neoforged.neoforge.common.conditions.WithConditions<Recipe<?>>>> CONDITIONAL_CODEC = net.neoforged.neoforge.common.conditions.ConditionalOps.createConditionalCodecWithConditions(CODEC);
    StreamCodec<RegistryFriendlyByteBuf, Recipe<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.RECIPE_SERIALIZER)
        .dispatch(Recipe::getSerializer, RecipeSerializer::streamCodec);

    boolean matches(C p_44002_, Level p_44003_);

    ItemStack assemble(C p_44001_, HolderLookup.Provider p_336092_);

    boolean canCraftInDimensions(int p_43999_, int p_44000_);

    ItemStack getResultItem(HolderLookup.Provider p_336125_);

    default NonNullList<ItemStack> getRemainingItems(C p_44004_) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(p_44004_.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < nonnulllist.size(); i++) {
            ItemStack item = p_44004_.getItem(i);
            if (item.hasCraftingRemainingItem()) {
                nonnulllist.set(i, item.getCraftingRemainingItem());
            }
        }

        return nonnulllist;
    }

    default NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    default boolean isSpecial() {
        return false;
    }

    default boolean showNotification() {
        return true;
    }

    default String getGroup() {
        return "";
    }

    default ItemStack getToastSymbol() {
        return new ItemStack(Blocks.CRAFTING_TABLE);
    }

    RecipeSerializer<?> getSerializer();

    RecipeType<?> getType();

    default boolean isIncomplete() {
        NonNullList<Ingredient> nonnulllist = this.getIngredients();
        return nonnulllist.isEmpty() || nonnulllist.stream().anyMatch(Ingredient::hasNoItems);
    }
}
