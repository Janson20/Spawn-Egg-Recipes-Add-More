
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.jason.spawneggrecipes.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.core.registries.BuiltInRegistries;

import net.jason.spawneggrecipes.item.TotemOfInapproachableItem;
import net.jason.spawneggrecipes.item.BedrockSwordItem;
import net.jason.spawneggrecipes.item.BedrockPickaxeItem;
import net.jason.spawneggrecipes.SpawnEggRecipesMod;

public class SpawnEggRecipesModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(BuiltInRegistries.ITEM, SpawnEggRecipesMod.MODID);
	public static final DeferredHolder<Item, Item> TOTEM_OF_INAPPROACHABLE = REGISTRY.register("totem_of_inapproachable", TotemOfInapproachableItem::new);
	public static final DeferredHolder<Item, Item> BOSS_SPAWN_EGG = REGISTRY.register("boss_spawn_egg", () -> new DeferredSpawnEggItem(SpawnEggRecipesModEntities.BOSS, -1, -1, new Item.Properties()));
	public static final DeferredHolder<Item, Item> BROKEN_BEDROCK = block(SpawnEggRecipesModBlocks.BROKEN_BEDROCK);
	public static final DeferredHolder<Item, Item> BEDROCK_PICKAXE = REGISTRY.register("bedrock_pickaxe", BedrockPickaxeItem::new);
	public static final DeferredHolder<Item, Item> BEDROCK_SWORD = REGISTRY.register("bedrock_sword", BedrockSwordItem::new);

	// Start of user code block custom items
	// End of user code block custom items
	private static DeferredHolder<Item, Item> block(DeferredHolder<Block, Block> block) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
	}
}
