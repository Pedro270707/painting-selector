package net.pedroricardo;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.pedroricardo.network.PaintingChangePacket;
import org.jetbrains.annotations.Nullable;

public class PaintingWidget extends PressableWidget {
    private final TextRenderer textRenderer;
    private final @Nullable PaintingVariant painting;
    private final Hand hand;

    public static final int RANDOM_PAINTING_WIDTH = 16, RANDOM_PAINTING_HEIGHT = 16;

    public PaintingWidget(int x, int y, TextRenderer textRenderer, @Nullable PaintingVariant painting, Hand hand) {
        super(x, y, (painting == null ? 0 : painting.getWidth()) + 100, painting == null ? 16 : painting.getHeight(), getPaintingTitle(painting));
        this.textRenderer = textRenderer;
        this.painting = painting;
        this.hand = hand;
    }

    protected static MutableText getPaintingTitle(@Nullable PaintingVariant painting) {
        return painting == null ? Text.translatable("painting.random") : Text.translatable(Registries.PAINTING_VARIANT.getId(painting).toTranslationKey("painting", "title"));
    }

    protected static MutableText getPaintingAuthor(@Nullable PaintingVariant painting) {
        return painting == null ? Text.empty() : Text.translatable(Registries.PAINTING_VARIANT.getId(painting).toTranslationKey("painting", "author"));
    }

    protected static MutableText getPaintingSize(@Nullable PaintingVariant painting) {
        return painting == null ? Text.empty() : Text.translatable("painting.dimensions", MathHelper.ceilDiv(painting.getWidth(), 16), MathHelper.ceilDiv(painting.getHeight(), 16));
    }

    @Override
    public void onPress() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            client.setScreen(null);
            return;
        }

        if (PaintingSelectorClient.inPaintingSelectorServer) {
            ItemStack itemStack = client.player.getStackInHand(this.hand);
            ClientPlayNetworking.send(new PaintingChangePacket(client.player.getInventory().getSlotWithStack(itemStack), this.getPainting() == null ? PSHelper.RANDOM_PAINTING_ID : Registries.PAINTING_VARIANT.getId(this.getPainting())));
        } else if (client.player.isInCreativeMode() && client.interactionManager != null) {
            ItemStack itemStack = client.player.getStackInHand(this.hand);
            PSHelper.setPainting(itemStack, this.getPainting());
            client.player.setStackInHand(this.hand, itemStack);
            client.interactionManager.clickCreativeStack(itemStack, 36 + client.player.getInventory().getSlotWithStack(itemStack));
            client.player.playerScreenHandler.sendContentUpdates();
        }
        client.setScreen(null);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int paintingWidth;
        int paintingHeight;
        if (this.getPainting() == null) {
            paintingWidth = RANDOM_PAINTING_WIDTH;
            paintingHeight = RANDOM_PAINTING_HEIGHT;
            context.drawGuiTexture(new Identifier(PaintingSelector.MOD_ID, "random_painting"), this.getX(), this.getY(), paintingWidth, paintingHeight);
        } else {
            paintingWidth = this.getPainting().getWidth();
            paintingHeight = this.getPainting().getHeight();
            context.drawSprite(this.getX(), this.getY(), 100, paintingWidth, paintingHeight, MinecraftClient.getInstance().getPaintingManager().getPaintingSprite(this.getPainting()));
            context.drawTextWithShadow(this.textRenderer, getPaintingSize(this.getPainting()).formatted(Formatting.GRAY), this.getX() + paintingWidth + 6, this.getY() + 20 + (paintingHeight > 20 ? 2 : 0), 0xFFFFFFFF);
        }
        if (this.isSelected()) {
            context.drawBorder(this.getX() - 1, this.getY() - 1, paintingWidth + 2, paintingHeight + 2, 0xFFFFFFFF);
        }
        context.drawTextWithShadow(this.textRenderer, getPaintingTitle(this.getPainting()).asOrderedText(), this.getX() + paintingWidth + 6, this.getY() + (paintingHeight > 20 ? 2 : 0), 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, getPaintingAuthor(this.getPainting()).formatted(Formatting.GRAY).asOrderedText(), this.getX() + paintingWidth + 6, this.getY() + 10 + (paintingHeight > 20 ? 2 : 0), 0xFFFFFFFF);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        if (this.getPainting() == null || !PaintingSelectorClient.CONFIG.narrateAll()) {
            builder.put(NarrationPart.TITLE, getPaintingTitle(this.getPainting()));
        } else {
            builder.put(NarrationPart.TITLE, Text.translatable("narration.painting_selector.widget", getPaintingTitle(this.getPainting()), getPaintingAuthor(this.getPainting()), MathHelper.ceilDiv(this.getPainting().getWidth(), 16), MathHelper.ceilDiv(this.getPainting().getHeight(), 16)));
        }
    }

    @Nullable
    public PaintingVariant getPainting() {
        return this.painting;
    }
}
