package net.pedroricardo;

import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class PSHelper {
    public static final Identifier RANDOM_PAINTING_ID = new Identifier(PaintingSelector.MOD_ID, "random");

    public static void setPainting(ItemStack stack, @Nullable PaintingVariant painting) {
        if (!stack.isOf(Items.PAINTING)) return;

        if (painting != null) {
            NbtCompound compound = stack.getOrCreateSubNbt("EntityTag");
            PaintingEntity.writeVariantToNbt(compound, Registries.PAINTING_VARIANT.getEntry(painting));
            stack.writeNbt(compound);
        } else {
            NbtCompound compound = stack.getSubNbt("EntityTag");
            if (compound != null) {
                compound.remove(PaintingEntity.VARIANT_NBT_KEY);
                stack.setSubNbt("EntityTag", compound);
            }
        }
    }

    public static void setPaintingId(ItemStack stack, Identifier paintingId) {
        if (paintingId.equals(RANDOM_PAINTING_ID)) {
            setPainting(stack, null);
        } else {
            setPainting(stack, Registries.PAINTING_VARIANT.get(paintingId));
        }
    }
}
