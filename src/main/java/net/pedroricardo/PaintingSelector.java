package net.pedroricardo;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaintingSelector implements ModInitializer {
    public static final String MOD_ID = "paintingselector";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sender.sendPacket(new Identifier(PaintingSelector.MOD_ID, "sync_client"), PacketByteBufs.empty()));
		ServerPlayNetworking.registerGlobalReceiver(new Identifier(PaintingSelector.MOD_ID, "change_painting"), (server, player, handler, buf, responseSender) -> {
			int slot = buf.readInt();
			ItemStack stack = player.getInventory().getStack(slot);
			if (stack.isEmpty() || !stack.isOf(Items.PAINTING)) return;
			PSHelper.setPaintingId(stack, buf.readIdentifier());
			player.getInventory().setStack(slot, stack);
			player.currentScreenHandler.sendContentUpdates();
		});
	}
}