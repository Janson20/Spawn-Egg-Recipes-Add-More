package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class Lighting {
    private static final Vector3f DIFFUSE_LIGHT_0 = new Vector3f(0.2F, 1.0F, -0.7F).normalize();
    private static final Vector3f DIFFUSE_LIGHT_1 = new Vector3f(-0.2F, 1.0F, 0.7F).normalize();
    private static final Vector3f NETHER_DIFFUSE_LIGHT_0 = new Vector3f(0.2F, 1.0F, -0.7F).normalize();
    private static final Vector3f NETHER_DIFFUSE_LIGHT_1 = new Vector3f(-0.2F, -1.0F, 0.7F).normalize();
    private static final Vector3f INVENTORY_DIFFUSE_LIGHT_0 = new Vector3f(0.2F, -1.0F, 1.0F).normalize();
    private static final Vector3f INVENTORY_DIFFUSE_LIGHT_1 = new Vector3f(-0.2F, -1.0F, 0.0F).normalize();

    public static void setupNetherLevel() {
        RenderSystem.setupLevelDiffuseLighting(NETHER_DIFFUSE_LIGHT_0, NETHER_DIFFUSE_LIGHT_1);
    }

    public static void setupLevel() {
        RenderSystem.setupLevelDiffuseLighting(DIFFUSE_LIGHT_0, DIFFUSE_LIGHT_1);
    }

    public static void setupForFlatItems() {
        RenderSystem.setupGuiFlatDiffuseLighting(DIFFUSE_LIGHT_0, DIFFUSE_LIGHT_1);
    }

    public static void setupFor3DItems() {
        RenderSystem.setupGui3DDiffuseLighting(DIFFUSE_LIGHT_0, DIFFUSE_LIGHT_1);
    }

    public static void setupForEntityInInventory() {
        RenderSystem.setShaderLights(INVENTORY_DIFFUSE_LIGHT_0, INVENTORY_DIFFUSE_LIGHT_1);
    }

    public static void setupForEntityInInventory(Quaternionf p_324524_) {
        RenderSystem.setShaderLights(
            p_324524_.transform(INVENTORY_DIFFUSE_LIGHT_0, new Vector3f()), p_324524_.transform(INVENTORY_DIFFUSE_LIGHT_1, new Vector3f())
        );
    }
}
