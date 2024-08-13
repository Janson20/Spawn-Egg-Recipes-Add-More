package net.minecraft.world.item;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.crafting.Ingredient;

public class ArmorMaterials {
    public static final Holder<ArmorMaterial> LEATHER = register(
        "leather",
        Util.make(new EnumMap<>(ArmorItem.Type.class), p_323384_ -> {
            p_323384_.put(ArmorItem.Type.BOOTS, 1);
            p_323384_.put(ArmorItem.Type.LEGGINGS, 2);
            p_323384_.put(ArmorItem.Type.CHESTPLATE, 3);
            p_323384_.put(ArmorItem.Type.HELMET, 1);
            p_323384_.put(ArmorItem.Type.BODY, 3);
        }),
        15,
        SoundEvents.ARMOR_EQUIP_LEATHER,
        0.0F,
        0.0F,
        () -> Ingredient.of(Items.LEATHER),
        List.of(new ArmorMaterial.Layer(new ResourceLocation("leather"), "", true), new ArmorMaterial.Layer(new ResourceLocation("leather"), "_overlay", false))
    );
    public static final Holder<ArmorMaterial> CHAIN = register("chainmail", Util.make(new EnumMap<>(ArmorItem.Type.class), p_323382_ -> {
        p_323382_.put(ArmorItem.Type.BOOTS, 1);
        p_323382_.put(ArmorItem.Type.LEGGINGS, 4);
        p_323382_.put(ArmorItem.Type.CHESTPLATE, 5);
        p_323382_.put(ArmorItem.Type.HELMET, 2);
        p_323382_.put(ArmorItem.Type.BODY, 4);
    }), 12, SoundEvents.ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT));
    public static final Holder<ArmorMaterial> IRON = register("iron", Util.make(new EnumMap<>(ArmorItem.Type.class), p_323378_ -> {
        p_323378_.put(ArmorItem.Type.BOOTS, 2);
        p_323378_.put(ArmorItem.Type.LEGGINGS, 5);
        p_323378_.put(ArmorItem.Type.CHESTPLATE, 6);
        p_323378_.put(ArmorItem.Type.HELMET, 2);
        p_323378_.put(ArmorItem.Type.BODY, 5);
    }), 9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT));
    public static final Holder<ArmorMaterial> GOLD = register("gold", Util.make(new EnumMap<>(ArmorItem.Type.class), p_323383_ -> {
        p_323383_.put(ArmorItem.Type.BOOTS, 1);
        p_323383_.put(ArmorItem.Type.LEGGINGS, 3);
        p_323383_.put(ArmorItem.Type.CHESTPLATE, 5);
        p_323383_.put(ArmorItem.Type.HELMET, 2);
        p_323383_.put(ArmorItem.Type.BODY, 7);
    }), 25, SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, () -> Ingredient.of(Items.GOLD_INGOT));
    public static final Holder<ArmorMaterial> DIAMOND = register("diamond", Util.make(new EnumMap<>(ArmorItem.Type.class), p_323380_ -> {
        p_323380_.put(ArmorItem.Type.BOOTS, 3);
        p_323380_.put(ArmorItem.Type.LEGGINGS, 6);
        p_323380_.put(ArmorItem.Type.CHESTPLATE, 8);
        p_323380_.put(ArmorItem.Type.HELMET, 3);
        p_323380_.put(ArmorItem.Type.BODY, 11);
    }), 10, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, () -> Ingredient.of(Items.DIAMOND));
    public static final Holder<ArmorMaterial> TURTLE = register("turtle", Util.make(new EnumMap<>(ArmorItem.Type.class), p_323381_ -> {
        p_323381_.put(ArmorItem.Type.BOOTS, 2);
        p_323381_.put(ArmorItem.Type.LEGGINGS, 5);
        p_323381_.put(ArmorItem.Type.CHESTPLATE, 6);
        p_323381_.put(ArmorItem.Type.HELMET, 2);
        p_323381_.put(ArmorItem.Type.BODY, 5);
    }), 9, SoundEvents.ARMOR_EQUIP_TURTLE, 0.0F, 0.0F, () -> Ingredient.of(Items.TURTLE_SCUTE));
    public static final Holder<ArmorMaterial> NETHERITE = register("netherite", Util.make(new EnumMap<>(ArmorItem.Type.class), p_323379_ -> {
        p_323379_.put(ArmorItem.Type.BOOTS, 3);
        p_323379_.put(ArmorItem.Type.LEGGINGS, 6);
        p_323379_.put(ArmorItem.Type.CHESTPLATE, 8);
        p_323379_.put(ArmorItem.Type.HELMET, 3);
        p_323379_.put(ArmorItem.Type.BODY, 11);
    }), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.1F, () -> Ingredient.of(Items.NETHERITE_INGOT));
    public static final Holder<ArmorMaterial> ARMADILLO = register("armadillo", Util.make(new EnumMap<>(ArmorItem.Type.class), p_323385_ -> {
        p_323385_.put(ArmorItem.Type.BOOTS, 3);
        p_323385_.put(ArmorItem.Type.LEGGINGS, 6);
        p_323385_.put(ArmorItem.Type.CHESTPLATE, 8);
        p_323385_.put(ArmorItem.Type.HELMET, 3);
        p_323385_.put(ArmorItem.Type.BODY, 11);
    }), 10, SoundEvents.ARMOR_EQUIP_WOLF, 0.0F, 0.0F, () -> Ingredient.of(Items.ARMADILLO_SCUTE));

    public static Holder<ArmorMaterial> bootstrap(Registry<ArmorMaterial> p_323827_) {
        return LEATHER;
    }

    private static Holder<ArmorMaterial> register(
        String p_323589_,
        EnumMap<ArmorItem.Type, Integer> p_323819_,
        int p_323636_,
        Holder<SoundEvent> p_323958_,
        float p_323937_,
        float p_323731_,
        Supplier<Ingredient> p_323970_
    ) {
        List<ArmorMaterial.Layer> list = List.of(new ArmorMaterial.Layer(new ResourceLocation(p_323589_)));
        return register(p_323589_, p_323819_, p_323636_, p_323958_, p_323937_, p_323731_, p_323970_, list);
    }

    private static Holder<ArmorMaterial> register(
        String p_323865_,
        EnumMap<ArmorItem.Type, Integer> p_324599_,
        int p_324319_,
        Holder<SoundEvent> p_324145_,
        float p_323494_,
        float p_324549_,
        Supplier<Ingredient> p_323845_,
        List<ArmorMaterial.Layer> p_323990_
    ) {
        EnumMap<ArmorItem.Type, Integer> enummap = new EnumMap<>(ArmorItem.Type.class);

        for (ArmorItem.Type armoritem$type : ArmorItem.Type.values()) {
            enummap.put(armoritem$type, p_324599_.get(armoritem$type));
        }

        return Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            new ResourceLocation(p_323865_),
            new ArmorMaterial(enummap, p_324319_, p_324145_, p_323845_, p_323990_, p_323494_, p_324549_)
        );
    }
}