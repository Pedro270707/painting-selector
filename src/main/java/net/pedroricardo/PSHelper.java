package net.pedroricardo;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class PSHelper {
    public static final Identifier RANDOM_PAINTING_ID = new Identifier(PaintingSelector.MOD_ID, "random");

    public static void setPainting(ItemStack stack, @Nullable PaintingVariant painting) {
        if (!stack.isOf(Items.PAINTING)) return;

        if (painting != null) {
            NbtComponent nbtComponent = NbtComponent.DEFAULT.with(PaintingEntity.VARIANT_MAP_CODEC, Registries.PAINTING_VARIANT.getEntry(painting)).getOrThrow().apply(nbt -> nbt.putString("id", "minecraft:painting"));
            stack.set(DataComponentTypes.ENTITY_DATA, nbtComponent);
        } else {
            stack.remove(DataComponentTypes.ENTITY_DATA);
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
