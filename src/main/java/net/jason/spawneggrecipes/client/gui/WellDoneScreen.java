package net.jason.spawneggrecipes.client.gui;

import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import net.jason.spawneggrecipes.world.inventory.WellDoneMenu;
import net.jason.spawneggrecipes.network.WellDoneButtonMessage;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;

public class WellDoneScreen extends AbstractContainerScreen<WellDoneMenu> {
	private final static HashMap<String, Object> guistate = WellDoneMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	EditBox textfield;
	Button button_effect_of_inapproachable;
	Button button_get_xp;

	public WellDoneScreen(WellDoneMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 300;
		this.imageHeight = 200;
	}

	private static final ResourceLocation texture = new ResourceLocation("spawn_egg_recipes:textures/screens/well_done.png");

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		textfield.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		if (textfield.isFocused())
			return textfield.keyPressed(key, b, c);
		return super.keyPressed(key, b, c);
	}

	@Override
	public void resize(Minecraft minecraft, int width, int height) {
		String textfieldValue = textfield.getValue();
		super.resize(minecraft, width, height);
		textfield.setValue(textfieldValue);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, Component.translatable("gui.spawn_egg_recipes.well_done.label_inapproachable"), 112, 30, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		textfield = new EditBox(this.font, this.leftPos + 67, this.topPos + 160, 165, 18, Component.translatable("gui.spawn_egg_recipes.well_done.textfield")) {
			@Override
			public void insertText(String text) {
				super.insertText(text);
				if (getValue().isEmpty())
					setSuggestion(Component.translatable("gui.spawn_egg_recipes.well_done.textfield").getString());
				else
					setSuggestion(null);
			}

			@Override
			public void moveCursorTo(int pos, boolean flag) {
				super.moveCursorTo(pos, flag);
				if (getValue().isEmpty())
					setSuggestion(Component.translatable("gui.spawn_egg_recipes.well_done.textfield").getString());
				else
					setSuggestion(null);
			}
		};
		textfield.setMaxLength(32767);
		textfield.setSuggestion(Component.translatable("gui.spawn_egg_recipes.well_done.textfield").getString());
		guistate.put("text:textfield", textfield);
		this.addWidget(this.textfield);
		button_effect_of_inapproachable = Button.builder(Component.translatable("gui.spawn_egg_recipes.well_done.button_effect_of_inapproachable"), e -> {
			if (true) {
				PacketDistributor.sendToServer(new WellDoneButtonMessage(0, x, y, z));
				WellDoneButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}).bounds(this.leftPos + 75, this.topPos + 46, 150, 20).build();
		guistate.put("button:button_effect_of_inapproachable", button_effect_of_inapproachable);
		this.addRenderableWidget(button_effect_of_inapproachable);
		button_get_xp = Button.builder(Component.translatable("gui.spawn_egg_recipes.well_done.button_get_xp"), e -> {
			if (true) {
				PacketDistributor.sendToServer(new WellDoneButtonMessage(1, x, y, z));
				WellDoneButtonMessage.handleButtonAction(entity, 1, x, y, z);
			}
		}).bounds(this.leftPos + 75, this.topPos + 68, 56, 20).build();
		guistate.put("button:button_get_xp", button_get_xp);
		this.addRenderableWidget(button_get_xp);
	}
}
