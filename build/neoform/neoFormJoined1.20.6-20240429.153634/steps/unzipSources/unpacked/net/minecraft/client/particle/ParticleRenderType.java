package net.minecraft.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ParticleRenderType {
    ParticleRenderType TERRAIN_SHEET = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder p_107441_, TextureManager p_107442_) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            p_107441_.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator p_107444_) {
            p_107444_.end();
        }

        @Override
        public String toString() {
            return "TERRAIN_SHEET";
        }
    };
    ParticleRenderType PARTICLE_SHEET_OPAQUE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder p_107448_, TextureManager p_107449_) {
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            p_107448_.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator p_107451_) {
            p_107451_.end();
        }

        @Override
        public String toString() {
            return "PARTICLE_SHEET_OPAQUE";
        }

        @Override
        public boolean isTranslucent() {
            return false;
        }
    };
    ParticleRenderType PARTICLE_SHEET_TRANSLUCENT = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder p_107455_, TextureManager p_107456_) {
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            p_107455_.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator p_107458_) {
            p_107458_.end();
        }

        @Override
        public String toString() {
            return "PARTICLE_SHEET_TRANSLUCENT";
        }
    };
    ParticleRenderType PARTICLE_SHEET_LIT = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder p_107462_, TextureManager p_107463_) {
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            p_107462_.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator p_107465_) {
            p_107465_.end();
        }

        @Override
        public String toString() {
            return "PARTICLE_SHEET_LIT";
        }

        @Override
        public boolean isTranslucent() {
            return false;
        }
    };
    ParticleRenderType CUSTOM = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder p_107469_, TextureManager p_107470_) {
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }

        @Override
        public void end(Tesselator p_107472_) {
        }

        @Override
        public String toString() {
            return "CUSTOM";
        }
    };
    ParticleRenderType NO_RENDER = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder p_107476_, TextureManager p_107477_) {
        }

        @Override
        public void end(Tesselator p_107479_) {
        }

        @Override
        public String toString() {
            return "NO_RENDER";
        }
    };

    void begin(BufferBuilder p_107436_, TextureManager p_107437_);

    void end(Tesselator p_107438_);

    /** {@return whether this type renders before or after the translucent chunk layer} */
    default boolean isTranslucent() {
        return true;
    }
}
