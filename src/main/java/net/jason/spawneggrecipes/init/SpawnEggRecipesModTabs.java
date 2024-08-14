
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.jason.spawneggrecipes.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import net.jason.spawneggrecipes.SpawnEggRecipesMod;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class SpawnEggRecipesModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SpawnEggRecipesMod.MODID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> INAPPROACHABLE_ITEMS = REGISTRY.register("inapproachable_items",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.spawn_egg_recipes.inapproachable_items")).icon(() -> new ItemStack(Items.TOTEM_OF_UNDYING)).displayItems((parameters, tabData) -> {
				tabData.accept(SpawnEggRecipesModItems.TOTEM_OF_INAPPROACHABLE.get());
				tabData.accept(SpawnEggRecipesModItems.BOSS_SPAWN_EGG.get());
			}).withSearchBar().build());

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.COMBAT) {
			tabData.accept(SpawnEggRecipesModItems.BEDROCK_SWORD.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			tabData.accept(SpawnEggRecipesModItems.BOSS_SPAWN_EGG.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
			tabData.accept(SpawnEggRecipesModBlocks.BROKEN_BEDROCK.get().asItem());
		} else if (tabData.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			tabData.accept(SpawnEggRecipesModItems.BEDROCK_PICKAXE.get());
			tabData.accept(SpawnEggRecipesModItems.BEDROCK_SHOVEL.get());
			tabData.accept(SpawnEggRecipesModItems.BEDROCK_AXE.get());
			tabData.accept(SpawnEggRecipesModItems.BEDROCK_HOE.get());
		}
	}
}
