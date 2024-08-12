package net.minecraft.world.level.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class BannerPatterns {
    public static final ResourceKey<BannerPattern> BASE = create("base");
    public static final ResourceKey<BannerPattern> SQUARE_BOTTOM_LEFT = create("square_bottom_left");
    public static final ResourceKey<BannerPattern> SQUARE_BOTTOM_RIGHT = create("square_bottom_right");
    public static final ResourceKey<BannerPattern> SQUARE_TOP_LEFT = create("square_top_left");
    public static final ResourceKey<BannerPattern> SQUARE_TOP_RIGHT = create("square_top_right");
    public static final ResourceKey<BannerPattern> STRIPE_BOTTOM = create("stripe_bottom");
    public static final ResourceKey<BannerPattern> STRIPE_TOP = create("stripe_top");
    public static final ResourceKey<BannerPattern> STRIPE_LEFT = create("stripe_left");
    public static final ResourceKey<BannerPattern> STRIPE_RIGHT = create("stripe_right");
    public static final ResourceKey<BannerPattern> STRIPE_CENTER = create("stripe_center");
    public static final ResourceKey<BannerPattern> STRIPE_MIDDLE = create("stripe_middle");
    public static final ResourceKey<BannerPattern> STRIPE_DOWNRIGHT = create("stripe_downright");
    public static final ResourceKey<BannerPattern> STRIPE_DOWNLEFT = create("stripe_downleft");
    public static final ResourceKey<BannerPattern> STRIPE_SMALL = create("small_stripes");
    public static final ResourceKey<BannerPattern> CROSS = create("cross");
    public static final ResourceKey<BannerPattern> STRAIGHT_CROSS = create("straight_cross");
    public static final ResourceKey<BannerPattern> TRIANGLE_BOTTOM = create("triangle_bottom");
    public static final ResourceKey<BannerPattern> TRIANGLE_TOP = create("triangle_top");
    public static final ResourceKey<BannerPattern> TRIANGLES_BOTTOM = create("triangles_bottom");
    public static final ResourceKey<BannerPattern> TRIANGLES_TOP = create("triangles_top");
    public static final ResourceKey<BannerPattern> DIAGONAL_LEFT = create("diagonal_left");
    public static final ResourceKey<BannerPattern> DIAGONAL_RIGHT = create("diagonal_up_right");
    public static final ResourceKey<BannerPattern> DIAGONAL_LEFT_MIRROR = create("diagonal_up_left");
    public static final ResourceKey<BannerPattern> DIAGONAL_RIGHT_MIRROR = create("diagonal_right");
    public static final ResourceKey<BannerPattern> CIRCLE_MIDDLE = create("circle");
    public static final ResourceKey<BannerPattern> RHOMBUS_MIDDLE = create("rhombus");
    public static final ResourceKey<BannerPattern> HALF_VERTICAL = create("half_vertical");
    public static final ResourceKey<BannerPattern> HALF_HORIZONTAL = create("half_horizontal");
    public static final ResourceKey<BannerPattern> HALF_VERTICAL_MIRROR = create("half_vertical_right");
    public static final ResourceKey<BannerPattern> HALF_HORIZONTAL_MIRROR = create("half_horizontal_bottom");
    public static final ResourceKey<BannerPattern> BORDER = create("border");
    public static final ResourceKey<BannerPattern> CURLY_BORDER = create("curly_border");
    public static final ResourceKey<BannerPattern> GRADIENT = create("gradient");
    public static final ResourceKey<BannerPattern> GRADIENT_UP = create("gradient_up");
    public static final ResourceKey<BannerPattern> BRICKS = create("bricks");
    public static final ResourceKey<BannerPattern> GLOBE = create("globe");
    public static final ResourceKey<BannerPattern> CREEPER = create("creeper");
    public static final ResourceKey<BannerPattern> SKULL = create("skull");
    public static final ResourceKey<BannerPattern> FLOWER = create("flower");
    public static final ResourceKey<BannerPattern> MOJANG = create("mojang");
    public static final ResourceKey<BannerPattern> PIGLIN = create("piglin");
    public static final ResourceKey<BannerPattern> FLOW = create("flow");
    public static final ResourceKey<BannerPattern> GUSTER = create("guster");

    private static ResourceKey<BannerPattern> create(String p_222757_) {
        return ResourceKey.create(Registries.BANNER_PATTERN, new ResourceLocation(p_222757_));
    }

    public static void bootstrap(BootstrapContext<BannerPattern> p_332769_) {
        register(p_332769_, BASE);
        register(p_332769_, SQUARE_BOTTOM_LEFT);
        register(p_332769_, SQUARE_BOTTOM_RIGHT);
        register(p_332769_, SQUARE_TOP_LEFT);
        register(p_332769_, SQUARE_TOP_RIGHT);
        register(p_332769_, STRIPE_BOTTOM);
        register(p_332769_, STRIPE_TOP);
        register(p_332769_, STRIPE_LEFT);
        register(p_332769_, STRIPE_RIGHT);
        register(p_332769_, STRIPE_CENTER);
        register(p_332769_, STRIPE_MIDDLE);
        register(p_332769_, STRIPE_DOWNRIGHT);
        register(p_332769_, STRIPE_DOWNLEFT);
        register(p_332769_, STRIPE_SMALL);
        register(p_332769_, CROSS);
        register(p_332769_, STRAIGHT_CROSS);
        register(p_332769_, TRIANGLE_BOTTOM);
        register(p_332769_, TRIANGLE_TOP);
        register(p_332769_, TRIANGLES_BOTTOM);
        register(p_332769_, TRIANGLES_TOP);
        register(p_332769_, DIAGONAL_LEFT);
        register(p_332769_, DIAGONAL_RIGHT);
        register(p_332769_, DIAGONAL_LEFT_MIRROR);
        register(p_332769_, DIAGONAL_RIGHT_MIRROR);
        register(p_332769_, CIRCLE_MIDDLE);
        register(p_332769_, RHOMBUS_MIDDLE);
        register(p_332769_, HALF_VERTICAL);
        register(p_332769_, HALF_HORIZONTAL);
        register(p_332769_, HALF_VERTICAL_MIRROR);
        register(p_332769_, HALF_HORIZONTAL_MIRROR);
        register(p_332769_, BORDER);
        register(p_332769_, CURLY_BORDER);
        register(p_332769_, GRADIENT);
        register(p_332769_, GRADIENT_UP);
        register(p_332769_, BRICKS);
        register(p_332769_, GLOBE);
        register(p_332769_, CREEPER);
        register(p_332769_, SKULL);
        register(p_332769_, FLOWER);
        register(p_332769_, MOJANG);
        register(p_332769_, PIGLIN);
    }

    public static void register(BootstrapContext<BannerPattern> p_332752_, ResourceKey<BannerPattern> p_331267_) {
        p_332752_.register(p_331267_, new BannerPattern(p_331267_.location(), "block.minecraft.banner." + p_331267_.location().toShortLanguageKey()));
    }
}
