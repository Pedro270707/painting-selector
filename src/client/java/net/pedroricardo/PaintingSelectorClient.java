package net.pedroricardo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.PaintingVariantTags;
import net.minecraft.util.TypedActionResult;
import net.pedroricardo.network.PSClientPackets;

import java.util.ArrayList;
import java.util.Optional;

public class PaintingSelectorClient implements ClientModInitializer {
	public static boolean inPaintingSelectorServer = false;

	public static final net.pedroricardo.PSConfig CONFIG = net.pedroricardo.PSConfig.createAndLoad();

	@Override
	public void onInitializeClient() {
		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getStackInHand(hand);
			if (player != MinecraftClient.getInstance().player) return TypedActionResult.pass(stack);
			if (stack.isOf(Items.PAINTING)) {
				MinecraftClient.getInstance().setScreen(new PaintingSelectorScreen(() -> {
					ArrayList<Optional<PaintingVariant>> paintings = new ArrayList<>();
					if (player.isCreative()) {
						paintings.add(Optional.empty());
						Registries.PAINTING_VARIANT.streamEntries().forEach(paintingVariant -> paintings.add(Optional.of(paintingVariant.value())));
					} else if (inPaintingSelectorServer) {
						paintings.add(Optional.empty());
						Registries.PAINTING_VARIANT.iterateEntries(PaintingVariantTags.PLACEABLE).forEach(paintingVariant -> paintings.add(Optional.of(paintingVariant.value())));
					} else {
						NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.ENTITY_DATA, NbtComponent.DEFAULT);
						if (nbtComponent.isEmpty()) {
							paintings.add(Optional.empty());
						} else {
							RegistryEntry<PaintingVariant> variant = nbtComponent.get(PaintingEntity.VARIANT_MAP_CODEC).result().orElse(null);
							paintings.add(Optional.ofNullable(variant == null ? null : variant.value()));
						}
					}
					return paintings;
					}, player, hand));
				return TypedActionResult.success(stack, false);
			}
			return TypedActionResult.pass(stack);
		});

		PSClientPackets.init();
	}
}