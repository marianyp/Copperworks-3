package dev.mariany.copperworks.client.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ClickableTextFieldWidget extends TextFieldWidget {
    public ClickableTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, null, text);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        super.onClick(click, doubled);
        this.setFocused(true);
    }
}
