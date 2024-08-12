
package net.jason.spawneggrecipes.potion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;

import net.jason.spawneggrecipes.procedures.WellProcedure;

public class InapproachableMobEffect extends MobEffect {
	public InapproachableMobEffect() {
		super(MobEffectCategory.BENEFICIAL, -1);
	}

	@Override
	public void onEffectStarted(LivingEntity entity, int amplifier) {
		WellProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		WellProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity);
		return super.applyEffectTick(entity, amplifier);
	}
}
