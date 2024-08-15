
package net.jason.spawneggrecipes.item;

import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.Util;

import java.util.List;
import java.util.EnumMap;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public abstract class BedrockArmorItem extends ArmorItem {
	public static Holder<ArmorMaterial> ARMOR_MATERIAL = null;

	@SubscribeEvent
	public static void registerArmorMaterial(RegisterEvent event) {
		event.register(Registries.ARMOR_MATERIAL, registerHelper -> {
			ArmorMaterial armorMaterial = new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
				map.put(ArmorItem.Type.BOOTS, Integer.MAX_VALUE);
				map.put(ArmorItem.Type.LEGGINGS, Integer.MAX_VALUE);
				map.put(ArmorItem.Type.CHESTPLATE, Integer.MAX_VALUE);
				map.put(ArmorItem.Type.HELMET, Integer.MAX_VALUE);
				map.put(ArmorItem.Type.BODY, Integer.MAX_VALUE);
			}), 15, DeferredHolder.create(Registries.SOUND_EVENT, new ResourceLocation("item.armor.equip_netherite")), () -> Ingredient.of(new ItemStack(Blocks.BEDROCK)),
					List.of(new ArmorMaterial.Layer(new ResourceLocation("spawn_egg_recipes:bedrock"))), 1f/0, 1f/0);
			registerHelper.register(new ResourceLocation("spawn_egg_recipes:bedrock_armor"), armorMaterial);
			ARMOR_MATERIAL = BuiltInRegistries.ARMOR_MATERIAL.wrapAsHolder(armorMaterial);
		});
	}

	public BedrockArmorItem(ArmorItem.Type type, Item.Properties properties) {
		super(ARMOR_MATERIAL, type, properties);
	}

	public static class Helmet extends BedrockArmorItem {
		public Helmet() {
			super(ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(Integer.MAX_VALUE)).fireResistant());
		}

		@Override
		public boolean makesPiglinsNeutral(ItemStack itemstack, LivingEntity entity) {
			return true;
		}
	}

	public static class Chestplate extends BedrockArmorItem {
		public Chestplate() {
			super(ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(Integer.MAX_VALUE)).fireResistant());
		}

		@Override
		public boolean makesPiglinsNeutral(ItemStack itemstack, LivingEntity entity) {
			return true;
		}
	}

	public static class Leggings extends BedrockArmorItem {
		public Leggings() {
			super(ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(Integer.MAX_VALUE)).fireResistant());
		}

		@Override
		public boolean makesPiglinsNeutral(ItemStack itemstack, LivingEntity entity) {
			return true;
		}
	}

	public static class Boots extends BedrockArmorItem {
		public Boots() {
			super(ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(Integer.MAX_VALUE)).fireResistant());
		}

		@Override
		public boolean makesPiglinsNeutral(ItemStack itemstack, LivingEntity entity) {
			return true;
		}
	}
}
