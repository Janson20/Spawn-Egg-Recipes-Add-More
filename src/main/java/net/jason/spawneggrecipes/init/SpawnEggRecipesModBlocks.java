
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.jason.spawneggrecipes.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.level.block.Block;

import net.jason.spawneggrecipes.block.BrokenBedrockBlock;
import net.jason.spawneggrecipes.SpawnEggRecipesMod;

public class SpawnEggRecipesModBlocks {
	public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(SpawnEggRecipesMod.MODID);
	public static final DeferredHolder<Block, Block> BROKEN_BEDROCK = REGISTRY.register("broken_bedrock", BrokenBedrockBlock::new);
	// Start of user code block custom blocks
	// End of user code block custom blocks
}
