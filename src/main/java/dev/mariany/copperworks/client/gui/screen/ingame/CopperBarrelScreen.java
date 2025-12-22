package dev.mariany.copperworks.client.gui.screen.ingame;

import dev.mariany.copperworks.Copperworks;
import dev.mariany.copperworks.screen.CopperBarrelScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CopperBarrelScreen extends HandledScreen<CopperBarrelScreenHandler> {
    private static final Identifier TEXTURE = Copperworks.id("textures/gui/container/copper_barrel.png");
    private static final Identifier SCROLLER_TEXTURE = Copperworks.id("container/copper_barrel/scroller");
    private static final Identifier SCROLLER_DISABLED_TEXTURE = Copperworks.id(
            "container/copper_barrel/scroller_disabled"
    );

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    private static final int SLOT_BOX_SIZE = 18;

    public CopperBarrelScreen(
            CopperBarrelScreenHandler handler,
            PlayerInventory inventory,
            Text title
    ) {
        super(handler, inventory, title);

        this.backgroundHeight = 114 + this.getRows() * SLOT_BOX_SIZE;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    private int getRows() {
        return this.handler.getRows();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        int rows = this.getRows();
        int centerX = (this.width - this.backgroundWidth) / 2;
        int centerY = (this.height - this.backgroundHeight) / 2;

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                centerX,
                centerY,
                0,
                0,
                this.backgroundWidth,
                rows * SLOT_BOX_SIZE + 17,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                centerX,
                centerY + rows * SLOT_BOX_SIZE + 17,
                0,
                126,
                this.backgroundWidth,
                96,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }
}
