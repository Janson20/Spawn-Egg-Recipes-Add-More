package net.minecraft.world.item;

import net.minecraft.tags.BlockTags;

public class PickaxeItem extends DiggerItem {
    public PickaxeItem(Tier p_42961_, Item.Properties p_42964_) {
        super(p_42961_, BlockTags.MINEABLE_WITH_PICKAXE, p_42964_);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.neoforged.neoforge.common.ToolAction toolAction) {
        return net.neoforged.neoforge.common.ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(toolAction);
    }
}
