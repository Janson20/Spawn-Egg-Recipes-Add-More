package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class InventoryMenu extends RecipeBookMenu<CraftingContainer> {
    public static final int CONTAINER_ID = 0;
    public static final int RESULT_SLOT = 0;
    public static final int CRAFT_SLOT_START = 1;
    public static final int CRAFT_SLOT_COUNT = 4;
    public static final int CRAFT_SLOT_END = 5;
    public static final int ARMOR_SLOT_START = 5;
    public static final int ARMOR_SLOT_COUNT = 4;
    public static final int ARMOR_SLOT_END = 9;
    public static final int INV_SLOT_START = 9;
    public static final int INV_SLOT_END = 36;
    public static final int USE_ROW_SLOT_START = 36;
    public static final int USE_ROW_SLOT_END = 45;
    public static final int SHIELD_SLOT = 45;
    public static final ResourceLocation BLOCK_ATLAS = new ResourceLocation("textures/atlas/blocks.png");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = new ResourceLocation("item/empty_armor_slot_helmet");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = new ResourceLocation("item/empty_armor_slot_chestplate");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = new ResourceLocation("item/empty_armor_slot_leggings");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = new ResourceLocation("item/empty_armor_slot_boots");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD = new ResourceLocation("item/empty_armor_slot_shield");
    static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{
        EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET
    };
    private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final CraftingContainer craftSlots = new TransientCraftingContainer(this, 2, 2);
    private final ResultContainer resultSlots = new ResultContainer();
    public final boolean active;
    private final Player owner;

    public InventoryMenu(Inventory p_39706_, boolean p_39707_, final Player p_39708_) {
        super(null, 0);
        this.active = p_39707_;
        this.owner = p_39708_;
        this.addSlot(new ResultSlot(p_39706_.player, this.craftSlots, this.resultSlots, 0, 154, 28));

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                this.addSlot(new Slot(this.craftSlots, j + i * 2, 98 + j * 18, 18 + i * 18));
            }
        }

        for (int k = 0; k < 4; k++) {
            final EquipmentSlot equipmentslot = SLOT_IDS[k];
            this.addSlot(new Slot(p_39706_, 39 - k, 8, 8 + k * 18) {
                @Override
                public void setByPlayer(ItemStack p_270969_, ItemStack p_299918_) {
                    InventoryMenu.onEquipItem(p_39708_, equipmentslot, p_270969_, p_299918_);
                    super.setByPlayer(p_270969_, p_299918_);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean mayPlace(ItemStack p_39746_) {
                    return p_39746_.canEquip(equipmentslot, owner);
                }

                @Override
                public boolean mayPickup(Player p_39744_) {
                    ItemStack itemstack = this.getItem();
                    return !itemstack.isEmpty() && !p_39744_.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.mayPickup(p_39744_);
                }

                @Override
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.TEXTURE_EMPTY_SLOTS[equipmentslot.getIndex()]);
                }
            });
        }

        for (int l = 0; l < 3; l++) {
            for (int j1 = 0; j1 < 9; j1++) {
                this.addSlot(new Slot(p_39706_, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; i1++) {
            this.addSlot(new Slot(p_39706_, i1, 8 + i1 * 18, 142));
        }

        this.addSlot(new Slot(p_39706_, 40, 77, 62) {
            @Override
            public void setByPlayer(ItemStack p_270479_, ItemStack p_299920_) {
                InventoryMenu.onEquipItem(p_39708_, EquipmentSlot.OFFHAND, p_270479_, p_299920_);
                super.setByPlayer(p_270479_, p_299920_);
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });
    }

    static void onEquipItem(Player p_270432_, EquipmentSlot p_270254_, ItemStack p_270316_, ItemStack p_270993_) {
        p_270432_.onEquipItem(p_270254_, p_270993_, p_270316_);
    }

    public static boolean isHotbarSlot(int p_150593_) {
        return p_150593_ >= 36 && p_150593_ < 45 || p_150593_ == 45;
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedContents p_39714_) {
        this.craftSlots.fillStackedContents(p_39714_);
    }

    @Override
    public void clearCraftingContent() {
        this.resultSlots.clearContent();
        this.craftSlots.clearContent();
    }

    @Override
    public boolean recipeMatches(RecipeHolder<? extends Recipe<CraftingContainer>> p_300901_) {
        return p_300901_.value().matches(this.craftSlots, this.owner.level());
    }

    @Override
    public void slotsChanged(Container p_39710_) {
        CraftingMenu.slotChangedCraftingGrid(this, this.owner.level(), this.owner, this.craftSlots, this.resultSlots);
    }

    @Override
    public void removed(Player p_39721_) {
        super.removed(p_39721_);
        this.resultSlots.clearContent();
        if (!p_39721_.level().isClientSide) {
            this.clearContainer(p_39721_, this.craftSlots);
        }
    }

    @Override
    public boolean stillValid(Player p_39712_) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player p_39723_, int p_39724_) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(p_39724_);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
            if (p_39724_ == 0) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (p_39724_ >= 1 && p_39724_ < 5) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (p_39724_ >= 5 && p_39724_ < 9) {
                if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentslot.getType() == EquipmentSlot.Type.ARMOR && !this.slots.get(8 - equipmentslot.getIndex()).hasItem()) {
                int i = 8 - equipmentslot.getIndex();
                if (!this.moveItemStackTo(itemstack1, i, i + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (equipmentslot == EquipmentSlot.OFFHAND && !this.slots.get(45).hasItem()) {
                if (!this.moveItemStackTo(itemstack1, 45, 46, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (p_39724_ >= 9 && p_39724_ < 36) {
                if (!this.moveItemStackTo(itemstack1, 36, 45, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (p_39724_ >= 36 && p_39724_ < 45) {
                if (!this.moveItemStackTo(itemstack1, 9, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY, itemstack);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(p_39723_, itemstack1);
            if (p_39724_ == 0) {
                p_39723_.drop(itemstack1, false);
            }
        }

        return itemstack;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack p_39716_, Slot p_39717_) {
        return p_39717_.container != this.resultSlots && super.canTakeItemForPickAll(p_39716_, p_39717_);
    }

    @Override
    public int getResultSlotIndex() {
        return 0;
    }

    @Override
    public int getGridWidth() {
        return this.craftSlots.getWidth();
    }

    @Override
    public int getGridHeight() {
        return this.craftSlots.getHeight();
    }

    @Override
    public int getSize() {
        return 5;
    }

    public CraftingContainer getCraftSlots() {
        return this.craftSlots;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    public boolean shouldMoveToInventory(int p_150591_) {
        return p_150591_ != this.getResultSlotIndex();
    }
}
