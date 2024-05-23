package net.pedroricardo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PaintingSelectorScreen extends Screen {
    private List<Optional<PaintingVariant>> listOld;
    private final Supplier<List<Optional<PaintingVariant>>> paintingSupplier;
    private int currentPainting = -1;
    private final PlayerEntity player;
    private final Hand hand;

    private final List<PaintingWidget> paintingWidgets = new ArrayList<>();
    private Widget textOrSearchElement = null;

    public PaintingSelectorScreen(Supplier<List<Optional<PaintingVariant>>> paintingSupplier, PlayerEntity player, Hand hand) {
        super(Text.translatable("painting_selector.title"));
        this.listOld = paintingSupplier.get();
        this.paintingSupplier = paintingSupplier;
        this.player = player;
        this.hand = hand;
    }

    public PaintingSelectorScreen(Supplier<List<Optional<PaintingVariant>>> paintingSupplier, PlayerEntity player, Hand hand, int initialPainting) {
        this(paintingSupplier, player, hand);
        int listSize = this.paintingSupplier.get().size();
        this.currentPainting = Math.clamp(initialPainting, 0, listSize - 1);
    }

    @Override
    protected void init() {
        super.init();
        if (PaintingSelectorClient.CONFIG.searchBar()) {
            this.textOrSearchElement = this.addDrawableChild(new TextFieldWidget(this.textRenderer, 160, 20, Text.translatable("painting_selector.search")));
            ((TextFieldWidget)this.textOrSearchElement).setChangedListener((value) -> {
                PaintingVariant oldVariant = this.paintingWidgets.isEmpty() || this.currentPainting >= this.paintingWidgets.size() ? null : this.paintingWidgets.get(this.currentPainting).getPainting();
                this.setPaintings(this.paintingSupplier.get().stream().filter(variant -> {
                    if (value.startsWith("author:")) {
                        return PaintingWidget.getPaintingAuthor(variant.orElse(null)).getString().toLowerCase(Locale.ROOT).contains(value.toLowerCase(Locale.ROOT).substring("author:".length()));
                    } else if (value.startsWith("size:")) {
                        return PaintingWidget.getPaintingSize(variant.orElse(null)).getString().toLowerCase(Locale.ROOT).contains(value.toLowerCase(Locale.ROOT).substring("size:".length()));
                    }
                    return PaintingWidget.getPaintingTitle(variant.orElse(null)).getString().toLowerCase(Locale.ROOT).contains(value.toLowerCase(Locale.ROOT));
                }).collect(Collectors.toList()), new FocusValue(FocusType.NONE, oldVariant));
            });
        } else {
            this.textOrSearchElement = this.addDrawableChild(new TextWidget(this.getTitle(), this.textRenderer));
        }

        RegistryEntry<PaintingVariant> painting = null;
        NbtComponent nbtComponent = this.player.getStackInHand(this.hand).getOrDefault(DataComponentTypes.ENTITY_DATA, NbtComponent.DEFAULT);
        if (!nbtComponent.isEmpty()) {
            painting = nbtComponent.get(PaintingEntity.VARIANT_MAP_CODEC).result().orElse(null);
        }
        this.setPaintings(this.paintingSupplier.get(), new FocusValue(FocusType.INITIAL, painting == null ? null : painting.value()));

        if (!this.paintingWidgets.isEmpty()) {
            this.textOrSearchElement.setPosition(this.width / 2 - this.textOrSearchElement.getWidth() - ((this.currentPainting >= this.paintingWidgets.size() || this.paintingWidgets.get(this.currentPainting).getPainting() == null) ? 20 : this.paintingWidgets.get(this.currentPainting).getPainting().getWidth() + 4), (this.height - this.textOrSearchElement.getHeight()) / 2);
        } else {
            MinecraftClient.getInstance().setScreen(new PaintingSelectorEmptyScreen(Text.translatable("painting_selector.no_paintings")));
        }
    }

    public void setPaintings(List<Optional<PaintingVariant>> list, FocusValue focusValue) {
        this.currentPainting = -1;
        for (PaintingWidget widget : this.paintingWidgets) {
            this.remove(widget);
        }
        this.paintingWidgets.clear();
        for (int i = 0; i < list.size(); i++) {
            Optional<PaintingVariant> painting = list.get(i);
            PaintingWidget widget = new PaintingWidget(this.width / 2, this.height / 2, this.textRenderer, painting.orElse(null), this.hand);
            widget.setX(this.width / 2 - (painting.map(PaintingVariant::getWidth).orElse(16)));
            widget.setY(widget.getY() - widget.getHeight() / 2 + i * 80);
            this.paintingWidgets.add(this.addDrawableChild(widget));
        }

        if (this.player != null && this.currentPainting == -1) {
            for (PaintingWidget widget : this.paintingWidgets) {
                if (focusValue.variant() != null && focusValue.variant() == widget.getPainting()) {
                    if (focusValue.type() == FocusType.INITIAL) {
                        this.setInitialFocus(widget);
                    } else if (focusValue.type() == FocusType.NORMAL) {
                        this.setFocused(widget);
                    }
                    this.centerOn(widget);
                }
            }
        }
        if (this.currentPainting == -1) {
            this.currentPainting = 0;
            if (!this.paintingWidgets.isEmpty()) {
                if (focusValue.type() == FocusType.INITIAL) {
                    this.setInitialFocus(this.paintingWidgets.getFirst());
                } else if (focusValue.type() == FocusType.NORMAL) {
                    this.setFocused(this.paintingWidgets.getFirst());
                }
                this.recenter();
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount) || this.increment((int)-Math.signum(verticalAmount));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.getFocused() instanceof PaintingWidget paintingWidget && this.paintingWidgets.contains(paintingWidget)) {
            this.centerOn(paintingWidget);
        } else if (keyCode >= 262 && keyCode <= 265 && !this.paintingWidgets.isEmpty()) {
            this.setFocused(this.paintingWidgets.get(this.currentPainting));
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        List<Optional<PaintingVariant>> list = this.paintingSupplier.get();
        if (!this.listOld.equals(list)) {
            PaintingVariant oldVariant = this.listOld.get(this.currentPainting).orElse(null);
            this.listOld = list;
            this.setPaintings(list, new FocusValue(FocusType.NORMAL, oldVariant));
            return;
        }

        for (PaintingWidget paintingWidget : this.paintingWidgets) {
            if (paintingWidget.isFocused()) {
                this.centerOn(paintingWidget);
            }
        }

        if (!this.paintingWidgets.isEmpty()) {
            this.textOrSearchElement.setPosition(this.width / 2 - this.textOrSearchElement.getWidth() - ((this.currentPainting >= this.paintingWidgets.size() || this.paintingWidgets.get(this.currentPainting).getPainting() == null) ? 20 : this.paintingWidgets.get(this.currentPainting).getPainting().getWidth() + 4), (this.height - this.textOrSearchElement.getHeight()) / 2);
        } else {
            this.textOrSearchElement.setPosition((this.width - this.textOrSearchElement.getWidth()) / 2, (this.height - this.textOrSearchElement.getHeight()) / 2);
        }
        super.tick();
    }

    public void centerOn(PaintingWidget widget) {
        for (int i = 0; i < this.paintingWidgets.size(); i++) {
            if (this.paintingWidgets.get(i) == widget) {
                if (this.isFocused() && this.getFocused() != widget) this.setFocused((Element) this.textOrSearchElement);
                this.currentPainting = i;
                break;
            }
        }
        for (int i = 0; i < this.paintingWidgets.size(); i++) {
            int pos = i - this.currentPainting;
            this.paintingWidgets.get(i).setY(this.height / 2 - this.paintingWidgets.get(i).getHeight() / 2 + pos * 80);
        }
    }

    public void recenter() {
        if (!this.paintingWidgets.isEmpty()) {
            this.centerOn(this.paintingWidgets.get(this.currentPainting));
        }
    }

    public boolean increment(int amount) {
        if (this.getFocused() instanceof PaintingWidget) this.setFocused((Element) this.textOrSearchElement);
        int listSize = this.paintingWidgets.size();
        int oldPainting = this.currentPainting;
        if (listSize > 0) {
            this.currentPainting = Math.clamp(this.currentPainting + amount, 0, listSize - 1);
        }
        this.recenter();
        return oldPainting != this.currentPainting;
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        int currentPainting = this.currentPainting;
        super.resize(client, width, height);
        this.centerOn(this.paintingWidgets.get(currentPainting));
    }

    @Override
    public void close() {
        super.close();
    }

    public enum FocusType {
        NONE,
        INITIAL,
        NORMAL
    }

    public record FocusValue(FocusType type, PaintingVariant variant) {}
}
