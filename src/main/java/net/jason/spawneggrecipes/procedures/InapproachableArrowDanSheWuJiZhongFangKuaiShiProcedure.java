package net.jason.spawneggrecipes.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

import net.jason.spawneggrecipes.network.SpawnEggRecipesModVariables;

public class InapproachableArrowDanSheWuJiZhongFangKuaiShiProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		entity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("spawn_egg_recipes:nothing")))),
				(float) SpawnEggRecipesModVariables.MapVariables.get(world).difficulty);
		if (world instanceof Level _level && !_level.isClientSide())
			_level.explode(null, x, y, z, (float) SpawnEggRecipesModVariables.MapVariables.get(world).difficulty, Level.ExplosionInteraction.NONE);
	}
}
