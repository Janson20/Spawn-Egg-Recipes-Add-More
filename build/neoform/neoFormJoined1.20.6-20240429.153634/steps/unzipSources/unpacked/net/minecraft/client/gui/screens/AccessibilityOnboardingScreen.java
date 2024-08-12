package net.minecraft.client.gui.screens;

import com.mojang.text2speech.Narrator;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AccessibilityOnboardingScreen extends Screen {
    private static final Component TITLE = Component.translatable("accessibility.onboarding.screen.title");
    private static final Component ONBOARDING_NARRATOR_MESSAGE = Component.translatable("accessibility.onboarding.screen.narrator");
    private static final int PADDING = 4;
    private static final int TITLE_PADDING = 16;
    private final LogoRenderer logoRenderer;
    private final Options options;
    private final boolean narratorAvailable;
    private boolean hasNarrated;
    private float timer;
    private final Runnable onClose;
    @Nullable
    private FocusableTextWidget textWidget;
    @Nullable
    private AbstractWidget narrationButton;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, this.initTitleYPos(), 33);

    public AccessibilityOnboardingScreen(Options p_265483_, Runnable p_300004_) {
        super(TITLE);
        this.options = p_265483_;
        this.onClose = p_300004_;
        this.logoRenderer = new LogoRenderer(true);
        this.narratorAvailable = Minecraft.getInstance().getNarrator().isActive();
    }

    @Override
    public void init() {
        LinearLayout linearlayout = this.layout.addToContents(LinearLayout.vertical());
        linearlayout.defaultCellSetting().alignHorizontallyCenter().padding(4);
        this.textWidget = linearlayout.addChild(new FocusableTextWidget(this.width, this.title, this.font), p_329717_ -> p_329717_.padding(8));
        this.narrationButton = this.options.narrator().createButton(this.options);
        this.narrationButton.active = this.narratorAvailable;
        linearlayout.addChild(this.narrationButton);
        linearlayout.addChild(
            CommonButtons.accessibility(150, p_280782_ -> this.closeAndSetScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)), false)
        );
        linearlayout.addChild(
            CommonButtons.language(
                150, p_280781_ -> this.closeAndSetScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), false
            )
        );
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_CONTINUE, p_267841_ -> this.onClose()).build());
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        if (this.textWidget != null) {
            this.textWidget.containWithin(this.width);
        }

        this.layout.arrangeElements();
    }

    @Override
    protected void setInitialFocus() {
        if (this.narratorAvailable && this.narrationButton != null) {
            this.setInitialFocus(this.narrationButton);
        } else {
            super.setInitialFocus();
        }
    }

    private int initTitleYPos() {
        return 90;
    }

    @Override
    public void onClose() {
        this.close(this.onClose);
    }

    private void closeAndSetScreen(Screen p_272914_) {
        this.close(() -> this.minecraft.setScreen(p_272914_));
    }

    private void close(Runnable p_299978_) {
        this.options.onboardAccessibility = false;
        this.options.save();
        Narrator.getNarrator().clear();
        p_299978_.run();
    }

    @Override
    public void render(GuiGraphics p_282353_, int p_265135_, int p_265032_, float p_265387_) {
        super.render(p_282353_, p_265135_, p_265032_, p_265387_);
        this.handleInitialNarrationDelay();
        this.logoRenderer.renderLogo(p_282353_, this.width, 1.0F);
    }

    @Override
    protected void renderPanorama(GuiGraphics p_330740_, float p_331952_) {
        PANORAMA.render(p_330740_, this.width, this.height, 1.0F, 0.0F);
    }

    private void handleInitialNarrationDelay() {
        if (!this.hasNarrated && this.narratorAvailable) {
            if (this.timer < 40.0F) {
                this.timer++;
            } else if (this.minecraft.isWindowActive()) {
                Narrator.getNarrator().say(ONBOARDING_NARRATOR_MESSAGE.getString(), true);
                this.hasNarrated = true;
            }
        }
    }
}
