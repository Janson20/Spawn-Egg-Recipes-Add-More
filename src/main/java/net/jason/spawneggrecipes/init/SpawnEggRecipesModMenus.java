
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.jason.spawneggrecipes.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.core.registries.Registries;

import net.jason.spawneggrecipes.world.inventory.WellDoneMenu;
import net.jason.spawneggrecipes.SpawnEggRecipesMod;

public class SpawnEggRecipesModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, SpawnEggRecipesMod.MODID);
	public static final DeferredHolder<MenuType<?>, MenuType<WellDoneMenu>> WELL_DONE = REGISTRY.register("well_done", () -> IMenuTypeExtension.create(WellDoneMenu::new));
}
