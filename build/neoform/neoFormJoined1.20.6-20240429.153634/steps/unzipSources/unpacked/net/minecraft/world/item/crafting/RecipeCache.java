package net.minecraft.world.item.crafting;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RecipeCache {
    private final RecipeCache.Entry[] entries;
    private WeakReference<RecipeManager> cachedRecipeManager = new WeakReference<>(null);

    public RecipeCache(int p_307489_) {
        this.entries = new RecipeCache.Entry[p_307489_];
    }

    public Optional<RecipeHolder<CraftingRecipe>> get(Level p_307315_, CraftingContainer p_307684_) {
        if (p_307684_.isEmpty()) {
            return Optional.empty();
        } else {
            this.validateRecipeManager(p_307315_);

            for (int i = 0; i < this.entries.length; i++) {
                RecipeCache.Entry recipecache$entry = this.entries[i];
                if (recipecache$entry != null && recipecache$entry.matches(p_307684_.getItems())) {
                    this.moveEntryToFront(i);
                    return Optional.ofNullable(recipecache$entry.value());
                }
            }

            return this.compute(p_307684_, p_307315_);
        }
    }

    private void validateRecipeManager(Level p_307638_) {
        RecipeManager recipemanager = p_307638_.getRecipeManager();
        if (recipemanager != this.cachedRecipeManager.get()) {
            this.cachedRecipeManager = new WeakReference<>(recipemanager);
            Arrays.fill(this.entries, null);
        }
    }

    private Optional<RecipeHolder<CraftingRecipe>> compute(CraftingContainer p_307329_, Level p_307202_) {
        Optional<RecipeHolder<CraftingRecipe>> optional = p_307202_.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, p_307329_, p_307202_);
        this.insert(p_307329_.getItems(), optional.orElse(null));
        return optional;
    }

    private void moveEntryToFront(int p_307277_) {
        if (p_307277_ > 0) {
            RecipeCache.Entry recipecache$entry = this.entries[p_307277_];
            System.arraycopy(this.entries, 0, this.entries, 1, p_307277_);
            this.entries[0] = recipecache$entry;
        }
    }

    private void insert(List<ItemStack> p_307307_, @Nullable RecipeHolder<CraftingRecipe> p_336146_) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(p_307307_.size(), ItemStack.EMPTY);

        for (int i = 0; i < p_307307_.size(); i++) {
            nonnulllist.set(i, p_307307_.get(i).copyWithCount(1));
        }

        System.arraycopy(this.entries, 0, this.entries, 1, this.entries.length - 1);
        this.entries[0] = new RecipeCache.Entry(nonnulllist, p_336146_);
    }

    static record Entry(NonNullList<ItemStack> key, @Nullable RecipeHolder<CraftingRecipe> value) {
        public boolean matches(List<ItemStack> p_307411_) {
            if (this.key.size() != p_307411_.size()) {
                return false;
            } else {
                for (int i = 0; i < this.key.size(); i++) {
                    if (!ItemStack.isSameItemSameComponents(this.key.get(i), p_307411_.get(i))) {
                        return false;
                    }
                }

                return true;
            }
        }
    }
}
