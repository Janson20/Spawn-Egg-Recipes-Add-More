
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.jason.spawneggrecipes.init;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;

import net.jason.spawneggrecipes.client.renderer.BossRenderer;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SpawnEggRecipesModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(SpawnEggRecipesModEntities.INAPPROACHABLE_ARROW.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(SpawnEggRecipesModEntities.BOSS.get(), BossRenderer::new);
	}
}
