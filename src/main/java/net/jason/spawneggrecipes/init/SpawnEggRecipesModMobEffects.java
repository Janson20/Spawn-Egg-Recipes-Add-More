
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.jason.spawneggrecipes.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.Registries;

import net.jason.spawneggrecipes.procedures.BestProcedure;
import net.jason.spawneggrecipes.potion.InapproachableMobEffect;
import net.jason.spawneggrecipes.SpawnEggRecipesMod;

@EventBusSubscriber
public class SpawnEggRecipesModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(Registries.MOB_EFFECT, SpawnEggRecipesMod.MODID);
	public static final DeferredHolder<MobEffect, MobEffect> INAPPROACHABLE = REGISTRY.register("inapproachable", () -> new InapproachableMobEffect());

	@SubscribeEvent
	public static void onEffectRemoved(MobEffectEvent.Remove event) {
		MobEffectInstance effectInstance = event.getEffectInstance();
		if (effectInstance != null) {
			expireEffects(event.getEntity(), effectInstance);
		}
	}

	@SubscribeEvent
	public static void onEffectExpired(MobEffectEvent.Expired event) {
		MobEffectInstance effectInstance = event.getEffectInstance();
		if (effectInstance != null) {
			expireEffects(event.getEntity(), effectInstance);
		}
	}

	private static void expireEffects(Entity entity, MobEffectInstance effectInstance) {
		if (effectInstance.getEffect().is(INAPPROACHABLE)) {
			BestProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ());
		}
	}
}
