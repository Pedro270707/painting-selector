package net.pedroricardo.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pedroricardo.PaintingSelectorClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DecorationItem.class)
public class PaintingItemFixMixin {
    @WrapOperation(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V", ordinal = 0))
    private void decrementOnServer(ItemStack instance, int amount, Operation<Void> original, @Local(ordinal = 0) World world) {
        if (!world.isClient() || !PaintingSelectorClient.CONFIG.fixSetPaintingPlacement()) {
            original.call(instance, amount);
        }
    }
}
