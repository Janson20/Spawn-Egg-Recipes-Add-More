
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.jason.spawneggrecipes.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.registries.Registries;

import net.jason.spawneggrecipes.SpawnEggRecipesMod;

public class SpawnEggRecipesModPotions {
	public static final DeferredRegister<Potion> REGISTRY = DeferredRegister.create(Registries.POTION, SpawnEggRecipesMod.MODID);
	public static final DeferredHolder<Potion, Potion> INAPPROACHABILITY = REGISTRY.register("inapproachability",
			() -> new Potion(new MobEffectInstance(SpawnEggRecipesModMobEffects.INAPPROACHABLE, 3600, 0, false, true), new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600, 0, false, true),
					new MobEffectInstance(MobEffects.INVISIBILITY, 3600, 0, false, true), new MobEffectInstance(MobEffects.WATER_BREATHING, 3600, 0, false, true), new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600, 0, false, true),
					new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 3600, 0, false, true), new MobEffectInstance(MobEffects.LUCK, 3600, 0, false, true), new MobEffectInstance(MobEffects.REGENERATION, 3600, 0, false, true),
					new MobEffectInstance(MobEffects.CONDUIT_POWER, 3600, 0, false, true)));
}
