package net.pedroricardo;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.pedroricardo.network.PaintingChangePacket;
import net.pedroricardo.network.PaintingSelectorSyncPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaintingSelector implements ModInitializer {
    public static final String MOD_ID = "paintingselector";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(PaintingSelectorSyncPacket.PACKET_ID, PaintingSelectorSyncPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(PaintingSelectorSyncPacket.PACKET_ID, PaintingSelectorSyncPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(PaintingChangePacket.PACKET_ID, PaintingChangePacket.CODEC);
		PayloadTypeRegistry.playS2C().register(PaintingChangePacket.PACKET_ID, PaintingChangePacket.CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sender.sendPacket(new PaintingSelectorSyncPacket()));
		ServerPlayNetworking.registerGlobalReceiver(PaintingChangePacket.PACKET_ID, (payload, context) -> {
			ItemStack stack = context.player().getInventory().getStack(payload.slot());
			if (stack.isEmpty() || !stack.isOf(Items.PAINTING)) return;
			PSHelper.setPaintingId(context.player(), stack, payload.paintingId());
			context.player().getInventory().setStack(payload.slot(), stack);
			context.player().currentScreenHandler.sendContentUpdates();
		});
	}
}