package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;

public class BookCloningRecipe extends CustomRecipe {
    public BookCloningRecipe(CraftingBookCategory p_251090_) {
        super(p_251090_);
    }

    public boolean matches(CraftingContainer p_43814_, Level p_43815_) {
        int i = 0;
        ItemStack itemstack = ItemStack.EMPTY;

        for (int j = 0; j < p_43814_.getContainerSize(); j++) {
            ItemStack itemstack1 = p_43814_.getItem(j);
            if (!itemstack1.isEmpty()) {
                if (itemstack1.is(Items.WRITTEN_BOOK)) {
                    if (!itemstack.isEmpty()) {
                        return false;
                    }

                    itemstack = itemstack1;
                } else {
                    if (!itemstack1.is(Items.WRITABLE_BOOK)) {
                        return false;
                    }

                    i++;
                }
            }
        }

        return !itemstack.isEmpty() && i > 0;
    }

    public ItemStack assemble(CraftingContainer p_43812_, HolderLookup.Provider p_335702_) {
        int i = 0;
        ItemStack itemstack = ItemStack.EMPTY;

        for (int j = 0; j < p_43812_.getContainerSize(); j++) {
            ItemStack itemstack1 = p_43812_.getItem(j);
            if (!itemstack1.isEmpty()) {
                if (itemstack1.is(Items.WRITTEN_BOOK)) {
                    if (!itemstack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }

                    itemstack = itemstack1;
                } else {
                    if (!itemstack1.is(Items.WRITABLE_BOOK)) {
                        return ItemStack.EMPTY;
                    }

                    i++;
                }
            }
        }

        WrittenBookContent writtenbookcontent = itemstack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (!itemstack.isEmpty() && i >= 1 && writtenbookcontent != null) {
            WrittenBookContent writtenbookcontent1 = writtenbookcontent.tryCraftCopy();
            if (writtenbookcontent1 == null) {
                return ItemStack.EMPTY;
            } else {
                ItemStack itemstack2 = itemstack.copyWithCount(i);
                itemstack2.set(DataComponents.WRITTEN_BOOK_CONTENT, writtenbookcontent1);
                return itemstack2;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    public NonNullList<ItemStack> getRemainingItems(CraftingContainer p_43820_) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(p_43820_.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < nonnulllist.size(); i++) {
            ItemStack itemstack = p_43820_.getItem(i);
            if (itemstack.hasCraftingRemainingItem()) {
                nonnulllist.set(i, itemstack.getCraftingRemainingItem());
            } else if (itemstack.getItem() instanceof WrittenBookItem) {
                nonnulllist.set(i, itemstack.copyWithCount(1));
                break;
            }
        }

        return nonnulllist;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.BOOK_CLONING;
    }

    @Override
    public boolean canCraftInDimensions(int p_43804_, int p_43805_) {
        return p_43804_ >= 3 && p_43805_ >= 3;
    }
}
