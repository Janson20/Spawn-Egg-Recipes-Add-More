package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class VanillaEnchantmentTagsProvider extends EnchantmentTagsProvider {
    public VanillaEnchantmentTagsProvider(PackOutput p_341093_, CompletableFuture<HolderLookup.Provider> p_341136_) {
        super(p_341093_, p_341136_, FeatureFlagSet.of(FeatureFlags.VANILLA));
    }

    @Override
    protected void addTags(HolderLookup.Provider p_341310_) {
        this.tooltipOrder(
            p_341310_,
            new Enchantment[]{
                Enchantments.BINDING_CURSE,
                Enchantments.VANISHING_CURSE,
                Enchantments.RIPTIDE,
                Enchantments.CHANNELING,
                Enchantments.FROST_WALKER,
                Enchantments.SHARPNESS,
                Enchantments.SMITE,
                Enchantments.BANE_OF_ARTHROPODS,
                Enchantments.IMPALING,
                Enchantments.POWER,
                Enchantments.PIERCING,
                Enchantments.SWEEPING_EDGE,
                Enchantments.MULTISHOT,
                Enchantments.FIRE_ASPECT,
                Enchantments.FLAME,
                Enchantments.KNOCKBACK,
                Enchantments.PUNCH,
                Enchantments.PROTECTION,
                Enchantments.BLAST_PROTECTION,
                Enchantments.FIRE_PROTECTION,
                Enchantments.PROJECTILE_PROTECTION,
                Enchantments.FEATHER_FALLING,
                Enchantments.FORTUNE,
                Enchantments.LOOTING,
                Enchantments.SILK_TOUCH,
                Enchantments.LUCK_OF_THE_SEA,
                Enchantments.EFFICIENCY,
                Enchantments.QUICK_CHARGE,
                Enchantments.LURE,
                Enchantments.RESPIRATION,
                Enchantments.AQUA_AFFINITY,
                Enchantments.SOUL_SPEED,
                Enchantments.SWIFT_SNEAK,
                Enchantments.DEPTH_STRIDER,
                Enchantments.THORNS,
                Enchantments.LOYALTY,
                Enchantments.UNBREAKING,
                Enchantments.INFINITY,
                Enchantments.MENDING
            }
        );
    }
}
