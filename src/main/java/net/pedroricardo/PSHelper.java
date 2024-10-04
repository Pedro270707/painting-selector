package net.pedroricardo;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.PaintingVariantTags;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PSHelper {
    public static final Identifier RANDOM_PAINTING_ID = Identifier.of(PaintingSelector.MOD_ID, "random");

    public static void setPainting(ItemStack stack, @Nullable RegistryEntry<PaintingVariant> painting, RegistryWrapper.WrapperLookup lookup) {
        if (!stack.isOf(Items.PAINTING)) return;

        NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.ENTITY_DATA, NbtComponent.DEFAULT);
        if (painting != null) {
            nbtComponent = nbtComponent.with(lookup.getOps(NbtOps.INSTANCE), PaintingEntity.VARIANT_MAP_CODEC, painting).getOrThrow().apply(nbt -> nbt.putString("id", "minecraft:painting"));
            stack.set(DataComponentTypes.ENTITY_DATA, nbtComponent);
        } else {
            nbtComponent = nbtComponent.apply(compound -> PaintingEntity.VARIANT_MAP_CODEC.keys(NbtOps.INSTANCE).forEach(key -> {
                compound.remove(key.asString());
                System.out.printf("Removing key %s%n", key);
            }));
            stack.set(DataComponentTypes.ENTITY_DATA, nbtComponent);
        }
    }

    public static void setPaintingId(PlayerEntity player, ItemStack stack, Identifier paintingId, RegistryWrapper.WrapperLookup lookup) {
        Optional<RegistryEntry.Reference<PaintingVariant>> variant = lookup.createRegistryLookup().getOptionalEntry(RegistryKeys.PAINTING_VARIANT, RegistryKey.of(RegistryKeys.PAINTING_VARIANT, paintingId));
        if (variant.isEmpty() || paintingId.equals(RANDOM_PAINTING_ID)) {
            setPainting(stack, null, lookup);
        } else if (player.isCreative() || variant.get().isIn(PaintingVariantTags.PLACEABLE)) {
            setPainting(stack, variant.get(), lookup);
        }
    }
}
