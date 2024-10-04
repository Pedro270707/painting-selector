package net.pedroricardo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.NarratedMultilineTextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

@Environment(value= EnvType.CLIENT)
public class PaintingSelectorEmptyScreen
        extends Screen {
    @Nullable
    private NarratedMultilineTextWidget textWidget;
    @Nullable
    private ButtonWidget buttonWidget;

    public PaintingSelectorEmptyScreen(Text text) {
        super(text);
    }

    @Override
    protected void init() {
        this.textWidget = this.addDrawableChild(new NarratedMultilineTextWidget(this.textRenderer, this.title, this.width));
        this.buttonWidget = this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
            MinecraftClient.getInstance().setScreen(null);
        }).build());
        this.initTabNavigation();
    }

    @Override
    protected void initTabNavigation() {
        if (this.textWidget != null) {
            this.textWidget.setMaxWidth(this.width);
            this.textWidget.setPosition(this.width / 2 - this.textWidget.getWidth() / 2, this.height / 2 - this.textRenderer.fontHeight / 2);
        }
        if (this.buttonWidget != null) {
            this.buttonWidget.setPosition(this.width / 2 - this.buttonWidget.getWidth() / 2, this.height / 2 + this.textRenderer.fontHeight / 2 + 16);
        }
    }

    @Override
    protected boolean hasUsageText() {
        return false;
    }
}
