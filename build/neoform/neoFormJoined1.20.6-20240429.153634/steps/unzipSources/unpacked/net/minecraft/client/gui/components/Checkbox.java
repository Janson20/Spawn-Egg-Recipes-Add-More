package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Checkbox extends AbstractButton {
    private static final ResourceLocation CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = new ResourceLocation("widget/checkbox_selected_highlighted");
    private static final ResourceLocation CHECKBOX_SELECTED_SPRITE = new ResourceLocation("widget/checkbox_selected");
    private static final ResourceLocation CHECKBOX_HIGHLIGHTED_SPRITE = new ResourceLocation("widget/checkbox_highlighted");
    private static final ResourceLocation CHECKBOX_SPRITE = new ResourceLocation("widget/checkbox");
    private static final int TEXT_COLOR = 14737632;
    private static final int SPACING = 4;
    private static final int BOX_PADDING = 8;
    private boolean selected;
    private final Checkbox.OnValueChange onValueChange;

    Checkbox(int p_93826_, int p_93827_, Component p_93830_, Font p_309061_, boolean p_93831_, Checkbox.OnValueChange p_309172_) {
        super(p_93826_, p_93827_, getBoxSize(p_309061_) + 4 + p_309061_.width(p_93830_), getBoxSize(p_309061_), p_93830_);
        this.selected = p_93831_;
        this.onValueChange = p_309172_;
    }

    public static Checkbox.Builder builder(Component p_309029_, Font p_309027_) {
        return new Checkbox.Builder(p_309029_, p_309027_);
    }

    public static int getBoxSize(Font p_309147_) {
        return 9 + 8;
    }

    @Override
    public void onPress() {
        this.selected = !this.selected;
        this.onValueChange.onValueChange(this, this.selected);
    }

    public boolean selected() {
        return this.selected;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput p_260253_) {
        p_260253_.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                p_260253_.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
            } else {
                p_260253_.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics p_283124_, int p_282925_, int p_282705_, float p_282612_) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.enableDepthTest();
        Font font = minecraft.font;
        p_283124_.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        ResourceLocation resourcelocation;
        if (this.selected) {
            resourcelocation = this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
        } else {
            resourcelocation = this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
        }

        int i = getBoxSize(font);
        int j = this.getX() + i + 4;
        int k = this.getY() + (this.height >> 1) - (9 >> 1);
        p_283124_.blitSprite(resourcelocation, this.getX(), this.getY(), i, i);
        p_283124_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        p_283124_.drawString(font, this.getMessage(), j, k, 14737632 | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Component message;
        private final Font font;
        private int x = 0;
        private int y = 0;
        private Checkbox.OnValueChange onValueChange = Checkbox.OnValueChange.NOP;
        private boolean selected = false;
        @Nullable
        private OptionInstance<Boolean> option = null;
        @Nullable
        private Tooltip tooltip = null;

        Builder(Component p_308972_, Font p_309137_) {
            this.message = p_308972_;
            this.font = p_309137_;
        }

        public Checkbox.Builder pos(int p_309178_, int p_309168_) {
            this.x = p_309178_;
            this.y = p_309168_;
            return this;
        }

        public Checkbox.Builder onValueChange(Checkbox.OnValueChange p_308967_) {
            this.onValueChange = p_308967_;
            return this;
        }

        public Checkbox.Builder selected(boolean p_308945_) {
            this.selected = p_308945_;
            this.option = null;
            return this;
        }

        public Checkbox.Builder selected(OptionInstance<Boolean> p_309117_) {
            this.option = p_309117_;
            this.selected = p_309117_.get();
            return this;
        }

        public Checkbox.Builder tooltip(Tooltip p_309197_) {
            this.tooltip = p_309197_;
            return this;
        }

        public Checkbox build() {
            Checkbox.OnValueChange checkbox$onvaluechange = this.option == null ? this.onValueChange : (p_309064_, p_308939_) -> {
                this.option.set(p_308939_);
                this.onValueChange.onValueChange(p_309064_, p_308939_);
            };
            Checkbox checkbox = new Checkbox(this.x, this.y, this.message, this.font, this.selected, checkbox$onvaluechange);
            checkbox.setTooltip(this.tooltip);
            return checkbox;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnValueChange {
        Checkbox.OnValueChange NOP = (p_309046_, p_309014_) -> {
        };

        void onValueChange(Checkbox p_308872_, boolean p_309171_);
    }
}
