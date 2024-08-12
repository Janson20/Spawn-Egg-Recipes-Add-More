package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UnsupportedGraphicsWarningScreen extends Screen {
    private static final int BUTTON_PADDING = 20;
    private static final int BUTTON_MARGIN = 5;
    private static final int BUTTON_HEIGHT = 20;
    private final Component narrationMessage;
    private final FormattedText message;
    private final ImmutableList<UnsupportedGraphicsWarningScreen.ButtonOption> buttonOptions;
    private MultiLineLabel messageLines = MultiLineLabel.EMPTY;
    private int contentTop;
    private int buttonWidth;

    protected UnsupportedGraphicsWarningScreen(
        Component p_304776_, List<Component> p_304862_, ImmutableList<UnsupportedGraphicsWarningScreen.ButtonOption> p_304681_
    ) {
        super(p_304776_);
        this.message = FormattedText.composite(p_304862_);
        this.narrationMessage = CommonComponents.joinForNarration(p_304776_, ComponentUtils.formatList(p_304862_, CommonComponents.EMPTY));
        this.buttonOptions = p_304681_;
    }

    @Override
    public Component getNarrationMessage() {
        return this.narrationMessage;
    }

    @Override
    public void init() {
        for (UnsupportedGraphicsWarningScreen.ButtonOption unsupportedgraphicswarningscreen$buttonoption : this.buttonOptions) {
            this.buttonWidth = Math.max(this.buttonWidth, 20 + this.font.width(unsupportedgraphicswarningscreen$buttonoption.message) + 20);
        }

        int l = 5 + this.buttonWidth + 5;
        int i1 = l * this.buttonOptions.size();
        this.messageLines = MultiLineLabel.create(this.font, this.message, i1);
        int i = this.messageLines.getLineCount() * 9;
        this.contentTop = (int)((double)this.height / 2.0 - (double)i / 2.0);
        int j = this.contentTop + i + 9 * 2;
        int k = (int)((double)this.width / 2.0 - (double)i1 / 2.0);

        for (UnsupportedGraphicsWarningScreen.ButtonOption unsupportedgraphicswarningscreen$buttonoption1 : this.buttonOptions) {
            this.addRenderableWidget(
                Button.builder(unsupportedgraphicswarningscreen$buttonoption1.message, unsupportedgraphicswarningscreen$buttonoption1.onPress)
                    .bounds(k, j, this.buttonWidth, 20)
                    .build()
            );
            k += l;
        }
    }

    @Override
    public void render(GuiGraphics p_304747_, int p_304979_, int p_304519_, float p_304593_) {
        super.render(p_304747_, p_304979_, p_304519_, p_304593_);
        p_304747_.drawCenteredString(this.font, this.title, this.width / 2, this.contentTop - 9 * 2, -1);
        this.messageLines.renderCentered(p_304747_, this.width / 2, this.contentTop);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public static final class ButtonOption {
        final Component message;
        final Button.OnPress onPress;

        public ButtonOption(Component p_304713_, Button.OnPress p_304745_) {
            this.message = p_304713_;
            this.onPress = p_304745_;
        }
    }
}
