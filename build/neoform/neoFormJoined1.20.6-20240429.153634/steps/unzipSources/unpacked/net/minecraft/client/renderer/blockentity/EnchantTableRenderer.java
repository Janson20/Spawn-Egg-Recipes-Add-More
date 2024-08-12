package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnchantTableRenderer implements BlockEntityRenderer<EnchantingTableBlockEntity> {
    public static final Material BOOK_LOCATION = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/enchanting_table_book"));
    private final BookModel bookModel;

    public EnchantTableRenderer(BlockEntityRendererProvider.Context p_173619_) {
        this.bookModel = new BookModel(p_173619_.bakeLayer(ModelLayers.BOOK));
    }

    public void render(EnchantingTableBlockEntity p_341203_, float p_112419_, PoseStack p_112420_, MultiBufferSource p_112421_, int p_112422_, int p_112423_) {
        p_112420_.pushPose();
        p_112420_.translate(0.5F, 0.75F, 0.5F);
        float f = (float)p_341203_.time + p_112419_;
        p_112420_.translate(0.0F, 0.1F + Mth.sin(f * 0.1F) * 0.01F, 0.0F);
        float f1 = p_341203_.rot - p_341203_.oRot;

        while (f1 >= (float) Math.PI) {
            f1 -= (float) (Math.PI * 2);
        }

        while (f1 < (float) -Math.PI) {
            f1 += (float) (Math.PI * 2);
        }

        float f2 = p_341203_.oRot + f1 * p_112419_;
        p_112420_.mulPose(Axis.YP.rotation(-f2));
        p_112420_.mulPose(Axis.ZP.rotationDegrees(80.0F));
        float f3 = Mth.lerp(p_112419_, p_341203_.oFlip, p_341203_.flip);
        float f4 = Mth.frac(f3 + 0.25F) * 1.6F - 0.3F;
        float f5 = Mth.frac(f3 + 0.75F) * 1.6F - 0.3F;
        float f6 = Mth.lerp(p_112419_, p_341203_.oOpen, p_341203_.open);
        this.bookModel.setupAnim(f, Mth.clamp(f4, 0.0F, 1.0F), Mth.clamp(f5, 0.0F, 1.0F), f6);
        VertexConsumer vertexconsumer = BOOK_LOCATION.buffer(p_112421_, RenderType::entitySolid);
        this.bookModel.render(p_112420_, vertexconsumer, p_112422_, p_112423_, 1.0F, 1.0F, 1.0F, 1.0F);
        p_112420_.popPose();
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(EnchantingTableBlockEntity blockEntity) {
        net.minecraft.core.BlockPos pos = blockEntity.getBlockPos();
        return new net.minecraft.world.phys.AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1., pos.getY() + 1.5, pos.getZ() + 1.);
    }
}
