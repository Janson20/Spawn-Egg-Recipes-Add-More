package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PanoramaRenderer {
    public static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private final Minecraft minecraft;
    private final CubeMap cubeMap;
    private float spin;
    private float bob;

    public PanoramaRenderer(CubeMap p_110002_) {
        this.cubeMap = p_110002_;
        this.minecraft = Minecraft.getInstance();
    }

    public void render(GuiGraphics p_334063_, int p_333839_, int p_333923_, float p_110004_, float p_110005_) {
        float f = (float)((double)p_110005_ * this.minecraft.options.panoramaSpeed().get());
        this.spin = wrap(this.spin + f * 0.1F, 360.0F);
        this.bob = wrap(this.bob + f * 0.001F, (float) (Math.PI * 2));
        this.cubeMap.render(this.minecraft, 10.0F, -this.spin, p_110004_);
        RenderSystem.enableBlend();
        p_334063_.setColor(1.0F, 1.0F, 1.0F, p_110004_);
        p_334063_.blit(PANORAMA_OVERLAY, 0, 0, p_333839_, p_333923_, 0.0F, 0.0F, 16, 128, 16, 128);
        p_334063_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static float wrap(float p_249058_, float p_249548_) {
        return p_249058_ > p_249548_ ? p_249058_ - p_249548_ : p_249058_;
    }
}
