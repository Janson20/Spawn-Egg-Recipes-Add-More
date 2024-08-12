
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.jason.spawneggrecipes.init;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.jason.spawneggrecipes.client.gui.WellDoneScreen;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SpawnEggRecipesModScreens {
	@SubscribeEvent
	public static void clientLoad(RegisterMenuScreensEvent event) {
		event.register(SpawnEggRecipesModMenus.WELL_DONE.get(), WellDoneScreen::new);
	}
}
